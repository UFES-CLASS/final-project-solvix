package com.stockin.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {

        try {

            Connection conn = DatabaseConnection.getConnection();

            Statement stmt = conn.createStatement();

            // USERS
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS users ("
                            + "userId INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "username TEXT UNIQUE,"
                            + "password TEXT,"
                            + "role TEXT,"
                            + "isActive INTEGER DEFAULT 1"
                            + ");"
            );

            stmt.execute(
                    "INSERT OR IGNORE INTO users(username,password,role)"
                            + " VALUES('owner','12345','OWNER');"
            );

            stmt.execute(
                    "INSERT OR IGNORE INTO users(username,password,role)"
                            + " VALUES('staff','12345','STAFF');"
            );

            // MATERIALS
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS materials ("
                            + "materialId INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "materialName TEXT NOT NULL,"
                            + "category TEXT,"
                            + "unit TEXT,"
                            + "stock INTEGER DEFAULT 0,"
                            + "minimumStock INTEGER DEFAULT 0"
                            + ");"
            );

            // INCOMING MATERIAL
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS incoming_materials ("
                            + "incomingId INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "materialId INTEGER NOT NULL,"
                            + "incomingDate TEXT,"
                            + "quantity INTEGER,"
                            + "unitPrice REAL,"
                            + "totalPrice REAL,"
                            + "supplier TEXT,"
                            + "note TEXT,"
                            + "FOREIGN KEY(materialId) REFERENCES materials(materialId)"
                            + " ON DELETE CASCADE"
                            + ");"
            );

            // PRODUCTS 
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS products ("
                            + "productId INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "productName TEXT NOT NULL,"
                            + "productImage TEXT,"
                            + "description TEXT,"
                            + "sellingPrice REAL DEFAULT 0,"
                            + "isActive INTEGER DEFAULT 1"
                            + ");"
            );

            // PRODUCTION
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS production ("
                            + "productionId INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "productId INTEGER NOT NULL,"
                            + "productionDate TEXT,"
                            + "expiredDate TEXT,"
                            + "quantityProduced INTEGER DEFAULT 0,"
                            + "quantitySold INTEGER DEFAULT 0,"
                            + "sellingPrice REAL DEFAULT 0,"
                            + "note TEXT,"
                            + "FOREIGN KEY(productId) REFERENCES products(productId)"
                            + " ON DELETE CASCADE"
                            + ");"
            );

            // NOTIFICATIONS
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS notifications ("
                            + "notificationId INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "materialId INTEGER,"
                            + "message TEXT,"
                            + "createdAt TEXT,"
                            + "isRead INTEGER DEFAULT 0,"
                            + "FOREIGN KEY(materialId) REFERENCES materials(materialId)"
                            + " ON DELETE CASCADE"
                            + ");"
            );

            seedSampleData(conn);

            System.out.println("Database Initialized");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    private static void seedSampleData(Connection conn) throws Exception {

        try (Statement check = conn.createStatement();
             ResultSet rs = check.executeQuery("SELECT COUNT(*) AS total FROM materials")) {

            if (rs.next() && rs.getInt("total") == 0) {

                try (Statement stmt = conn.createStatement()) {

                    stmt.execute(
                            "INSERT INTO materials(materialName,category,unit,stock,minimumStock) VALUES "
                                    + "('Beras Sushi','Kering','kg',20,5),"
                                    + "('Nori','Kering','pcs',100,20),"
                                    + "('Salmon','Basah','kg',10,3),"
                                    + "('Mentimun','Basah','kg',8,2);"
                    );

                }

            }

        }

        try (Statement check = conn.createStatement();
             ResultSet rs = check.executeQuery("SELECT COUNT(*) AS total FROM products")) {

            if (rs.next() && rs.getInt("total") == 0) {

                try (Statement stmt = conn.createStatement()) {

                    stmt.execute(
                            "INSERT INTO products(productName,productImage,description,sellingPrice,isActive) VALUES "
                                    + "('Salmon Roll',NULL,'Sushi roll isi salmon segar',35000,1),"
                                    + "('California Roll',NULL,'Sushi roll dengan mentimun dan telur ikan',28000,1);"
                    );

                }

            }

        }

    }

}
