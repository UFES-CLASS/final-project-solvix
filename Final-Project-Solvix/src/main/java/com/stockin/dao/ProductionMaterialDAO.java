package com.stockin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.stockin.config.DatabaseConnection;
import com.stockin.model.MaterialUsageItem;

/**
 * DAO untuk tabel junction production_materials, yang menyimpan bahan-bahan
 * apa saja (beserta jumlahnya) yang dipakai pada satu batch produksi.
 * Tanpa tabel ini, riwayat pemakaian bahan per batch produksi tidak bisa
 * dilihat lagi setelah data production disimpan.
 */
public class ProductionMaterialDAO {

    /**
     * Menyimpan satu baris pemakaian bahan untuk sebuah production batch.
     */
    public boolean insertUsage(int productionId, int materialId, int quantityUsed) {

        String sql = "INSERT INTO production_materials(productionId, materialId, quantityUsed) "
                + "VALUES(?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productionId);
            ps.setInt(2, materialId);
            ps.setInt(3, quantityUsed);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    /**
     * Mengambil semua bahan yang tercatat dipakai pada sebuah production batch,
     * lengkap dengan nama dan satuan bahan (join ke tabel materials).
     */
    public List<MaterialUsageItem> getUsageByProduction(int productionId) {

        List<MaterialUsageItem> list = new ArrayList<>();

        String sql = "SELECT pm.materialId, m.materialName, m.unit, pm.quantityUsed "
                + "FROM production_materials pm "
                + "JOIN materials m ON pm.materialId = m.materialId "
                + "WHERE pm.productionId = ? "
                + "ORDER BY pm.productionMaterialId";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productionId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(new MaterialUsageItem(
                            rs.getInt("materialId"),
                            rs.getString("materialName"),
                            rs.getString("unit"),
                            rs.getInt("quantityUsed")));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;

    }

    /**
     * Menghapus seluruh catatan pemakaian bahan untuk sebuah production batch.
     * Dipanggil saat production dihapus (sebelum stok dikembalikan) meskipun
     * ON DELETE CASCADE pada FK sudah menangani ini, agar tetap eksplisit dan
     * aman walau PRAGMA foreign_keys nonaktif.
     */
    /**
     * Total kuantitas bahan yang terpakai (outgoing) pada satu tanggal
     * produksi tertentu. Dipakai oleh grafik
     * "Material Incoming vs Outgoing" di Dashboard.
     */
    public int getTotalQuantityUsedByDate(String dateIso) {

        String sql = "SELECT COALESCE(SUM(pm.quantityUsed),0) AS total "
                + "FROM production_materials pm "
                + "JOIN production p ON pm.productionId = p.productionId "
                + "WHERE p.productionDate = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dateIso);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;

    }

    /**
     * Mengambil bahan-bahan yang paling banyak terpakai pada rentang
     * tanggal tertentu (inklusif), diurutkan dari yang paling banyak,
     * dibatasi sebanyak {@code limit} baris. Dipakai oleh grafik
     * "Top Consumed Materials" di Dashboard.
     */
    public Map<String, Integer> getTopConsumedMaterials(String startDate, String endDate, int limit) {

        Map<String, Integer> result = new LinkedHashMap<>();

        String sql = "SELECT m.materialName AS materialName, SUM(pm.quantityUsed) AS total "
                + "FROM production_materials pm "
                + "JOIN production p ON pm.productionId = p.productionId "
                + "JOIN materials m ON pm.materialId = m.materialId "
                + "WHERE p.productionDate BETWEEN ? AND ? "
                + "GROUP BY m.materialName "
                + "ORDER BY total DESC "
                + "LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, startDate);
            ps.setString(2, endDate);
            ps.setInt(3, limit);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    result.put(rs.getString("materialName"), rs.getInt("total"));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }

    public boolean deleteByProduction(int productionId) {

        String sql = "DELETE FROM production_materials WHERE productionId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productionId);

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

}
