package com.stockin.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.stockin.util.PasswordUtil;

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

            // Akun default dibuat dengan password ter-hash (bukan plaintext).
            seedUserIfMissing(conn, "owner", "12345", "OWNER");
            seedUserIfMissing(conn, "staff", "12345", "STAFF");

            // Migrasi: kalau database lama masih punya password plaintext
            // (dari versi sebelum hashing diterapkan), hash-kan sekarang
            // supaya user tetap bisa login dengan password yang sama.
            migrateLegacyPlaintextPasswords(conn);

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
                            + "sku TEXT,"
                            + "category TEXT,"
                            + "description TEXT,"
                            + "sellingPrice REAL DEFAULT 0,"
                            + "isActive INTEGER DEFAULT 1"
                            + ");"
            );

            // Migrasi: database lama belum punya kolom sku/category di
            // tabel products, jadi ditambahkan di sini kalau belum ada.
            addColumnIfMissing(conn, "products", "sku", "TEXT");
            addColumnIfMissing(conn, "products", "category", "TEXT");

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

            // PRODUCT MATERIALS (Bill of Materials / BOM): daftar bahan + jumlah
            // yang dibutuhkan untuk membuat SATU unit sebuah produk. Dipakai
            // supaya saat Production dicatat, sistem bisa otomatis menghitung
            // dan mengurangi stok bahan yang diperlukan tanpa harus dipilih
            // manual satu per satu setiap kali produksi.
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS product_materials (" 
                            + "productMaterialId INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "productId INTEGER NOT NULL,"
                            + "materialId INTEGER NOT NULL,"
                            + "quantityRequired INTEGER NOT NULL,"
                            + "FOREIGN KEY(productId) REFERENCES products(productId)"
                            + " ON DELETE CASCADE,"
                            + "FOREIGN KEY(materialId) REFERENCES materials(materialId)"
                            + " ON DELETE RESTRICT"
                            + ");"
            );

            // PRODUCTION MATERIALS (junction table: bahan yang dipakai pada setiap batch produksi)
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS production_materials (" 
                            + "productionMaterialId INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "productionId INTEGER NOT NULL,"
                            + "materialId INTEGER NOT NULL,"
                            + "quantityUsed INTEGER NOT NULL,"
                            + "FOREIGN KEY(productionId) REFERENCES production(productionId)"
                            + " ON DELETE CASCADE,"
                            + "FOREIGN KEY(materialId) REFERENCES materials(materialId)"
                            + " ON DELETE RESTRICT"
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

            // ACTIVITY LOG (dipakai oleh panel "Recent Activity Log" di Dashboard)
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS activity_log ("
                            + "activityId INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "actor TEXT,"
                            + "action TEXT,"
                            + "type TEXT,"
                            + "activityDate TEXT,"
                            + "activityTime TEXT"
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
                                    + "('Beras Sushi','Dry','kg',60,10),"
                                    + "('Nori','Dry','pcs',200,30),"
                                    + "('Wasabi','Dry','pcs',120,20),"
                                    + "('Cuka Beras','Dry','liter',15,3),"
                                    + "('Gula Pasir','Dry','kg',10,2),"
                                    + "('Garam','Dry','kg',8,2),"
                                    + "('Wijen','Dry','kg',5,1),"
                                    + "('Kecap Asin','Dry','liter',20,4),"
                                    + "('Mayones Jepang','Dry','liter',12,3),"
                                    + "('Saus Unagi','Dry','liter',10,2),"
                                    + "('Minyak Goreng','Dry','liter',18,4),"
                                    + "('Tepung Panko','Dry','kg',7,2),"
                                    + "('Salmon','Wet','kg',15,3),"
                                    + "('Tuna','Wet','kg',12,3),"
                                    + "('Udang','Wet','kg',10,2),"
                                    + "('Kani','Wet','kg',9,2),"
                                    + "('Telur Ayam','Wet','pcs',60,12),"
                                    + "('Daging Ayam','Wet','kg',8,2),"
                                    + "('Mentimun','Wet','kg',10,2),"
                                    + "('Alpukat','Wet','kg',1,3),"
                                    + "('Cream Cheese','Wet','kg',6,2),"
                                    + "('Tobiko','Wet','kg',0,1),"
                                    + "('Ikan Tenggiri','Wet','kg',8,2),"
                                    + "('Daging Sapi','Wet','kg',6,2),"
                                    + "('Jamur Shiitake','Wet','kg',5,1),"
                                    + "('Timun Jepang','Wet','kg',8,2),"
                                    + "('Masago','Wet','kg',4,1),"
                                    + "('Saus Mentai','Dry','liter',8,2),"
                                    + "('Saus Teriyaki','Dry','liter',10,2),"
                                    + "('Bawang Goreng','Dry','kg',3,1),"
                                    + "('Keju Cheddar','Wet','kg',5,1),"
                                    + "('Mangga','Wet','kg',6,2),"
                                    + "('Kertas Soya','Dry','pcs',100,20),"
                                    + "('Jeruk Nipis','Wet','kg',4,1);"
                    );

                }

            }

        }

        try (Statement check = conn.createStatement();
             ResultSet rs = check.executeQuery("SELECT COUNT(*) AS total FROM products")) {

            if (rs.next() && rs.getInt("total") == 0) {

                try (Statement stmt = conn.createStatement()) {

                    stmt.execute(
                            "INSERT INTO products(productName,productImage,sku,category,description,sellingPrice,isActive) VALUES "
                                    + "('Salmon Roll',NULL,'SKU-SR-001','Sushi Roll','Sushi roll isi salmon segar',32000,1),"
                                    + "('California Roll',NULL,'SKU-CR-001','Sushi Roll','Sushi roll dengan mentimun, kani, dan telur ikan',28000,1),"
                                    + "('Tuna Roll',NULL,'SKU-TR-001','Sushi Roll','Sushi roll isi tuna segar',30000,1),"
                                    + "('Ebi Tempura Roll',NULL,'SKU-ETR-001','Sushi Roll','Sushi roll udang tempura crispy',33000,1),"
                                    + "('Spicy Tuna Roll',NULL,'SKU-STR-001','Sushi Roll','Sushi roll tuna pedas dengan mayones jepang',34000,1),"
                                    + "('Dragon Roll',NULL,'SKU-DR-001','Sushi Roll','Roll salmon panggang dengan alpukat dan saus unagi',42000,1),"
                                    + "('Philadelphia Roll',NULL,'SKU-PR-001','Sushi Roll','Roll salmon dengan cream cheese dan mentimun',35000,1),"
                                    + "('Salmon Nigiri',NULL,'SKU-SN-001','Nigiri','Nasi dengan topping salmon segar',18000,1),"
                                    + "('Tuna Nigiri',NULL,'SKU-TN-001','Nigiri','Nasi dengan topping tuna segar',18000,1),"
                                    + "('Ebi Nigiri',NULL,'SKU-EN-001','Nigiri','Nasi dengan topping udang',16000,1),"
                                    + "('Tamago Nigiri',NULL,'SKU-TMN-001','Nigiri','Nasi dengan topping telur dadar manis',12000,1),"
                                    + "('Chicken Katsu Roll',NULL,'SKU-CKR-001','Sushi Roll','Roll ayam katsu dengan telur dan saus mayones',27000,1),"
                                    + "('Rainbow Roll',NULL,'SKU-RR-001','Sushi Roll','California roll dengan topping aneka sashimi segar',45000,1),"
                                    + "('Volcano Roll',NULL,'SKU-VR-001','Sushi Roll','Roll panggang topping seafood pedas dan mayones',40000,1),"
                                    + "('Mentai Roll',NULL,'SKU-MR-001','Sushi Roll','Roll dipanggang dengan saus mentai creamy',38000,1),"
                                    + "('Unagi Roll',NULL,'SKU-UR-001','Sushi Roll','Roll belut panggang dengan saus unagi manis',40000,1),"
                                    + "('Aburi Salmon Roll',NULL,'SKU-ASR-001','Sushi Roll','Roll salmon yang dibakar sekilas (aburi) di atasnya',38000,1),"
                                    + "('Crunchy Roll',NULL,'SKU-CRR-001','Sushi Roll','Roll dengan tambahan tempura crunch renyah',30000,1),"
                                    + "('Mango Roll',NULL,'SKU-MGR-001','Sushi Roll','Roll segar dengan mangga dan udang',33000,1),"
                                    + "('Avocado Roll',NULL,'SKU-AVR-001','Sushi Roll','Roll vegetarian dengan isian alpukat segar',24000,1),"
                                    + "('Inari Sushi',NULL,'SKU-IS-001','Sushi Lainnya','Nasi sushi dibungkus tahu manis khas Jepang',15000,1),"
                                    + "('Salmon Sashimi',NULL,'SKU-SS-001','Sashimi','Irisan salmon segar tanpa nasi, 5 potong',35000,1);"
                    );

                }

            }

        }

    }

    /**
     * Menambahkan user default (owner/staff) dengan password ter-hash,
     * hanya kalau username tersebut belum ada di tabel users.
     */
    private static void seedUserIfMissing(Connection conn, String username, String plainPassword, String role)
            throws Exception {

        String checkSql = "SELECT COUNT(*) AS total FROM users WHERE username = ?";

        try (PreparedStatement check = conn.prepareStatement(checkSql)) {

            check.setString(1, username);

            try (ResultSet rs = check.executeQuery()) {

                if (rs.next() && rs.getInt("total") > 0) {
                    return;
                }

            }

        }

        String insertSql = "INSERT INTO users(username, password, role) VALUES (?, ?, ?)";

        try (PreparedStatement insert = conn.prepareStatement(insertSql)) {

            insert.setString(1, username);
            insert.setString(2, PasswordUtil.hash(plainPassword));
            insert.setString(3, role);

            insert.executeUpdate();

        }

    }

    /**
     * Menambahkan kolom baru ke tabel yang sudah ada kalau kolom tersebut
     * belum ada, supaya database lama (dibuat sebelum field ini
     * ditambahkan) tetap bisa dipakai tanpa perlu dihapus/reset manual.
     */
    private static void addColumnIfMissing(Connection conn, String table, String column, String type)
            throws Exception {

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) {

            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    return;
                }
            }

        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
        }

    }

    /**
     * Untuk database lama (dibuat sebelum hashing diterapkan): kolom
     * password-nya masih berisi plaintext. Method ini mendeteksi baris
     * seperti itu dan meng-hash-kannya di tempat, supaya user tetap bisa
     * login dengan password yang sama seperti sebelumnya tanpa perlu reset.
     */
    private static void migrateLegacyPlaintextPasswords(Connection conn) throws Exception {

        try (Statement select = conn.createStatement();
             ResultSet rs = select.executeQuery("SELECT userId, password FROM users")) {

            String updateSql = "UPDATE users SET password = ? WHERE userId = ?";

            try (PreparedStatement update = conn.prepareStatement(updateSql)) {

                while (rs.next()) {

                    String currentPassword = rs.getString("password");

                    if (currentPassword != null && !PasswordUtil.isHashed(currentPassword)) {

                        update.setString(1, PasswordUtil.hash(currentPassword));
                        update.setInt(2, rs.getInt("userId"));
                        update.executeUpdate();

                    }

                }

            }

        }

    }

}
