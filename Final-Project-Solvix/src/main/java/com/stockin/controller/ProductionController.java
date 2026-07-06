package com.stockin.controller;

import java.time.LocalDate;

import com.stockin.dao.MaterialDAO;
import com.stockin.dao.NotificationDAO;
import com.stockin.dao.ProductDAO;
import com.stockin.dao.ProductionDAO;
import com.stockin.model.Material;
import com.stockin.model.MaterialUsageItem;
import com.stockin.model.Product;
import com.stockin.model.Production;
import com.stockin.util.AlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ProductionController {

    @FXML
    private ComboBox<Product> cmbProduct;

    @FXML
    private DatePicker dateProduction;

    @FXML
    private DatePicker dateExpired;

    @FXML
    private TextField txtSellingPrice;

    @FXML
    private TextField txtQuantityProduced;

    @FXML
    private TextField txtQuantitySold;

    @FXML
    private TextField txtNote;

    @FXML
    private ComboBox<Material> cmbMaterialUsed;

    @FXML
    private TextField txtMaterialQtyUsed;

    @FXML
    private TableView<MaterialUsageItem> tableMaterialUsed;

    @FXML
    private TableColumn<MaterialUsageItem, String> colMaterialUsedName;

    @FXML
    private TableColumn<MaterialUsageItem, String> colMaterialUsedQty;

    @FXML
    private TableView<Production> tableProduction;

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
    private TableColumn<Production, Double> colPrice;

    @FXML
    private TableColumn<Production, Double> colRevenue;

    @FXML
    private TableColumn<Production, String> colNote;

    private final ProductionDAO productionDAO = new ProductionDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final MaterialDAO materialDAO = new MaterialDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private final ObservableList<Production> productionList = FXCollections.observableArrayList();
    private final ObservableList<MaterialUsageItem> materialUsageList = FXCollections.observableArrayList();

    private Production selectedProduction;

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("productionId"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colProdDate.setCellValueFactory(new PropertyValueFactory<>("productionDate"));
        colExpDate.setCellValueFactory(new PropertyValueFactory<>("expiredDate"));
        colProduced.setCellValueFactory(new PropertyValueFactory<>("quantityProduced"));
        colSold.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        colRevenue.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        tableProduction.setItems(productionList);

        colMaterialUsedName.setCellValueFactory(new PropertyValueFactory<>("materialName"));
        colMaterialUsedQty.setCellValueFactory(new PropertyValueFactory<>("quantityDisplay"));
        tableMaterialUsed.setItems(materialUsageList);

        cmbProduct.setItems(FXCollections.observableArrayList(productDAO.getAllProducts()));
        cmbMaterialUsed.setItems(FXCollections.observableArrayList(materialDAO.getAllMaterials()));


        cmbProduct.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtSellingPrice.setText(String.valueOf(newVal.getSellingPrice()));
            }
        });

        dateProduction.setValue(LocalDate.now());

        tableProduction.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedProduction = newVal;
                fillForm(newVal);
            }
        });

        loadTable();

    }

    private void loadTable() {
        productionList.setAll(productionDAO.getAllProduction());
    }

    private void fillForm(Production production) {

        for (Product product : cmbProduct.getItems()) {
            if (product.getProductId() == production.getProductId()) {
                cmbProduct.setValue(product);
                break;
            }
        }

        dateProduction.setValue(LocalDate.parse(production.getProductionDate()));

        if (production.getExpiredDate() != null && !production.getExpiredDate().isEmpty()) {
            dateExpired.setValue(LocalDate.parse(production.getExpiredDate()));
        } else {
            dateExpired.setValue(null);
        }

        txtSellingPrice.setText(String.valueOf(production.getSellingPrice()));
        txtQuantityProduced.setText(String.valueOf(production.getQuantityProduced()));
        txtQuantitySold.setText(String.valueOf(production.getQuantitySold()));
        txtNote.setText(production.getNote());

        // Field bahan digunakan hanya berlaku saat input baru
        cmbMaterialUsed.setValue(null);
        txtMaterialQtyUsed.clear();
        materialUsageList.clear();

    }

    @FXML
    private void saveProduction() {

        Product product = cmbProduct.getValue();

        if (product == null || dateProduction.getValue() == null) {
            AlertUtil.warning("Data belum lengkap", "Produk dan tanggal produksi wajib diisi.");
            return;
        }

        try {

            Production production = new Production();
            production.setProductId(product.getProductId());
            production.setProductionDate(dateProduction.getValue().toString());
            production.setExpiredDate(dateExpired.getValue() != null ? dateExpired.getValue().toString() : null);
            production.setQuantityProduced(parseIntOrZero(txtQuantityProduced.getText()));
            production.setQuantitySold(parseIntOrZero(txtQuantitySold.getText()));
            production.setSellingPrice(parseDoubleOrZero(txtSellingPrice.getText()));
            production.setNote(txtNote.getText().trim());

            boolean success = productionDAO.addProduction(production);

            if (!success) {
                AlertUtil.error("Gagal", "Data produksi gagal disimpan.");
                return;
            }

            applyMaterialUsage();

            loadTable();
            clearForm();
            AlertUtil.info("Berhasil", "Data produksi berhasil disimpan.");

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data tidak valid", "Jumlah dan harga harus berupa angka.");
        }

    }

    @FXML
    private void updateProduction() {

        if (selectedProduction == null) {
            AlertUtil.warning("Belum ada pilihan", "Pilih data produksi pada tabel terlebih dahulu.");
            return;
        }

        Product product = cmbProduct.getValue();

        if (product == null || dateProduction.getValue() == null) {
            AlertUtil.warning("Data belum lengkap", "Produk dan tanggal produksi wajib diisi.");
            return;
        }

        try {

            selectedProduction.setProductId(product.getProductId());
            selectedProduction.setProductionDate(dateProduction.getValue().toString());
            selectedProduction.setExpiredDate(dateExpired.getValue() != null ? dateExpired.getValue().toString() : null);
            selectedProduction.setQuantityProduced(parseIntOrZero(txtQuantityProduced.getText()));
            selectedProduction.setQuantitySold(parseIntOrZero(txtQuantitySold.getText()));
            selectedProduction.setSellingPrice(parseDoubleOrZero(txtSellingPrice.getText()));
            selectedProduction.setNote(txtNote.getText().trim());

            boolean success = productionDAO.updateProduction(selectedProduction);

            if (success) {
                loadTable();
                clearForm();
                AlertUtil.info("Berhasil", "Data produksi berhasil diperbarui.");
            } else {
                AlertUtil.error("Gagal", "Data produksi gagal diperbarui.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data tidak valid", "Jumlah dan harga harus berupa angka.");
        }

    }

    @FXML
    private void deleteProduction() {

        if (selectedProduction == null) {
            AlertUtil.warning("Belum ada pilihan", "Pilih data produksi pada tabel terlebih dahulu.");
            return;
        }

        boolean confirm = AlertUtil.confirm("Konfirmasi Hapus", "Hapus data produksi ini?");

        if (!confirm) {
            return;
        }

        boolean success = productionDAO.deleteProduction(selectedProduction.getProductionId());

        if (success) {
            loadTable();
            clearForm();
            AlertUtil.info("Berhasil", "Data produksi berhasil dihapus.");
        } else {
            AlertUtil.error("Gagal", "Data produksi gagal dihapus.");
        }

    }

    @FXML
    private void clearForm() {

        selectedProduction = null;
        tableProduction.getSelectionModel().clearSelection();

        cmbProduct.setValue(null);
        dateProduction.setValue(LocalDate.now());
        dateExpired.setValue(null);
        txtSellingPrice.clear();
        txtQuantityProduced.clear();
        txtQuantitySold.clear();
        txtNote.clear();
        cmbMaterialUsed.setValue(null);
        txtMaterialQtyUsed.clear();
        materialUsageList.clear();

    }

    @FXML
    private void addMaterialUsage() {

        Material material = cmbMaterialUsed.getValue();
        String qtyText = txtMaterialQtyUsed.getText().trim();

        if (material == null) {
            AlertUtil.warning("Data belum lengkap", "Pilih material terlebih dahulu.");
            return;
        }

        if (qtyText.isEmpty()) {
            AlertUtil.warning("Data belum lengkap", "Isi jumlah bahan yang dipakai.");
            return;
        }

        try {

            int qtyUsed = Integer.parseInt(qtyText);

            if (qtyUsed <= 0) {
                AlertUtil.warning("Data tidak valid", "Jumlah bahan dipakai harus lebih dari 0.");
                return;
            }

            for (int i = 0; i < materialUsageList.size(); i++) {

                MaterialUsageItem existing = materialUsageList.get(i);

                if (existing.getMaterialId() == material.getMaterialId()) {

                    MaterialUsageItem merged = new MaterialUsageItem(
                            existing.getMaterialId(),
                            existing.getMaterialName(),
                            existing.getUnit(),
                            existing.getQuantityUsed() + qtyUsed);

                    materialUsageList.set(i, merged);

                    cmbMaterialUsed.setValue(null);
                    txtMaterialQtyUsed.clear();
                    return;

                }

            }

            materialUsageList.add(new MaterialUsageItem(
                    material.getMaterialId(),
                    material.getMaterialName(),
                    material.getUnit(),
                    qtyUsed));

            cmbMaterialUsed.setValue(null);
            txtMaterialQtyUsed.clear();

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data tidak valid", "Jumlah bahan dipakai harus berupa angka.");
        }

    }

    /**
     * Menghapus baris material yang sedang dipilih pada tabel daftar bahan.
     */
    @FXML
    private void removeMaterialUsage() {

        MaterialUsageItem selected = tableMaterialUsed.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.warning("Belum ada pilihan", "Pilih baris material pada daftar bahan terlebih dahulu.");
            return;
        }

        materialUsageList.remove(selected);

    }

    private void applyMaterialUsage() {

        if (materialUsageList.isEmpty()) {
            return;
        }

        for (MaterialUsageItem item : materialUsageList) {

            materialDAO.updateStock(item.getMaterialId(), -item.getQuantityUsed());

            Material refreshed = materialDAO.getMaterialById(item.getMaterialId());

            if (refreshed != null && MaterialDAO.isLowStock(refreshed)) {
                notificationDAO.createLowStockNotificationIfNeeded(
                        refreshed.getMaterialId(),
                        refreshed.getMaterialName(),
                        refreshed.getStock(),
                        refreshed.getMinimumStock());
            }

        }

        cmbMaterialUsed.setItems(FXCollections.observableArrayList(materialDAO.getAllMaterials()));
        materialUsageList.clear();

    }

    private int parseIntOrZero(String text) {

        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        return Integer.parseInt(text.trim());

    }

    private double parseDoubleOrZero(String text) {

        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        return Double.parseDouble(text.trim());

    }

}