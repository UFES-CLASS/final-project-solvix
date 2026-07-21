package com.stockin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.stockin.config.DatabaseConnection;
import com.stockin.model.Material;

public class MaterialDAO {

    public boolean addMaterial(Material material) {

        String sql = "INSERT INTO materials(materialName, category, unit, stock, minimumStock) "
                + "VALUES(?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, material.getMaterialName());
            ps.setString(2, material.getCategory());
            ps.setString(3, material.getUnit());
            ps.setInt(4, material.getStock());
            ps.setInt(5, material.getMinimumStock());

            int rows = ps.executeUpdate();

            if (rows > 0) {

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        material.setMaterialId(keys.getInt(1));
                    }
                }

                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean updateMaterial(Material material) {

        String sql = "UPDATE materials SET materialName=?, category=?, unit=?, "
                + "stock=?, minimumStock=? WHERE materialId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, material.getMaterialName());
            ps.setString(2, material.getCategory());
            ps.setString(3, material.getUnit());
            ps.setInt(4, material.getStock());
            ps.setInt(5, material.getMinimumStock());
            ps.setInt(6, material.getMaterialId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    /**
     * Hanya mengubah field minimumStock (fitur Owner: setMinimumStock).
     */
    public boolean setMinimumStock(int materialId, int minimumStock) {

        String sql = "UPDATE materials SET minimumStock=? WHERE materialId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, minimumStock);
            ps.setInt(2, materialId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean updateStock(int materialId, int delta) {

        String sql = "UPDATE materials SET stock = stock + ? WHERE materialId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, delta);
            ps.setInt(2, materialId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean deleteMaterial(int materialId) {

        String sql = "DELETE FROM materials WHERE materialId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, materialId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public Material getMaterialById(int materialId) {

        String sql = "SELECT * FROM materials WHERE materialId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, materialId);

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

    public List<Material> getAllMaterials() {

        List<Material> list = new ArrayList<>();

        String sql = "SELECT * FROM materials ORDER BY materialName";

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

    public List<Material> getLowStockMaterials() {

        List<Material> list = new ArrayList<>();

        String sql = "SELECT * FROM materials WHERE stock <= minimumStock ORDER BY materialName";

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

    public int countAll() {

        String sql = "SELECT COUNT(*) AS total FROM materials";

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

    /**
     * Mengecek apakah sudah ada material lain dengan nama yang sama
     * (tidak peka huruf besar/kecil). excludeMaterialId dipakai saat mode
     * update, supaya material yang sedang diedit tidak dianggap "duplikat"
     * terhadap dirinya sendiri.
     */
    public boolean existsByName(String materialName, Integer excludeMaterialId) {

        String sql = "SELECT COUNT(*) AS total FROM materials "
                + "WHERE LOWER(materialName) = LOWER(?) AND materialId <> ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, materialName);
            ps.setInt(2, excludeMaterialId == null ? -1 : excludeMaterialId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public static boolean isLowStock(Material material) {
        return material.getStock() <= material.getMinimumStock();
    }

    private Material mapRow(ResultSet rs) throws Exception {

        Material material = new Material();

        material.setMaterialId(rs.getInt("materialId"));
        material.setMaterialName(rs.getString("materialName"));
        material.setCategory(rs.getString("category"));
        material.setUnit(rs.getString("unit"));
        material.setStock(rs.getInt("stock"));
        material.setMinimumStock(rs.getInt("minimumStock"));

        return material;

    }

}
