package com.stockin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.stockin.config.DatabaseConnection;
import com.stockin.model.MaterialUsageItem;

/**
 * DAO untuk tabel product_materials, yaitu Bill of Materials (BOM) tiap
 * produk: daftar bahan beserta jumlah yang dibutuhkan untuk membuat SATU
 * unit produk tersebut. Dengan adanya BOM ini, halaman Production tidak
 * perlu lagi meminta user memilih bahan satu per satu secara manual -
 * begitu produk dan jumlah produksi dipilih, kebutuhan bahan bisa
 * dihitung otomatis (quantityRequired x quantityProduced).
 */
public class ProductMaterialDAO {

    /**
     * Mengganti seluruh BOM sebuah produk dengan daftar baru. Dipanggil
     * setiap kali produk disimpan (baik tambah baru maupun update), lebih
     * sederhana dan aman daripada mencoba diff baris lama vs baru.
     */
    public boolean replaceBom(int productId, List<MaterialUsageItem> items) {

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement delete = conn.prepareStatement(
                    "DELETE FROM product_materials WHERE productId = ?")) {
                delete.setInt(1, productId);
                delete.executeUpdate();
            }

            if (items == null || items.isEmpty()) {
                return true;
            }

            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO product_materials(productId, materialId, quantityRequired) VALUES (?,?,?)")) {

                for (MaterialUsageItem item : items) {
                    insert.setInt(1, productId);
                    insert.setInt(2, item.getMaterialId());
                    insert.setInt(3, item.getQuantityUsed());
                    insert.addBatch();
                }

                insert.executeBatch();

            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    /**
     * Mengambil BOM (bahan + jumlah kebutuhan per unit) sebuah produk,
     * lengkap dengan nama dan satuan bahan (join ke tabel materials).
     */
    public List<MaterialUsageItem> getBomByProduct(int productId) {

        List<MaterialUsageItem> list = new ArrayList<>();

        String sql = "SELECT pm.materialId, m.materialName, m.unit, pm.quantityRequired "
                + "FROM product_materials pm "
                + "JOIN materials m ON pm.materialId = m.materialId "
                + "WHERE pm.productId = ? "
                + "ORDER BY pm.productMaterialId";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(new MaterialUsageItem(
                            rs.getInt("materialId"),
                            rs.getString("materialName"),
                            rs.getString("unit"),
                            rs.getInt("quantityRequired")));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;

    }

    public boolean deleteByProduct(int productId) {

        String sql = "DELETE FROM product_materials WHERE productId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

}
