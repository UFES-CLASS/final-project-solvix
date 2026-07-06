package com.stockin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.stockin.config.DatabaseConnection;
import com.stockin.model.Production;

public class ProductionDAO {

    public boolean addProduction(Production production) {

        String sql = "INSERT INTO production"
                + "(productId, productionDate, expiredDate, quantityProduced, quantitySold, sellingPrice, note) "
                + "VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, production.getProductId());
            ps.setString(2, production.getProductionDate());
            ps.setString(3, production.getExpiredDate());
            ps.setInt(4, production.getQuantityProduced());
            ps.setInt(5, production.getQuantitySold());
            ps.setDouble(6, production.getSellingPrice());
            ps.setString(7, production.getNote());

            int rows = ps.executeUpdate();

            if (rows > 0) {

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        production.setProductionId(keys.getInt(1));
                    }
                }

                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean updateProduction(Production production) {

        String sql = "UPDATE production SET productId=?, productionDate=?, expiredDate=?, "
                + "quantityProduced=?, quantitySold=?, sellingPrice=?, note=? WHERE productionId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, production.getProductId());
            ps.setString(2, production.getProductionDate());
            ps.setString(3, production.getExpiredDate());
            ps.setInt(4, production.getQuantityProduced());
            ps.setInt(5, production.getQuantitySold());
            ps.setDouble(6, production.getSellingPrice());
            ps.setString(7, production.getNote());
            ps.setInt(8, production.getProductionId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean deleteProduction(int productionId) {

        String sql = "DELETE FROM production WHERE productionId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productionId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public List<Production> getAllProduction() {

        List<Production> list = new ArrayList<>();

        String sql = "SELECT p.*, pr.productName FROM production p "
                + "JOIN products pr ON p.productId = pr.productId "
                + "ORDER BY p.productionDate DESC, p.productionId DESC";

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
     * Mengambil data production dalam rentang tanggal tertentu (inklusif),
     * digunakan oleh halaman Financial Report. Jika startDate/endDate
     * bernilai null, filter tanggal tidak diterapkan.
     */
    public List<Production> getProductionBetween(String startDate, String endDate) {

        List<Production> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT p.*, pr.productName FROM production p "
                        + "JOIN products pr ON p.productId = pr.productId WHERE 1=1 ");

        if (startDate != null) {
            sql.append("AND p.productionDate >= ? ");
        }

        if (endDate != null) {
            sql.append("AND p.productionDate <= ? ");
        }

        sql.append("ORDER BY p.productionDate ASC, p.productionId ASC");

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
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;

    }

    public int getTotalProduced() {
        return getTotalProduced(null, null);
    }

    public int getTotalProduced(String startDate, String endDate) {

        int total = 0;

        for (Production p : getProductionBetween(startDate, endDate)) {
            total += p.getQuantityProduced();
        }

        return total;

    }

    public int getTotalSold() {
        return getTotalSold(null, null);
    }

    public int getTotalSold(String startDate, String endDate) {

        int total = 0;

        for (Production p : getProductionBetween(startDate, endDate)) {
            total += p.getQuantitySold();
        }

        return total;

    }

    public double getRevenue() {
        return getRevenue(null, null);
    }

    public double getRevenue(String startDate, String endDate) {

        double total = 0;

        for (Production p : getProductionBetween(startDate, endDate)) {
            total += p.getRevenue();
        }

        return total;

    }

    public Map<String, Double> getRevenueByProduct(String startDate, String endDate) {

        Map<String, Double> result = new LinkedHashMap<>();

        for (Production p : getProductionBetween(startDate, endDate)) {
            result.merge(p.getProductName(), p.getRevenue(), Double::sum);
        }

        return result;

    }

    public Map<String, Double> getRevenueByDate(String startDate, String endDate) {

        Map<String, Double> result = new LinkedHashMap<>();

        for (Production p : getProductionBetween(startDate, endDate)) {
            result.merge(p.getProductionDate(), p.getRevenue(), Double::sum);
        }

        return result;

    }

    private Production mapRow(ResultSet rs) throws Exception {

        Production production = new Production();

        production.setProductionId(rs.getInt("productionId"));
        production.setProductId(rs.getInt("productId"));
        production.setProductionDate(rs.getString("productionDate"));
        production.setExpiredDate(rs.getString("expiredDate"));
        production.setQuantityProduced(rs.getInt("quantityProduced"));
        production.setQuantitySold(rs.getInt("quantitySold"));
        production.setSellingPrice(rs.getDouble("sellingPrice"));
        production.setNote(rs.getString("note"));
        production.setProductName(rs.getString("productName"));

        return production;

    }

}
