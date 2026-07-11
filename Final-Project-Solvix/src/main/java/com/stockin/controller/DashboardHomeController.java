package com.stockin.controller;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.stockin.dao.ActivityLogDAO;
import com.stockin.dao.IncomingMaterialDAO;
import com.stockin.dao.MaterialDAO;
import com.stockin.dao.ProductDAO;
import com.stockin.dao.ProductionDAO;
import com.stockin.dao.ProductionMaterialDAO;
import com.stockin.model.ActivityLog;
import com.stockin.model.Material;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DashboardHomeController {

    // ===== STAT CARDS =====
    @FXML
    private Label lblMaterial;

    @FXML
    private Label lblProduct;

    @FXML
    private Label lblLowStock;

    @FXML
    private Label lblIncoming;

    @FXML
    private Label lblActiveProduction;

    @FXML
    private Label lblStockValue;

    // ===== DATE RANGE =====
    @FXML
    private ToggleButton btnRangeToday;

    @FXML
    private ToggleButton btnRangeWeek;

    @FXML
    private ToggleButton btnRangeMonth;

    private final ToggleGroup rangeGroup = new ToggleGroup();

    // ===== CHARTS =====
    @FXML
    private AreaChart<String, Number> flowChart;

    @FXML
    private BarChart<String, Number> consumedChart;

    @FXML
    private Label lblConsumedEmpty;

    // ===== NOTIFICATIONS & ACTIVITY =====
    @FXML
    private VBox notificationContainer;

    @FXML
    private VBox activityContainer;

    private final MaterialDAO materialDAO = new MaterialDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final IncomingMaterialDAO incomingMaterialDAO = new IncomingMaterialDAO();
    private final ProductionDAO productionDAO = new ProductionDAO();
    private final ProductionMaterialDAO productionMaterialDAO = new ProductionMaterialDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    // Dipasang oleh DashboardController supaya tombol "Restock Now" bisa
    // berpindah halaman ke Incoming Material.
    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {

        loadStatCards();
        setupDateRange();
        buildFlowChart();
        buildNotificationCards();
        buildActivityLog();

    }

    // =========================================================
    // STAT CARDS
    // =========================================================

    private void loadStatCards() {

        lblMaterial.setText(String.valueOf(materialDAO.countAll()));
        lblProduct.setText(String.valueOf(productDAO.countAll()));

        List<Material> lowStock = materialDAO.getLowStockMaterials();
        lblLowStock.setText(String.valueOf(lowStock.size()));

        lblIncoming.setText(String.valueOf(incomingMaterialDAO.countToday(LocalDate.now().toString())));

        lblActiveProduction.setText(String.valueOf(productionDAO.countActiveBatches()));

        lblStockValue.setText(formatRupiahShort(computeStockAssetValue()));

    }

    private double computeStockAssetValue() {

        double total = 0;

        for (Material material : materialDAO.getAllMaterials()) {

            double latestPrice = incomingMaterialDAO.getLatestUnitPrice(material.getMaterialId());
            total += latestPrice * material.getStock();

        }

        return total;

    }

    /**
     * Memformat angka rupiah menjadi bentuk singkat, contoh:
     * 15.200.000 -> "Rp 15.2jt", 850.000 -> "Rp 850rb", 500 -> "Rp 500".
     */
    private String formatRupiahShort(double value) {

        double abs = Math.abs(value);
        String sign = value < 0 ? "-" : "";

        if (abs >= 1_000_000_000d) {
            return "Rp " + sign + String.format(Locale.US, "%.1fM", abs / 1_000_000_000d);
        } else if (abs >= 1_000_000d) {
            return "Rp " + sign + String.format(Locale.US, "%.1fjt", abs / 1_000_000d);
        } else if (abs >= 1_000d) {
            return "Rp " + sign + String.format(Locale.US, "%.0frb", abs / 1_000d);
        } else {
            return "Rp " + sign + String.format(Locale.US, "%.0f", abs);
        }

    }

    // =========================================================
    // DATE RANGE (mempengaruhi grafik "Top Consumed Materials")
    // =========================================================

    private void setupDateRange() {

        btnRangeToday.setToggleGroup(rangeGroup);
        btnRangeWeek.setToggleGroup(rangeGroup);
        btnRangeMonth.setToggleGroup(rangeGroup);

        rangeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {

            if (newToggle == null) {
                // Jangan biarkan tidak ada pilihan yang aktif sama sekali.
                oldToggle.setSelected(true);
                return;
            }

            refreshConsumedChart();

        });

        btnRangeToday.setSelected(true);

        refreshConsumedChart();

    }

    // =========================================================
    // CHART: Material Incoming vs Outgoing (Last 7 Days)
    // =========================================================

    private void buildFlowChart() {

        flowChart.getData().clear();

        XYChart.Series<String, Number> incomingSeries = new XYChart.Series<>();
        incomingSeries.setName("Incoming");

        XYChart.Series<String, Number> outgoingSeries = new XYChart.Series<>();
        outgoingSeries.setName("Outgoing");

        LocalDate start = LocalDate.now().minusDays(6);

        for (int i = 0; i < 7; i++) {

            LocalDate date = start.plusDays(i);
            String label = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            int incomingTotal = incomingMaterialDAO.getTotalQuantityByDate(date.toString());
            int outgoingTotal = productionMaterialDAO.getTotalQuantityUsedByDate(date.toString());

            incomingSeries.getData().add(new XYChart.Data<>(label, incomingTotal));
            outgoingSeries.getData().add(new XYChart.Data<>(label, outgoingTotal));

        }

        flowChart.getData().addAll(incomingSeries, outgoingSeries);

    }

    // =========================================================
    // CHART: Top Consumed Materials
    // =========================================================

    private void refreshConsumedChart() {

        LocalDate today = LocalDate.now();
        String startDate;
        String endDate = today.toString();

        if (btnRangeToday.isSelected()) {
            startDate = today.toString();
        } else if (btnRangeMonth.isSelected()) {
            startDate = today.withDayOfMonth(1).toString();
        } else {
            startDate = today.minusDays(6).toString();
        }

        Map<String, Integer> topConsumed = productionMaterialDAO.getTopConsumedMaterials(startDate, endDate, 6);

        consumedChart.getData().clear();

        if (topConsumed.isEmpty()) {
            lblConsumedEmpty.setVisible(true);
            lblConsumedEmpty.setManaged(true);
            consumedChart.setVisible(false);
            consumedChart.setManaged(false);
            return;
        }

        lblConsumedEmpty.setVisible(false);
        lblConsumedEmpty.setManaged(false);
        consumedChart.setVisible(true);
        consumedChart.setManaged(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (Map.Entry<String, Integer> entry : topConsumed.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        consumedChart.getData().add(series);

    }

    // =========================================================
    // NOTIFICATIONS / LOW STOCK ITEMS
    // =========================================================

    private void buildNotificationCards() {

        notificationContainer.getChildren().clear();

        List<Material> lowStock = materialDAO.getLowStockMaterials();

        if (lowStock.isEmpty()) {

            Label empty = new Label("All materials are above their minimum stock level.");
            empty.getStyleClass().add("empty-state-label");
            notificationContainer.getChildren().add(empty);
            return;

        }

        for (Material material : lowStock) {
            notificationContainer.getChildren().add(buildNotificationCard(material));
        }

    }

    private VBox buildNotificationCard(Material material) {

        int deficit = material.getStock() - material.getMinimumStock();
        boolean anomaly = material.getStock() < 0;

        Label name = new Label(material.getMaterialName());
        name.getStyleClass().add("notification-item-title");

        Region nameSpacer = new Region();
        HBox.setHgrow(nameSpacer, Priority.ALWAYS);

        Label deficitBadge = new Label(deficit + material.getUnit() + "  \u26A0");
        deficitBadge.getStyleClass().add("notification-deficit-badge");

        HBox nameRow = new HBox(8, name, nameSpacer, deficitBadge);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label line1 = new Label("Low Stock Item: " + deficit + material.getUnit());
        line1.getStyleClass().add("notification-sub-label");

        Label line2 = new Label("Stock, target: " + material.getMinimumStock());
        line2.getStyleClass().add("notification-sub-label");

        Button restockBtn = new Button("Restock Now");
        restockBtn.getStyleClass().add("btn-primary");
        restockBtn.setPrefWidth(160);
        restockBtn.setOnAction(e -> {
            if (dashboardController != null) {
                dashboardController.goToIncoming();
            }
        });

        VBox leftCol = new VBox(6, nameRow, line1, line2, restockBtn);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        HBox row = new HBox(20, leftCol);
        row.setAlignment(Pos.TOP_LEFT);

        if (anomaly) {

            Label anomalyLabel = new Label("Stock minus anomaly \u26A0");
            anomalyLabel.getStyleClass().add("notification-anomaly-label");

            VBox rightCol = new VBox(anomalyLabel);
            rightCol.setPrefWidth(180);
            rightCol.setAlignment(Pos.TOP_RIGHT);

            row.getChildren().add(rightCol);

        }

        VBox card = new VBox(row);
        card.getStyleClass().add("notification-card");

        return card;

    }

    // =========================================================
    // RECENT ACTIVITY LOG
    // =========================================================

    private void buildActivityLog() {

        activityContainer.getChildren().clear();

        List<ActivityLog> logs = activityLogDAO.getRecent(15);

        if (logs.isEmpty()) {

            Label empty = new Label("No recent activity yet.");
            empty.getStyleClass().add("empty-state-label");
            activityContainer.getChildren().add(empty);
            return;

        }

        for (ActivityLog log : logs) {
            activityContainer.getChildren().add(buildActivityRow(log));
        }

    }

    private HBox buildActivityRow(ActivityLog log) {

        Label icon = new Label(iconFor(log.getType()));
        icon.getStyleClass().add("activity-icon");

        Label actor = new Label(log.getActor() + ":");
        actor.getStyleClass().add("activity-actor");

        Label action = new Label(log.getAction());
        action.getStyleClass().add("activity-action");

        HBox textRow = new HBox(4, actor, action);
        textRow.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label time = new Label(log.getActivityTime());
        time.getStyleClass().add("activity-time");

        HBox row = new HBox(10, icon, textRow, spacer, time);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("activity-row");

        return row;

    }

    private String iconFor(String type) {

        if (type == null) {
            return "\u2022";
        }

        switch (type) {
            case "LOGIN":
                return "\u2192";
            case "LOGOUT":
                return "\u2190";
            case "INCOMING":
                return "\u2B07";
            case "PRODUCTION":
                return "\u2692";
            default:
                return "\u25CF";
        }

    }

}
