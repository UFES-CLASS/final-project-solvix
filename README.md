# STOCKIN
### Smart Inventory System

Desktop-based Inventory Management System for Micro, Small, and Medium Enterprises (MSMEs)

---

# Background

The development of **STOCKIN** was initiated based on an interview and observation conducted with **J'Sushi**, a Micro, Small, and Medium Enterprise (MSME) operating in the food and beverage sector.

The interview revealed that most business operations were still managed manually. Product inventory, raw material stock, incoming material transactions, production records, and financial calculations were recorded using handwritten notes. This manual process often led to inaccurate stock records, delayed updates, calculation errors, and difficulties in monitoring business performance.

Furthermore, the absence of an integrated inventory management system made it difficult for the business owner to monitor stock availability, manage production activities, and generate financial reports efficiently.

To address these challenges, **STOCKIN (Smart Inventory System)** was developed as a desktop-based application. The system integrates inventory management, production recording, incoming material management, stock monitoring, and financial reporting into a single platform. By digitizing these business processes, STOCKIN aims to improve operational efficiency, reduce human error, and support better decision-making for MSMEs.

---

# Project Overview

STOCKIN is a desktop-based Smart Inventory System designed to assist Micro, Small, and Medium Enterprises (MSMEs) in managing inventory, production, and sales activities efficiently.

The application enables users to manage products, raw materials, incoming material transactions, production activities, stock notifications, and financial reports within a single integrated system.

The system is developed using **JavaFX**, follows the **Model–View–Controller (MVC)** architectural pattern, and uses **SQLite** as its database management system.

---

# Project Objectives

- Simplify inventory management.
- Monitor raw material availability in real time.
- Record incoming materials efficiently.
- Manage production activities automatically.
- Notify users when stock reaches the minimum threshold.
- Generate financial reports based on production records.
- Improve inventory management efficiency for MSMEs.

---

# Team Members

| Name | Student ID |
|------|------------|
| M. Sabili Rizky Adyallah | 25523141 |
| Muhammad 'Ibaadurrahmaan | 25523213 |
| Rekyan Noviandary Widyaningsih | 25523050 |
| Tubagus Zaina Al Arifin Bahri Baehaqi | 25523227 |
| Razzan Chesta Ugama | 25523016 |

---

# Features

### Authentication
- Login using Owner or Staff account
- Role-based access control

### Dashboard
- Product summary
- Material summary
- Production summary
- Low stock information

### Product Management
- Add product
- Update product
- Delete product
- Search product

### Material Management
- Add material
- Update material
- Delete material
- Search material

### Incoming Material
- Record incoming materials
- Automatically update stock quantity

### Production
- Record production process
- Automatically reduce raw material stock
- Update product inventory

### Notification
- Low stock notification
- Important inventory alerts

### Financial Report
- Revenue summary
- Expense summary
- Profit calculation
- Bar chart visualization

---

# Technologies Used

| Technology | Description |
|------------|-------------|
| Java 21 | Programming Language |
| JavaFX 21 | User Interface Framework |
| SQLite | Database Management System |
| Maven | Dependency Management |
| MVC | Software Architecture |
| DAO | Data Access Pattern |
| CSS | User Interface Styling |

---

# Data Structures Implemented

The STOCKIN application utilizes several data structures from the Java Collections Framework to efficiently manage application data.

| Data Structure | Purpose |
|----------------|---------|
| ArrayList | Stores dynamic collections of products, materials, production records, users, and incoming materials. |
| List | Provides abstraction for handling collections returned by DAO classes. |
| ObservableList | Synchronizes application data with JavaFX TableView components. |
| HashMap | Organizes grouped financial and production data for reporting purposes. |

---

## ArrayList

ArrayList is the primary data structure implemented in STOCKIN. It dynamically stores business data including products, materials, incoming materials, production records, and users.

Advantages:

- Dynamic size
- Easy CRUD operations
- Fast iteration
- Easy integration with JavaFX

Used in:

- Product Module
- Material Module
- Incoming Material Module
- Production Module
- User Module

---

## List

The List interface is used throughout the DAO layer to improve flexibility and maintainability.

Benefits:

- Supports polymorphism
- Easy implementation replacement
- Cleaner architecture

---

## ObservableList

ObservableList is used within JavaFX to synchronize data displayed in TableView components.

Benefits:

- Automatic UI refresh
- Easy TableView binding
- Better user experience

---

## HashMap

HashMap is utilized to temporarily organize grouped financial and production data before generating reports.

Benefits:

- Fast key-value access
- Efficient grouping
- Supports reporting calculations

---

# Software Architecture

The project follows the **Model-View-Controller (MVC)** architecture.

```
Presentation Layer
        │
        ▼
Controller Layer
        │
        ▼
DAO Layer
        │
        ▼
SQLite Database
```

---

# Project Structure

```
src
│
├── controller
│
├── dao
│
├── database
│
├── model
│
├── view
│
├── util
│
└── resources
     ├── css
     ├── fxml
     └── images
```

---

# Database

Database Type

SQLite

Main Tables

- users
- products
- materials
- incoming_material
- production
- notifications
- activity_logs

No additional database installation is required because the database file is included in the project.

---

# System Requirements

Operating System

- Windows 10 / Windows 11

Software

- Java Development Kit (JDK) 21
- Apache Maven 3.9+
- JavaFX SDK 21
- Visual Studio Code

---

# Installation

1. Download or extract the project ZIP.

2. Open the project using IntelliJ IDEA or Visual Studio Code.

3. Ensure Java JDK 21 is selected.

4. Allow Maven to download all project dependencies.

5. Verify that the SQLite database file is located in the project directory.

---

# Running the Application

## Using Maven

```bash
cd Final-Project-Solvix
mvn clean install
mvn javafx:run

```

# Login Credentials

## Owner

Username

owner

Password

12345

---

## Staff

Username

staff

Password

12345

---

# Application Modules

| Module | Description |
|----------|-------------|
| Login | User authentication |
| Dashboard | Display system information |
| Product | Product CRUD |
| Material | Material CRUD |
| Incoming Material | Record incoming stock |
| Production | Production management |
| Notification | Low stock alerts |
| Financial Report | Revenue and profit visualization |

---

# Project Workflow

```
User

↓

Login

↓

Dashboard

↓

Manage Products

↓

Manage Materials

↓

Record Incoming Materials

↓

Production Process

↓

Financial Report

↓

Logout
```

---

# How to Build JAR

```bash
mvn clean package
```

The generated JAR file will be located in:

```
target/
```

---
