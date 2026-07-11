package com.stockin.controller;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.stockin.dao.ProductionDAO;
import com.stockin.model.Production;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;

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
    private javafx.scene.control.ScrollPane chartByProductScroll;

    @FXML
    private BarChart<String, Number> chartByProduct;

    // Lebar per produk (bar) di dalam ScrollPane, supaya tiap bar tetap
    // punya lebar yang cukup untuk dibaca meskipun jumlah produk banyak.
    private static final double CHART_WIDTH_PER_PRODUCT = 110;

    private int lastProductCount = 0;

    @FXML
    private AreaChart<String, Number> chartByDate;

    @FXML
    private TableView<ProductSummary> tableSummary;

    @FXML
    private TableColumn<ProductSummary, Boolean> colSelect;

    @FXML
    private TableColumn<ProductSummary, String> colProduct;

    @FXML
    private TableColumn<ProductSummary, Integer> colProduced;

    @FXML
    private TableColumn<ProductSummary, Integer> colSold;

    @FXML
    private TableColumn<ProductSummary, Double> colRevenue;

    @FXML
    private TableColumn<ProductSummary, Void> colSparkline;

    private final ProductionDAO productionDAO = new ProductionDAO();

    private final DecimalFormat cardCurrency;

    // Warna yang dipakai bergantian untuk bar per produk & sparkline per baris,
    // supaya tiap produk punya identitas warna yang konsisten di seluruh halaman.
    private static final String[] PALETTE = {
            "#4C7AF0", "#F39A3D", "#F5C453", "#2F9E8F", "#7C6FE0", "#E5637C"
    };

    private static final String[] FOOD_EMOJI = {
            "\uD83C\uDF63", "\uD83C\uDF64", "\uD83C\uDF59", "\uD83C\uDF71", "\uD83C\uDF65"
    };

    public ReportController() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        cardCurrency = new DecimalFormat("'Rp' #,##0.00", symbols);
    }

    @FXML
    public void initialize() {

        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colProduced.setCellValueFactory(new PropertyValueFactory<>("totalProduced"));
        colSold.setCellValueFactory(new PropertyValueFactory<>("totalSold"));
        colRevenue.setCellValueFactory(new PropertyValueFactory<>("revenue"));

        setupSelectColumn();
        setupProductColumn();
        setupRevenueColumn();
        setupSparklineColumn();

        // Kalau ukuran ScrollPane berubah (mis. jendela di-resize), lebar
        // chart disesuaikan lagi supaya tetap mengisi ruang yang tersedia.
        if (chartByProductScroll != null) {
            chartByProductScroll.viewportBoundsProperty().addListener((obs, oldB, newB) -> applyChartWidth(lastProductCount));
        }

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
                    p.getProductName(), name -> new ProductSummary(name, p.getProductImage()));

            summary.addProduced(p.getQuantityProduced());
            summary.addSold(p.getQuantitySold());
            summary.addRevenue(p.getRevenue());
            summary.addTrendPoint(p.getRevenue());

            byDate.merge(p.getProductionDate(), p.getRevenue(), Double::sum);

        }

        lblTotalProduced.setText(String.valueOf(totalProduced));
        lblTotalSold.setText(String.valueOf(totalSold));
        lblRevenue.setText(cardCurrency.format(totalRevenue));

        tableSummary.setItems(FXCollections.observableArrayList(byProduct.values()));

        drawBarChart(byProduct);
        drawAreaChart(byDate);

    }

    // =========================================================
    // CHARTS
    // =========================================================

    /**
     * Semua produk digambar dalam SATU series saja. Kalau tiap produk punya
     * series sendiri (seperti sebelumnya), JavaFX BarChart membagi lebar
     * setiap bar dengan JUMLAH series (walau tiap series cuma punya 1 titik
     * data), jadi begitu produknya banyak, bar-nya jadi kelihatan setipis
     * garis. Dengan satu series, lebar bar dihitung penuh per kategori, dan
     * warna tiap bar tetap dibedakan lewat style per-node di bawah.
     */
    private void drawBarChart(Map<String, ProductSummary> byProduct) {

        chartByProduct.getData().clear();

        lastProductCount = byProduct.size();
        applyChartWidth(lastProductCount);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");

        int colorIndex = 0;

        for (ProductSummary summary : byProduct.values()) {

            XYChart.Data<String, Number> data = new XYChart.Data<>(summary.getProductName(), summary.getRevenue());
            series.getData().add(data);

            final int idx = colorIndex;
            applyBarColor(data, PALETTE[idx % PALETTE.length]);

            colorIndex++;

        }

        chartByProduct.getData().add(series);

    }

    private void applyBarColor(XYChart.Data<String, Number> data, String colorHex) {

        if (data.getNode() != null) {
            data.getNode().setStyle("-fx-bar-fill:" + colorHex + ";-fx-background-radius:4 4 0 0;");
        } else {
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill:" + colorHex + ";-fx-background-radius:4 4 0 0;");
                }
            });
        }

    }

    /**
     * Lebar chart mengikuti jumlah produk, supaya tiap bar cukup lebar
     * untuk terlihat jelas. Kalau totalnya melebihi lebar ScrollPane,
     * pengguna tinggal geser (scroll) horizontal untuk melihat sisanya.
     */
    private void applyChartWidth(int productCount) {

        double viewportWidth = chartByProductScroll != null && chartByProductScroll.getViewportBounds() != null
                ? chartByProductScroll.getViewportBounds().getWidth()
                : 0;

        double computedWidth = Math.max(productCount, 1) * CHART_WIDTH_PER_PRODUCT;
        double chartWidth = Math.max(computedWidth, viewportWidth);

        chartByProduct.setPrefWidth(chartWidth);
        chartByProduct.setMinWidth(chartWidth);

    }

    private void drawAreaChart(Map<String, Double> byDate) {

        chartByDate.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");

        for (Map.Entry<String, Double> entry : byDate.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        chartByDate.getData().add(series);

    }

    // =========================================================
    // TABLE COLUMNS
    // =========================================================

    private void setupSelectColumn() {

        CheckBox headerCheck = new CheckBox();
        headerCheck.setOnAction(e -> {
            for (ProductSummary summary : tableSummary.getItems()) {
                summary.setSelected(headerCheck.isSelected());
            }
            tableSummary.refresh();
        });

        HBox headerBox = new HBox(headerCheck);
        headerBox.setAlignment(Pos.CENTER);
        colSelect.setGraphic(headerBox);
        colSelect.setText("");

        colSelect.setCellFactory(col -> new TableCell<ProductSummary, Boolean>() {

            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                ProductSummary summary = (ProductSummary) getTableRow().getItem();
                checkBox.setSelected(summary.isSelected());
                checkBox.setOnAction(e -> summary.setSelected(checkBox.isSelected()));

                setGraphic(checkBox);
                setAlignment(Pos.CENTER);

            }
        });

    }

    private void setupProductColumn() {

        colProduct.setCellFactory(col -> new TableCell<ProductSummary, String>() {

            private final Label icon = new Label();
            private final ImageView imageView = new ImageView();
            private final Label name = new Label();
            private final HBox box = new HBox(10, icon, name);

            {
                icon.getStyleClass().add("report-product-icon");
                name.getStyleClass().add("report-product-name");
                imageView.setFitWidth(34);
                imageView.setFitHeight(34);
                imageView.setPreserveRatio(true);
                box.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                ProductSummary summary = (ProductSummary) getTableRow().getItem();
                int rowIndex = getIndex();

                name.setText(summary.getProductName());

                Image img = loadImage(summary.getProductImage());

                if (img != null) {
                    imageView.setImage(img);
                    box.getChildren().set(0, imageView);
                } else {
                    icon.setText(FOOD_EMOJI[rowIndex % FOOD_EMOJI.length]);
                    icon.setStyle("-fx-background-color:" + PALETTE[rowIndex % PALETTE.length] + "33;");
                    box.getChildren().set(0, icon);
                }

                setGraphic(box);

            }
        });

    }

    private void setupRevenueColumn() {

        // Kolom revenue di tabel ditampilkan apa adanya ("Rp " + angka),
        // sama seperti pada rancangan tampilan yang diberikan.
        colRevenue.setCellFactory(col -> new TableCell<ProductSummary, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                    return;
                }

                setText("Rp " + value);
                getStyleClass().add("report-revenue-cell");

            }
        });

    }

    private void setupSparklineColumn() {

        colSparkline.setCellFactory(col -> new TableCell<ProductSummary, Void>() {

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                ProductSummary summary = (ProductSummary) getTableRow().getItem();
                int rowIndex = getIndex();

                setGraphic(buildSparkline(summary.getTrend(), PALETTE[rowIndex % PALETTE.length]));
                setAlignment(Pos.CENTER);

            }
        });

    }

    /**
     * Membuat mini area-chart (sparkline) dari daftar nilai revenue per
     * tanggal untuk satu produk, dipakai di kolom "Sparkline Revenue".
     */
    private Pane buildSparkline(List<Double> values, String colorHex) {

        double width = 110;
        double height = 34;

        Pane pane = new Pane();
        pane.setPrefSize(width, height);
        pane.setMinSize(width, height);
        pane.setMaxSize(width, height);

        if (values == null || values.isEmpty()) {
            return pane;
        }

        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double range = Math.max(max - min, 1);

        int n = values.size();
        double stepX = n > 1 ? width / (n - 1) : 0;

        Polyline line = new Polyline();
        Polygon fill = new Polygon();

        fill.getPoints().addAll(0.0, height);

        for (int i = 0; i < n; i++) {
            double x = n > 1 ? i * stepX : width / 2;
            double y = height - ((values.get(i) - min) / range) * (height - 6) - 3;
            line.getPoints().addAll(x, y);
            fill.getPoints().addAll(x, y);
        }

        fill.getPoints().addAll(width, height);

        Color base = Color.web(colorHex);
        fill.setFill(Color.color(base.getRed(), base.getGreen(), base.getBlue(), 0.18));
        line.setStroke(base);
        line.setStrokeWidth(2);

        pane.getChildren().addAll(fill, line);

        return pane;

    }

    private Image loadImage(String path) {

        try {

            if (path != null && !path.isEmpty()) {
                return new Image(new File(path).toURI().toString(), true);
            }

        } catch (Exception e) {
            // gambar tidak valid / tidak ditemukan -> pakai fallback emoji
        }

        return null;

    }

    // =========================================================
    // ROW MODEL
    // =========================================================

    public static class ProductSummary {

        private final String productName;
        private final String productImage;
        private int totalProduced;
        private int totalSold;
        private double revenue;
        private boolean selected;
        private final List<Double> trend = new ArrayList<>();

        public ProductSummary(String productName, String productImage) {
            this.productName = productName;
            this.productImage = productImage;
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

        public void addTrendPoint(double amount) {
            trend.add(amount);
        }

        public String getProductName() {
            return productName;
        }

        public String getProductImage() {
            return productImage;
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

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public List<Double> getTrend() {
            return trend;
        }

    }

}
