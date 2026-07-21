package com.stockin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.stockin.config.DatabaseConnection;
import com.stockin.model.IncomingMaterial;

public class IncomingMaterialDAO {

    private final MaterialDAO materialDAO = new MaterialDAO();


    public boolean addIncomingMaterial(IncomingMaterial incoming) {

        String sql = "INSERT INTO incoming_materials"
                + "(materialId, incomingDate, quantity, unitPrice, totalPrice, supplier, note) "
                + "VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, incoming.getMaterialId());
                ps.setString(2, incoming.getIncomingDate());
                ps.setInt(3, incoming.getQuantity());
                ps.setDouble(4, incoming.getUnitPrice());
                ps.setDouble(5, incoming.getTotalPrice());
                ps.setString(6, incoming.getSupplier());
                ps.setString(7, incoming.getNote());

                int rows = ps.executeUpdate();

                if (rows > 0) {

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            incoming.setIncomingId(keys.getInt(1));
                        }
                    }

                    materialDAO.updateStock(incoming.getMaterialId(), incoming.getQuantity());

                    return true;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean updateIncomingMaterial(IncomingMaterial incoming) {

        IncomingMaterial old = getIncomingMaterialById(incoming.getIncomingId());

        if (old == null) {
            return false;
        }

        String sql = "UPDATE incoming_materials SET materialId=?, incomingDate=?, quantity=?, "
                + "unitPrice=?, totalPrice=?, supplier=?, note=? WHERE incomingId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, incoming.getMaterialId());
            ps.setString(2, incoming.getIncomingDate());
            ps.setInt(3, incoming.getQuantity());
            ps.setDouble(4, incoming.getUnitPrice());
            ps.setDouble(5, incoming.getTotalPrice());
            ps.setString(6, incoming.getSupplier());
            ps.setString(7, incoming.getNote());
            ps.setInt(8, incoming.getIncomingId());

            int rows = ps.executeUpdate();

            if (rows > 0) {

                // Batalkan efek stok lama, lalu terapkan efek stok baru
                if (old.getMaterialId() == incoming.getMaterialId()) {

                    int delta = incoming.getQuantity() - old.getQuantity();
                    materialDAO.updateStock(incoming.getMaterialId(), delta);

                } else {

                    materialDAO.updateStock(old.getMaterialId(), -old.getQuantity());
                    materialDAO.updateStock(incoming.getMaterialId(), incoming.getQuantity());

                }

                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    /**
     * Menghapus data bahan masuk sekaligus mengembalikan (mengurangi)
     * stok material yang sebelumnya ditambahkan.
     */
    public boolean deleteIncomingMaterial(int incomingId) {

        IncomingMaterial old = getIncomingMaterialById(incomingId);

        if (old == null) {
            return false;
        }

        String sql = "DELETE FROM incoming_materials WHERE incomingId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, incomingId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                materialDAO.updateStock(old.getMaterialId(), -old.getQuantity());
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public IncomingMaterial getIncomingMaterialById(int incomingId) {

        String sql = "SELECT im.*, m.materialName, m.unit FROM incoming_materials im "
                + "JOIN materials m ON im.materialId = m.materialId "
                + "WHERE im.incomingId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, incomingId);

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

    public List<IncomingMaterial> getAllIncomingMaterials() {

        List<IncomingMaterial> list = new ArrayList<>();

        String sql = "SELECT im.*, m.materialName, m.unit FROM incoming_materials im "
                + "JOIN materials m ON im.materialId = m.materialId "
                + "ORDER BY im.incomingDate DESC, im.incomingId DESC";

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

    public int countToday(String todayIso) {

        String sql = "SELECT COUNT(*) AS total FROM incoming_materials WHERE incomingDate = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, todayIso);

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
     * Harga satuan terbaru (paling akhir dicatat) untuk sebuah material.
     * Dipakai Dashboard untuk menghitung "Stock Asset Value".
     * Mengembalikan 0 kalau material tersebut belum pernah punya
     * catatan bahan masuk sama sekali.
     */
    public double getLatestUnitPrice(int materialId) {

        String sql = "SELECT unitPrice FROM incoming_materials WHERE materialId = ? "
                + "ORDER BY incomingDate DESC, incomingId DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, materialId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("unitPrice");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;

    }

    /**
     * Total pengeluaran (totalPrice) untuk seluruh pembelian bahan masuk
     * dalam rentang tanggal tertentu (inklusif). Dipakai oleh Financial
     * Report untuk menampilkan Expense di samping Revenue, supaya laporan
     * keuangan tidak hanya menampilkan pemasukan saja.
     */
    public double getExpenseBetween(String startDate, String endDate) {

        StringBuilder sql = new StringBuilder(
                "SELECT COALESCE(SUM(totalPrice),0) AS total FROM incoming_materials WHERE 1=1 ");

        if (startDate != null) {
            sql.append("AND incomingDate >= ? ");
        }

        if (endDate != null) {
            sql.append("AND incomingDate <= ? ");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;

            if (startDate != null) {
                ps.setString(idx++, startDate);
            }

            if (endDate != null) {
                ps.setString(idx++, endDate);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;

    }

    /**
     * Total kuantitas semua bahan masuk pada satu tanggal tertentu.
     * Dipakai untuk menyusun grafik "Material Incoming vs Outgoing".
     */
    public int getTotalQuantityByDate(String dateIso) {

        String sql = "SELECT COALESCE(SUM(quantity),0) AS total FROM incoming_materials WHERE incomingDate = ?";

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

    private IncomingMaterial mapRow(ResultSet rs) throws Exception {

        IncomingMaterial incoming = new IncomingMaterial();

        incoming.setIncomingId(rs.getInt("incomingId"));
        incoming.setMaterialId(rs.getInt("materialId"));
        incoming.setIncomingDate(rs.getString("incomingDate"));
        incoming.setQuantity(rs.getInt("quantity"));
        incoming.setUnitPrice(rs.getDouble("unitPrice"));
        incoming.setTotalPrice(rs.getDouble("totalPrice"));
        incoming.setSupplier(rs.getString("supplier"));
        incoming.setNote(rs.getString("note"));
        incoming.setMaterialName(rs.getString("materialName"));
        incoming.setUnit(rs.getString("unit"));

        return incoming;

    }

}
