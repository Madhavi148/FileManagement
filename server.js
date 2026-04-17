const express = require('express');
const mongoose = require('mongoose');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 3000;
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/filemanagement';

// Request Logger
app.use((req, res, next) => {
    console.log(`${new Date().toISOString()} - ${req.method} ${req.url}`);
    next();
});

// MongoDB connection
mongoose.connect(MONGODB_URI, {
    useNewUrlParser: true,
    useUnifiedTopology: true
}).then(() => console.log('Connected to MongoDB'))
  .catch(err => console.error('MongoDB connection error:', err));

const userSchema = new mongoose.Schema({
    username: { type: String, required: true, unique: true },
    password: { type: String, required: true },
    employeeId: { type: String, required: true }
});

const User = mongoose.model('User', userSchema);

const fileSchema = new mongoose.Schema({
    fileName: String,
    url: String,
    size: Number,
    uploadedBy: String,
    uploadDate: { type: Date, default: Date.now }
});

const FileMeta = mongoose.model('FileMeta', fileSchema);

// File storage config
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        const username = req.body.username || 'unknown';
        const userDir = path.join(__dirname, 'uploads', username);
        if (!fs.existsSync(userDir)) {
            fs.mkdirSync(userDir, { recursive: true });
        }
        cb(null, userDir);
    },
    filename: (req, file, cb) => {
        cb(null, Date.now() + '-' + file.originalname);
    }
});

const upload = multer({ storage });

const uploadsBaseDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadsBaseDir)) {
    fs.mkdirSync(uploadsBaseDir);
}
app.get('/', (req, res) => {
    res.send('File Management API is running 🚀');
});
// Auth Routes
app.post('/api/auth/register', async (req, res) => {
    try {
        const user = new User(req.body);
        await user.save();
        res.status(201).send({ message: 'User registered' });
    } catch (e) {
        console.error('Registration error:', e);
        res.status(400).send({ error: e.message });
    }
});

app.post('/api/auth/login', async (req, res) => {
    try {
        const { username, password } = req.body;
        const user = await User.findOne({ username, password });
        if (user) {
            res.send({ message: 'Login successful' });
        } else {
            res.status(401).send({ error: 'Invalid credentials' });
        }
    } catch (e) {
        console.error('Login error:', e);
        res.status(500).send({ error: 'Internal server error' });
    }
});

// File Routes
app.get('/api/files', async (req, res) => {
    try {
        const { username, isAdmin } = req.query;
        let query = {};
        if (isAdmin !== 'true') {
            query = { uploadedBy: username };
        } else if (username && username !== 'admin') {
            query = { uploadedBy: username };
        }
        const files = await FileMeta.find(query);
        res.send(files);
    } catch (e) {
        res.status(500).send({ error: e.message });
    }
});

app.get('/api/admin/users', async (req, res) => {
    try {
        const usersWithCounts = await FileMeta.aggregate([
            {
                $group: {
                    _id: "$uploadedBy",
                    fileCount: { $sum: 1 }
                }
            },
            {
                $project: {
                    _id: 0,
                    username: "$_id",
                    fileCount: 1
                }
            }
        ]);
        res.send(usersWithCounts);
    } catch (e) {
        res.status(500).send({ error: e.message });
    }
});

// Delete a single file
app.delete('/api/files/:id', async (req, res) => {
    try {
        console.log(`Attempting to delete file ID: ${req.params.id}`);
        const file = await FileMeta.findById(req.params.id);
        if (!file) {
            console.log('File not found in DB');
            return res.status(404).send({ message: 'File not found' });
        }

        const urlParts = file.url.split('/');
        const filename = urlParts[urlParts.length - 1];
        const filePath = path.join(__dirname, 'uploads', file.uploadedBy, filename);

        if (fs.existsSync(filePath)) {
            fs.unlinkSync(filePath);
            console.log('File deleted from disk');
        }

        await FileMeta.findByIdAndDelete(req.params.id);
        res.send({ message: 'File deleted successfully' });
    } catch (e) {
        console.error('Delete error:', e);
        res.status(500).send({ error: e.message });
    }
});

// Delete a user's folder
app.delete('/api/admin/users/:username', async (req, res) => {
    try {
        const username = req.params.username;
        console.log(`Attempting to delete folder for user: ${username}`);
        const userDir = path.join(__dirname, 'uploads', username);

        if (fs.existsSync(userDir)) {
            fs.rmSync(userDir, { recursive: true, force: true });
            console.log('Folder deleted from disk');
        }

        const result = await FileMeta.deleteMany({ uploadedBy: username });
        console.log(`Deleted ${result.deletedCount} records from DB`);
        res.send({ message: `Folder and files for ${username} deleted successfully` });
    } catch (e) {
        console.error('Delete folder error:', e);
        res.status(500).send({ error: e.message });
    }
});

app.post('/api/files/upload', upload.single('file'), async (req, res) => {
    try {
        const username = req.body.username;
        const host = req.get('host');
        const protocol = req.protocol;
        const fileMeta = new FileMeta({
            fileName: req.file.originalname,
            url: `${protocol}://${host}/api/files/download/${username}/${req.file.filename}`,
            size: req.file.size,
            uploadedBy: username
        });
        await fileMeta.save();
        res.status(201).send(fileMeta);
    } catch (e) {
        console.error('Upload error:', e);
        res.status(500).send({ error: e.message });
    }
});

app.get('/api/files/download/:username/:filename', (req, res) => {
    const filePath = path.join(__dirname, 'uploads', req.params.username, req.params.filename);
    if (fs.existsSync(filePath)) {
        res.download(filePath);
    } else {
        res.status(404).send({ error: 'File not found on server' });
    }
});

// Catch-all for unmatched routes
app.use((req, res) => {
    console.log(`404 - Unmatched Request: ${req.method} ${req.url}`);
    res.status(404).send({ error: `Route ${req.method} ${req.url} not found` });
});

app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on port ${PORT}`);
});
