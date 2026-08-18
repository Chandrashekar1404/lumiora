<div align="center">

# Lumiora

### The AI-Native Education Operating System

*AI-Powered Institute Management & CRM for Modern Education Institutions*

---

🚀 Modern Architecture • 🤖 AI First • 🔒 Secure • ☁ Cloud Ready • 📊 Analytics

---

> **Transforming education through intelligence, automation, and seamless digital experiences.**

</div>

---

# 📖 About Lumiora

Lumiora is an AI-native Education Operating System designed to simplify the complete lifecycle of modern educational institutions.

Instead of using multiple disconnected applications for admissions, academics, finance, communication, and reporting, Lumiora unifies everything into one intelligent platform.

It empowers administrators, trainers, students, and staff with automation, analytics, and AI-driven insights.

---

# 🎯 Vision

To build the most intelligent and user-friendly Education Operating System that empowers institutions through automation, artificial intelligence, and data-driven decision making.

---

# 🚀 Mission

To simplify institutional management by connecting every department into one intelligent platform while delivering exceptional digital experiences for students, educators, and administrators.

---

# 💡 Problem Statement

Educational institutions often rely on multiple disconnected systems that create:

- Manual administrative work
- Poor communication
- Duplicate data
- Limited reporting
- Outdated user interfaces
- Lack of AI assistance

---

# ✅ Solution

Lumiora provides one unified platform for:

- Admissions
- Student Management
- Trainer Management
- Course Management
- Attendance
- Finance
- Communication
- Reports & Analytics
- AI Assistant

---

# 👥 User Roles

- Super Admin
- Admin
- Counselor
- Trainer
- Student
- Accountant

---

# 🛠 Technology Stack

## Frontend

- React
- Vite
- Tailwind CSS
- React Router
- Axios

## Backend

- Java
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA

## Database

- MySQL

## AI

- OpenAI

## DevOps

- Docker
- GitHub Actions
- AWS

---

# 📁 Project Structure

```text
Lumiora/

├── backend/
├── frontend/
├── database/
├── docs/
├── prompts/
├── assets/
├── .github/
├── README.md
├── ROADMAP.md
├── CHANGELOG.md
└── LICENSE
```

---

# 🗺 Roadmap

- [x] Sprint 0 – Product Foundation
- [ ] Sprint 1 – Authentication
- [ ] Sprint 2 – Student Management
- [ ] Sprint 3 – Course Management
- [ ] Sprint 4 – Trainer Management
- [ ] Sprint 5 – Dashboard
- [ ] Sprint 6 – AI Integration
- [ ] Sprint 7 – Deployment

---

# 📜 License

This project is currently under active development.

License information will be added before the first public release.

---

<div align="center">

### Built with ❤️ using Java, Spring Boot, React and AI

**Lumiora © 2026**

</div>


## 🚧 Sprint Progress

### Sprint 0 ✅

- Product Planning
- Architecture
- Documentation
- Database Design

### Sprint 1 🚀

- [x] Spring Boot Initialization
- [x] Enterprise Package Structure
- [x] BaseEntity
- [x] ApiResponse
- [ ] Global Exception Handling
- [ ] User Management
- [ ] Authentication


## 🔐 Authentication Foundation

### Role Management

Lumiora uses database-driven roles instead of hard-coded Java enums.

#### Default Roles

- SUPER_ADMIN
- ADMIN
- TRAINER
- STUDENT
- COUNSELOR
- ACCOUNTANT

#### Role Initialization

On application startup, `RoleDataInitializer` checks whether each default role already exists.

If a role does not exist, Lumiora creates it automatically.

If the role already exists, it skips creation.

This makes the initialization process idempotent and prevents duplicate role records.

### Architecture

```text
Application Startup
        ↓
RoleDataInitializer
        ↓
RoleService
        ↓
RoleRepository
        ↓
MySQL
        ↓
roles table


## 👤 User Management

Lumiora now includes a complete User Management module secured with JWT authentication and role-based authorization.

### Features

- Create users
- View all users
- View individual users
- Update user details
- Deactivate users using `INACTIVE` status
- Validate request data
- Prevent duplicate email registration
- Protect user APIs using role-based authorization
- Never expose passwords through API responses
- Standardize API responses using `ApiResponse`

### User API

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users` | Create a user |
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get a user |
| PUT | `/api/users/{id}` | Update a user |
| DELETE | `/api/users/{id}` | Deactivate a user |

### Authorization

User management APIs are currently restricted to:

- `SUPER_ADMIN`
- `ADMIN`

Students and other non-administrative roles cannot access user-management endpoints.

### User Lifecycle

```text
User Created
     ↓
ACTIVE
     ↓
Deactivate
     ↓
INACTIVE