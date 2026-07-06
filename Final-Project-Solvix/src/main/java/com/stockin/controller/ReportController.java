package com.stockin.controller;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.stockin.dao.ProductionDAO;
import com.stockin.model.Production;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


public class ReportController {

    @FXML
    private DatePicker dateFrom;

    @FXML
    private DatePicker dateTo;

    @FXML
    private Label lblTotalProduced;

    @FXML
    private Label lblTotalSold;

    @FXML
    private Label lblRevenue;

    @FXML
    private BarChart<String, Number> chartByProduct;

    @FXML
    private LineChart<String, Number> chartByDate;

    @FXML
    private TableView<ProductSummary> tableSummary;

    @FXML
    private TableColumn<ProductSummary, String> colProduct;

    @FXML
    private TableColumn<ProductSummary, Integer> colProduced;

    @FXML
    private TableColumn<ProductSummary, Integer> colSold;

    @FXML
    private TableColumn<ProductSummary, Double> colRevenue;

    private final ProductionDAO productionDAO = new ProductionDAO();

    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @FXML
    public void initialize() {

        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colProduced.setCellValueFactory(new PropertyValueFactory<>("totalProduced"));
        colSold.setCellValueFactory(new PropertyValueFactory<>("totalSold"));
        colRevenue.setCellValueFactory(new PropertyValueFactory<>("revenue"));

        loadReport(null, null);

    }

    @FXML
    private void applyFilter() {

        String from = dateFrom.getValue() != null ? dateFrom.getValue().toString() : null;
        String to = dateTo.getValue() != null ? dateTo.getValue().toString() : null;

        loadReport(from, to);

    }

    @FXML
    private void resetFilter() {

        dateFrom.setValue(null);
        dateTo.setValue(null);

        loadReport(null, null);

    }

    private void loadReport(String from, String to) {

        List<Production> productions = productionDAO.getProductionBetween(from, to);

        int totalProduced = 0;
        int totalSold = 0;
        double totalRevenue = 0;

        Map<String, ProductSummary> byProduct = new LinkedHashMap<>();
        Map<String, Double> byDate = new LinkedHashMap<>();

        for (Production p : productions) {

            totalProduced += p.getQuantityProduced();
            totalSold += p.getQuantitySold();
            totalRevenue += p.getRevenue();

            ProductSummary summary = byProduct.computeIfAbsent(
                    p.getProductName(), ProductSummary::new);

            summary.addProduced(p.getQuantityProduced());
            summary.addSold(p.getQuantitySold());
            summary.addRevenue(p.getRevenue());

            byDate.merge(p.getProductionDate(), p.getRevenue(), Double::sum);

        }

        lblTotalProduced.setText(String.valueOf(totalProduced));
        lblTotalSold.setText(String.valueOf(totalSold));
        lblRevenue.setText(currency.format(totalRevenue));

        tableSummary.setItems(FXCollections.observableArrayList(byProduct.values()));

        drawBarChart(byProduct);
        drawLineChart(byDate);

    }

    private void drawBarChart(Map<String, ProductSummary> byProduct) {

        chartByProduct.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pendapatan");

        for (ProductSummary summary : byProduct.values()) {
            series.getData().add(new XYChart.Data<>(summary.getProductName(), summary.getRevenue()));
        }

        chartByProduct.getData().add(series);

    }

    private void drawLineChart(Map<String, Double> byDate) {

        chartByDate.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pendapatan");

        for (Map.Entry<String, Double> entry : byDate.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        chartByDate.getData().add(series);

    }

    public static class ProductSummary {

        private final String productName;
        private int totalProduced;
        private int totalSold;
        private double revenue;

        public ProductSummary(String productName) {
            this.productName = productName;
        }

        public void addProduced(int qty) {
            totalProduced += qty;
        }

        public void addSold(int qty) {
            totalSold += qty;
        }

        public void addRevenue(double amount) {
            revenue += amount;
        }

        public String getProductName() {
            return productName;
        }

        public int getTotalProduced() {
            return totalProduced;
        }

        public int getTotalSold() {
            return totalSold;
        }

        public double getRevenue() {
            return revenue;
        }

    }

}
