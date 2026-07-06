package com.stockin.controller;

import com.stockin.dao.MaterialDAO;
import com.stockin.dao.NotificationDAO;
import com.stockin.model.Material;
import com.stockin.util.AlertUtil;
import com.stockin.util.Session;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class MaterialController {

    @FXML
    private TextField txtMaterialName;

    @FXML
    private TextField txtCategory;

    @FXML
    private TextField txtUnit;

    @FXML
    private TextField txtStock;

    @FXML
    private TextField txtMinimumStock;

    @FXML
    private TableView<Material> tableMaterial;

    @FXML
    private TableColumn<Material, Integer> colId;

    @FXML
    private TableColumn<Material, String> colName;

    @FXML
    private TableColumn<Material, String> colCategory;

    @FXML
    private TableColumn<Material, String> colUnit;

    @FXML
    private TableColumn<Material, Integer> colStock;

    @FXML
    private TableColumn<Material, Integer> colMinimum;

    private final MaterialDAO materialDAO = new MaterialDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private final ObservableList<Material> materialList = FXCollections.observableArrayList();

    private Material selectedMaterial;

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("materialId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("materialName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colMinimum.setCellValueFactory(new PropertyValueFactory<>("minimumStock"));

        // Menandai baris dengan stok menipis
        tableMaterial.setRowFactory(tv -> new TableRow<Material>() {
            @Override
            protected void updateItem(Material item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else if (MaterialDAO.isLowStock(item)) {
                    setStyle("-fx-background-color:#FEE2E2;");
                } else {
                    setStyle("");
                }
            }
        });

        tableMaterial.setItems(materialList);

        tableMaterial.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedMaterial = newVal;
                fillForm(newVal);
            }
        });

        if (Session.isStaff()) {
            txtMinimumStock.setPromptText("Minimum stock diatur oleh Owner");
        }

        loadTable();

    }

    private void loadTable() {

        materialList.setAll(materialDAO.getAllMaterials());

    }

    private void fillForm(Material material) {

        txtMaterialName.setText(material.getMaterialName());
        txtCategory.setText(material.getCategory());
        txtUnit.setText(material.getUnit());
        txtStock.setText(String.valueOf(material.getStock()));
        txtMinimumStock.setText(String.valueOf(material.getMinimumStock()));

    }

    @FXML
    private void saveMaterial() {

        String name = txtMaterialName.getText().trim();

        if (name.isEmpty()) {
            AlertUtil.warning("Data belum lengkap", "Nama material wajib diisi.");
            return;
        }

        try {

            Material material = new Material();
            material.setMaterialName(name);
            material.setCategory(txtCategory.getText().trim());
            material.setUnit(txtUnit.getText().trim());
            material.setStock(parseIntOrZero(txtStock.getText()));
            material.setMinimumStock(parseIntOrZero(txtMinimumStock.getText()));

            boolean success = materialDAO.addMaterial(material);

            if (success) {
                checkLowStock(material);
                loadTable();
                clearForm();
                AlertUtil.info("Berhasil", "Material berhasil ditambahkan.");
            } else {
                AlertUtil.error("Gagal", "Material gagal ditambahkan.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data tidak valid", "Stock dan Minimum Stock harus berupa angka.");
        }

    }

    @FXML
    private void updateMaterial() {

        if (selectedMaterial == null) {
            AlertUtil.warning("Belum ada pilihan", "Pilih material pada tabel terlebih dahulu.");
            return;
        }

        try {

            selectedMaterial.setMaterialName(txtMaterialName.getText().trim());
            selectedMaterial.setCategory(txtCategory.getText().trim());
            selectedMaterial.setUnit(txtUnit.getText().trim());
            selectedMaterial.setStock(parseIntOrZero(txtStock.getText()));
            selectedMaterial.setMinimumStock(parseIntOrZero(txtMinimumStock.getText()));

            boolean success = materialDAO.updateMaterial(selectedMaterial);

            if (success) {
                checkLowStock(selectedMaterial);
                loadTable();
                clearForm();
                AlertUtil.info("Berhasil", "Material berhasil diperbarui.");
            } else {
                AlertUtil.error("Gagal", "Material gagal diperbarui.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data tidak valid", "Stock dan Minimum Stock harus berupa angka.");
        }

    }

    @FXML
    private void deleteMaterial() {

        if (selectedMaterial == null) {
            AlertUtil.warning("Belum ada pilihan", "Pilih material pada tabel terlebih dahulu.");
            return;
        }

        boolean confirm = AlertUtil.confirm("Konfirmasi Hapus",
                "Hapus material \"" + selectedMaterial.getMaterialName() + "\"?");

        if (!confirm) {
            return;
        }

        boolean success = materialDAO.deleteMaterial(selectedMaterial.getMaterialId());

        if (success) {
            loadTable();
            clearForm();
            AlertUtil.info("Berhasil", "Material berhasil dihapus.");
        } else {
            AlertUtil.error("Gagal", "Material gagal dihapus (mungkin masih dipakai di data lain).");
        }

    }

    @FXML
    private void clearForm() {

        selectedMaterial = null;
        tableMaterial.getSelectionModel().clearSelection();

        txtMaterialName.clear();
        txtCategory.clear();
        txtUnit.clear();
        txtStock.clear();
        txtMinimumStock.clear();

    }

    private void checkLowStock(Material material) {

        if (MaterialDAO.isLowStock(material)) {
            notificationDAO.createLowStockNotificationIfNeeded(
                    material.getMaterialId(),
                    material.getMaterialName(),
                    material.getStock(),
                    material.getMinimumStock());
        }

    }

    private int parseIntOrZero(String text) {

        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        return Integer.parseInt(text.trim());

    }

}
