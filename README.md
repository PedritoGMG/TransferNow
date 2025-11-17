# TransferNow

**TransferNow** is a small Spring Boot project that recreates a basic FTP-like system. It allows users to efficiently upload, manage, and share files while avoiding unnecessary duplicates. It also supports receiving files from other users in real time through public links.

## Features

- Upload files directly or use local paths to avoid duplicates.  
- Receive files from external users via a **public link**; both host and sender must be connected at the same time. Files are **not stored** until approved by the host.  
- Share files temporarily — accessible only while the application is running.  
- Organize and manage files easily.


<div style="display: flex; flex-direction: column; gap: 10px;">
  <img src="https://github.com/user-attachments/assets/f5738293-02bb-43bb-a60d-20665745b515" width="800">

  <div style="display: flex; gap: 10px;">
    <img src="https://github.com/user-attachments/assets/642c057a-4eb7-44a4-aee3-99c422a19d38" width="400">
    <img src="https://github.com/user-attachments/assets/c244419f-b206-4e86-b14d-52d526f501a7" width="400">
  </div>
</div>

## 🐳 Run the Project with Docker

You can run the backend and frontend independently with Docker:

```bash
docker-compose up --build
```

## 🔓 Opening Ports (Windows / Linux)

A Python script is included to open your ports so other users can access your app. Use the executable version for simplicity:

```bash
port_upnp.exe
```
The script will detect your local IP and open ports 8080 (backend) and 4200 (frontend).
Keep it running while you want the ports open. Press Ctrl+C to close the ports automatically.
