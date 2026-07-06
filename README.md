## Project Name
- STOCKIN is a desktop-based Smart Inventory System developed to assist Micro, Small, and Medium Enterprises (MSMEs) in managing inventory, production, and sales activities efficiently. The system enables users to manage product information, monitor material stock, records incoming materials, and manage production data efficiently. It also provides stock notifications and a financial report that summarizes revenue and sales performance based on production records. The system supports two user roles, Owner and Staff, and is developed using JavaFX with the Model View Controller (MVC) architecture and SQLite as the database management system

## Team Member
- M.Sabili Rizky Adyallah (25523141)
- Muhammad 'Ibaadurrahmaan (25523213)
- Rekyan Noviandary Widyaningsih (25523050)
- Tubagus Zaina Al Arifin Bahri Baehaqi (25523227)
- Razzan Chesta Ugama (25523016)

## Description  :
1. System Requirements Analysis: The initial phase involves identifying user needs and inventory management business processes, as well as determining the key features to be developed in the system.

2. Database Design: Designing the SQLite database structure, which includes tables for users, raw materials, products, production, incoming goods, and other supporting data, to ensure all data is stored in a structured manner.

3. Login Page Development: Developing a user authentication feature using a username and password validated against the database so that only users with accounts can access the system.       

4. Dashboard Development: Creating a dashboard page as an information hub that displays data summaries such as product quantities, raw materials, production activities, and navigation to all system features.

5. Material Management Module: Developing features to add, edit, delete, and search for raw material data, thereby simplifying the material inventory management process.

6. Incoming Material Module: Creating a feature for recording incoming goods that automatically updates raw material inventory levels whenever new stock is added.

7. Product Module: Develop a product data management page that includes functions for adding, editing, deleting, and searching for data on products manufactured by the company.

8. Production Module: Develop production features that link raw materials to finished products. The system will adjust material inventory levels based on the quantities used during the production process.

9. Notification Module: Add a notification system to provide users with important information, such as when raw material inventory is running low or when specific activities occur within the system.

10. Database Integration: Connect all modules to the database using the DAO (Data Access Object) concept so that CRUD (Create, Read, Update, Delete) operations can run smoothly.

11. System Testing: Test all features to ensure every function operates as intended, from login and data management to the production process.

12. Financial Report: Developing a financial report page that presents data in the form of a “bar chart” so that users can view comparisons of revenue, expenses, and profits more easily and in a more informative way.

## How To Run
- cd C:\xamp\VScode\final-project-solvix\Final-Project-Solvix
- mvn javafx:run