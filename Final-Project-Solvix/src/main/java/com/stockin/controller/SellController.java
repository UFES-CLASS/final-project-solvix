package com.stockin.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.stockin.dao.ActivityLogDAO;
import com.stockin.dao.ProductionDAO;
import com.stockin.model.Production;
import com.stockin.util.AlertUtil;
import com.stockin.util.Session;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller untuk halaman Sell, yang sekarang berdiri sendiri dan
 * terpisah dari halaman Production (lihat Expo Feedback #1). Halaman ini
 * khusus dipakai untuk mencatat jumlah produk yang terjual setiap hari,
 * sementara halaman Production tetap fokus pada pencatatan produksi.
 */
public class SellController {

    @FXML
    private ComboBox<Production> cmbProduct;

    @FXML
    private TextField txtRemainingStock;

    @FXML
    private TextField txtQuantitySold;

    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<Production> tableSell;

    @FXML
    private TableColumn<Production, Integer> colId;

    @FXML
    private TableColumn<Production, String> colProduct;

    @FXML
    private TableColumn<Production, String> colProdDate;

    @FXML
    private TableColumn<Production, String> colExpDate;

    @FXML
    private TableColumn<Production, Integer> colProduced;

    @FXML
    private TableColumn<Production, Integer> colSold;

    @FXML
    private TableColumn<Production, Integer> colRemaining;

    @FXML
    private TableColumn<Production, Double> colPrice;

    private final ProductionDAO productionDAO = new ProductionDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    private final ObservableList<Production> allBatches = FXCollections.observableArrayList();
    private final ObservableList<Production> filteredBatches = FXCollections.observableArrayList();

    private Production selectedBatch;

    // Guard flag supaya sinkronisasi dua arah antara cmbProduct dan
    // pilihan baris tabel tidak saling memicu listener secara berulang.
    private boolean syncingSelection = false;

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("productionId"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colProdDate.setCellValueFactory(new PropertyValueFactory<>("productionDate"));
        colExpDate.setCellValueFactory(new PropertyValueFactory<>("expiredDate"));
        colProduced.setCellValueFactory(new PropertyValueFactory<>("quantityProduced"));
        colSold.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));
        colRemaining.setCellValueFactory(new PropertyValueFactory<>("remainingStock"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));

        tableSell.setItems(filteredBatches);

        // Product sekarang dropdown (ComboBox), bukan field yang cuma
        // terisi otomatis dari klik baris tabel. Menampilkan nama produk +
        // nomor batch + sisa stok langsung di pilihannya, supaya user
        // tahu batch mana yang dipilih tanpa harus menebak dari tabel.
        cmbProduct.setItems(filteredBatches);
        cmbProduct.setButtonCell(createBatchCell());
        cmbProduct.setCellFactory(list -> createBatchCell());

        cmbProduct.valueProperty().addListener((obs, oldVal, newVal) -> {

            selectedBatch = newVal;

            if (newVal != null) {
                txtRemainingStock.setText(String.valueOf(newVal.getRemainingStock()));
            } else {
                txtRemainingStock.clear();
            }

            if (!syncingSelection) {
                syncingSelection = true;
                tableSell.getSelectionModel().select(newVal);
                syncingSelection = false;
            }

        });

        tableSell.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal != null && !syncingSelection) {
                syncingSelection = true;
                cmbProduct.setValue(newVal);
                syncingSelection = false;
            }

        });

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        loadTable();

    }

    /**
     * Format tampilan tiap pilihan di dropdown Product: nama produk, nomor
     * batch, dan sisa stok - supaya jelas batch mana yang sedang dipilih.
     */
    private ListCell<Production> createBatchCell() {

        return new ListCell<>() {
            @Override
            protected void updateItem(Production item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getProductName() + "  •  Batch #" + item.getProductionId()
                            + "  •  Remaining: " + item.getRemainingStock());
                }
            }
        };

    }

    private void loadTable() {
        allBatches.setAll(productionDAO.getAllProduction());
        applyFilter();
    }

    private void applyFilter() {

        String query = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();

        List<Production> filtered = allBatches.stream()
                .filter(p -> query.isEmpty()
                        || (p.getProductName() != null && p.getProductName().toLowerCase().contains(query)))
                .collect(Collectors.toList());

        filteredBatches.setAll(filtered);

    }

    @FXML
    private void recordSale() {

        if (selectedBatch == null) {
            AlertUtil.warning("No selection", "Please select a product/batch from the dropdown first.");
            return;
        }

        String qtyText = txtQuantitySold.getText() == null ? "" : txtQuantitySold.getText().trim();

        if (qtyText.isEmpty()) {
            AlertUtil.warning("Data is incomplete", "Please enter the quantity sold.");
            return;
        }

        try {

            int qty = Integer.parseInt(qtyText);

            if (qty <= 0) {
                AlertUtil.warning("Data is invalid", "Quantity sold must be greater than 0.");
                return;
            }

            int remaining = selectedBatch.getRemainingStock();

            if (qty > remaining) {
                AlertUtil.warning("Insufficient stock",
                        "Only " + remaining + " unit(s) of \"" + selectedBatch.getProductName()
                                + "\" are left from this batch.");
                return;
            }

            selectedBatch.setQuantitySold(selectedBatch.getQuantitySold() + qty);

            boolean success = productionDAO.updateProduction(selectedBatch);

            if (success) {

                activityLogDAO.log(Session.getCurrentUserLabel(),
                        "Sold " + qty + "x " + selectedBatch.getProductName()
                                + " (Batch #" + selectedBatch.getProductionId() + ")", "SELL");

                loadTable();
                clearSelection();
                AlertUtil.info("Success", "Sale recorded successfully.");

            } else {
                AlertUtil.error("Failed", "Failed to record the sale.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data is invalid", "Quantity sold must be a valid number.");
        }

    }

    @FXML
    private void clearSelection() {

        selectedBatch = null;
        tableSell.getSelectionModel().clearSelection();
        cmbProduct.setValue(null);

        txtRemainingStock.clear();
        txtQuantitySold.clear();

    }

}
