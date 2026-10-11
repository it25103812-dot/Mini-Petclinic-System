# 🐾 Mini PetClinic System

### 🏥 A Smart Veterinary Clinic Management System

Welcome to the **Mini PetClinic System**! 🐶🐱

This is a web-based application designed to simplify veterinary clinic management. It helps clinic staff manage pet owners, pets, appointments, medical visit records, and doctor accounts efficiently.

## ✨ Features

* 🔐 **Secure Login** – Authentication and role-based access control.
* 📊 **Dashboard** – View important clinic information.
* 👨‍👩‍👧 **Owner Management** – Register and manage pet owners.
* 🐕 **Pet Management** – Add, view, and manage pet details.
* 📅 **Appointment Management** – Schedule and manage appointments.
* 🩺 **Visit Records** – Record diagnoses, treatments, and medical notes.
* 🗓️ **Calendar View** – View scheduled appointments.
* 👨‍⚕️ **Doctor Management** – Manage doctor accounts.
* 🛡️ **Admin Controls** – Access administrative functions.

## 🛠️ Technologies Used

| Technology         | Purpose                                 |
| ------------------ | --------------------------------------- |
| ☕ Java 17          | Programming language                    |
| 🍃 Spring Boot     | Backend framework                       |
| 🎨 HTML & CSS      | Frontend design                         |
| 🖥️ Thymeleaf      | Dynamic web pages                       |
| 🗄️ MySQL          | Database management                     |
| 🔗 Spring Data JPA | Database interaction                    |
| 🔐 Spring Security | Authentication and authorization        |
| 📦 Maven           | Project build and dependency management |

## 👥 User Roles

### 👨‍⚕️ Doctor

* 🐾 Manage pet owners and pets.
* 📅 Manage appointments.
* 🩺 Record medical visits.
* 📊 Access the clinic dashboard.

### 🛡️ Administrator

* 👨‍⚕️ Access doctor functionalities.
* 👤 Manage doctor accounts.
* 🗑️ Permanently delete records where permitted.
* ⚙️ Manage administrative functions.

## 🗄️ Database Structure

The system uses a MySQL database named `clinic`.

Main database tables:

* 👤 `app_users` – Stores user accounts, roles, and doctor information.
* 🧑‍🤝‍🧑 `owners` – Stores pet owner information.
* 🐕 `pets` – Stores pet details and owner relationships.
* 📅 `appointments` – Stores appointment and booking information.

## ⚙️ Installation and Setup

### 📋 Requirements

Make sure you have installed:

* ☕ Java JDK 17 or later
* 🗄️ MySQL Server
* 📦 Maven
* 💻 An IDE such as IntelliJ IDEA, Eclipse, or VS Code (optional)

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/it25103812-dot/Mini-Petclinic-Systemmm.git
```

### 2️⃣ Open the Project

📂 Open the downloaded project folder using your preferred IDE.

### 3️⃣ Configure the Database

Create the database in MySQL:

```sql
CREATE DATABASE clinic;
```

⚙️ Configure the database URL, username, and password in `src/main/resources/application.properties`.

🔒 **Security tip:** Use environment variables for database credentials. Never upload real passwords to GitHub.

### 4️⃣ Run the Application

Open a terminal in the project directory and execute:

```bash
mvn spring-boot:run
```

### 5️⃣ Access the Application

🌐 Open your browser and visit:

**http://localhost:8080**

🔑 Log in using the development credentials configured in the project.

## 🚀 Future Improvements

* 🧪 Add automated testing.
* 📈 Improve reports and analytics.
* 🔔 Add appointment reminders.
* 🎨 Enhance the user interface.
* 🔐 Strengthen application security.
* ☁️ Deploy the application online.

## 🔗 GitHub Repository

📌 **Project Source Code:**

https://github.com/it25103812-dot/Mini-Petclinic-Systemmm

## 👨‍💻 Author

Developed as a **Mini PetClinic System project**.

## ❤️ Support

⭐ If you find this project useful, please consider giving the repository a star!

🐾 Thank you for visiting the Mini PetClinic System! 🐶🐱
