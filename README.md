📁 File Management Backend API

A Node.js + Express backend application for user authentication and file management with MongoDB integration. Supports file upload, download, and admin operations. 


🚀 Live Demo
https://filemanagement-klrl.onrender.com/


🧱 Tech Stack
1.Node.js
2.Express.js
3.MongoDB (Mongoose)
4.Multer (file upload)
CORS
📂 Features

🔐 Authentication,
User Registration,
User Login


📁 File Management
Upload files,
Download files,
View uploaded files,
Delete files


👨‍💼 Admin Features
View users with file count,
Delete user folders and files



📁 Project Structure
FileManagement/
 ├── server.js
 ├── package.json
 ├── uploads/
⚙️ Installation (Local Setup)


git clone https://github.com/Madhavi148/FileManagement.git


cd FileManagement
npm install


▶️ Run Locally
node server.js

App runs at:

http://localhost:3000
🌐 Environment Variables

Create a .env file (optional):

MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/filemanagement
PORT=3000
☁️ Deployment

Deployed on Render:

Build Command:
npm install


Start Command:
npm start


📡 API Endpoints
🔐 Auth APIs


Register
POST /api/auth/register

Body:

{
  "username": "madhavi",
  "password": "1234",
  "employeeId": "EMP001"
}
Login
POST /api/auth/login
📁 File APIs
Upload File
POST /api/files/upload

Form-data:

file → File
username → Text
Get Files
GET /api/files?username=madhavi
Download File
GET /api/files/download/:username/:filename
Delete File
DELETE /api/files/:id
👨‍💼 Admin APIs
Get Users with File Count
GET /api/admin/users
Delete User Folder
DELETE /api/admin/users/:username
⚠️ Important Notes
File storage uses local /uploads directory
On Render (free tier), files are temporary
Files may be lost after restart
🔐 Security Improvements (Recommended)
Add password hashing (bcrypt)
Implement JWT authentication
Use cloud storage (AWS S3 / Cloudinary)
❌ Common Issues
1. Route not found
GET /

→ Add root route in server.js

2. Upload not working

→ Use form-data, not JSON

3. MongoDB connection error

→ Check:

MONGODB_URI
Network access (0.0.0.0/0)
4. UnknownHostException (Android)

→ Ensure correct backend URL

📱 Android Integration

Set base URL:

String BASE_URL = "https://filemanagement-klrl.onrender.com/";
📌 Future Enhancements
JWT Authentication
Role-based access control
Cloud file storage integration
UI dashboard
👩‍💻 Author

Madhavi Bojja

Android & FullStack Developer


My LinkedIn profile - https://linkedin.com/in/madhavi-bojja-725586199

📜 License

This project is open-source and free to use.
