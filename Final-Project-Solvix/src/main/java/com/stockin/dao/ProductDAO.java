package com.stockin.dao;

import com.stockin.config.DatabaseConnection;
import com.stockin.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class ProductDAO {

    public boolean addProduct(Product product) {

        String sql = "INSERT INTO products(productName, productImage, sku, category, description, sellingPrice, isActive) "
                + "VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, product.getProductName());
            ps.setString(2, product.getProductImage());
            ps.setString(3, product.getSku());
            ps.setString(4, product.getCategory());
            ps.setString(5, product.getDescription());
            ps.setDouble(6, product.getSellingPrice());
            ps.setInt(7, product.isActive() ? 1 : 0);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        product.setProductId(keys.getInt(1));
                    }
                }

                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean updateProduct(Product product) {

        String sql = "UPDATE products SET productName=?, productImage=?, sku=?, category=?, description=?, "
                + "sellingPrice=?, isActive=? WHERE productId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getProductName());
            ps.setString(2, product.getProductImage());
            ps.setString(3, product.getSku());
            ps.setString(4, product.getCategory());
            ps.setString(5, product.getDescription());
            ps.setDouble(6, product.getSellingPrice());
            ps.setInt(7, product.isActive() ? 1 : 0);
            ps.setInt(8, product.getProductId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean deleteProduct(int productId) {

        String sql = "DELETE FROM products WHERE productId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public Product getProductById(int productId) {

        String sql = "SELECT * FROM products WHERE productId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    public List<Product> getAllProducts() {

        List<Product> list = new ArrayList<>();

        String sql = "SELECT * FROM products ORDER BY productName";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;

    }

    /**
     * Daftar kategori unik yang sudah pernah dipakai, buat mengisi pilihan
     * di ComboBox kategori (baik di form maupun filter tabel).
     */
    public List<String> getAllCategories() {

        TreeSet<String> categories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        String sql = "SELECT DISTINCT category FROM products WHERE category IS NOT NULL AND category != ''";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(rs.getString("category"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>(categories);

    }

    public int countAll() {

        String sql = "SELECT COUNT(*) AS total FROM products";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;

    }

    private Product mapRow(ResultSet rs) throws Exception {

        Product product = new Product();

        product.setProductId(rs.getInt("productId"));
        product.setProductName(rs.getString("productName"));
        product.setProductImage(rs.getString("productImage"));
        product.setSku(rs.getString("sku"));
        product.setCategory(rs.getString("category"));
        product.setDescription(rs.getString("description"));
        product.setSellingPrice(rs.getDouble("sellingPrice"));
        product.setActive(rs.getInt("isActive") == 1);

        return product;

    }

}
