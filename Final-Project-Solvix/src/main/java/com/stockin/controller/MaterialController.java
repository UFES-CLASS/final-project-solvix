package com.stockin.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.stockin.dao.MaterialDAO;
import com.stockin.dao.NotificationDAO;
import com.stockin.model.Material;
import com.stockin.util.AlertUtil;
import com.stockin.util.Session;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

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
    private TextField txtSearch;

    @FXML
    private ComboBox<String> cmbCategoryFilter;

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

    @FXML
    private TableColumn<Material, Void> colActions;

    private final MaterialDAO materialDAO = new MaterialDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    // Daftar utama (belum difilter) dan daftar yang benar-benar ditampilkan
    // di tabel (hasil filter pencarian + kategori).
    private final ObservableList<Material> allMaterials = FXCollections.observableArrayList();
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

        setupStockColumn();
        setupActionsColumn();

        // Menandai baris dengan stok menipis
        tableMaterial.setRowFactory(tv -> new TableRow<Material>() {
            @Override
            protected void updateItem(Material item, boolean empty) {
                super.updateItem(item, empty);

                getStyleClass().remove("row-low-stock");

                if (item != null && !empty && MaterialDAO.isLowStock(item)) {
                    getStyleClass().add("row-low-stock");
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

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cmbCategoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        if (Session.isStaff()) {
            txtMinimumStock.setEditable(false);
            txtMinimumStock.setFocusTraversable(false);
            txtMinimumStock.setStyle("-fx-opacity: 0.6;");
            txtMinimumStock.setPromptText("Minimum stock is not editable for staff.");
        }

        loadTable();

    }

    // =========================================================
    // TABLE LOADING & FILTERING
    // =========================================================

    private void loadTable() {

        allMaterials.setAll(materialDAO.getAllMaterials());
        refreshCategoryFilterOptions();
        applyFilters();

    }

    private void refreshCategoryFilterOptions() {

        String current = cmbCategoryFilter.getValue();

        List<String> categories = allMaterials.stream()
                .map(Material::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());

        ObservableList<String> options = FXCollections.observableArrayList();
        options.add("All");
        options.addAll(categories);

        cmbCategoryFilter.setItems(options);

        if (current != null && options.contains(current)) {
            cmbCategoryFilter.setValue(current);
        } else {
            cmbCategoryFilter.setValue("All");
        }

    }

    private void applyFilters() {

        String query = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        String category = cmbCategoryFilter.getValue();

        List<Material> filtered = allMaterials.stream()
                .filter(m -> query.isEmpty()
                        || (m.getMaterialName() != null && m.getMaterialName().toLowerCase().contains(query)))
                .filter(m -> category == null || category.equals("All")
                        || category.equalsIgnoreCase(m.getCategory()))
                .collect(Collectors.toList());

        materialList.setAll(filtered);

    }

    // =========================================================
    // CUSTOM COLUMNS
    // =========================================================

    /**
     * Menampilkan stok apa adanya kalau normal, tapi memberi tanda
     * peringatan + satuan kalau materialnya sedang low stock, contoh
     * "55" untuk stok normal vs "\u26A0 -6kg" untuk stok yang menipis.
     */
    private void setupStockColumn() {

        colStock.setCellFactory(col -> new TableCell<Material, Integer>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);

                getStyleClass().remove("stock-cell-warning");

                if (empty || stock == null || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }

                Material material = (Material) getTableRow().getItem();

                if (MaterialDAO.isLowStock(material)) {
                    setText("\u26A0 " + stock + material.getUnit());
                    getStyleClass().add("stock-cell-warning");
                } else {
                    setText(String.valueOf(stock));
                }

            }
        });

    }

    /**
     * Kolom Actions berisi tombol Edit dan Delete per baris, menggantikan
     * tombol global Update/Delete supaya alurnya lebih jelas seperti pada
     * tabel modern pada umumnya.
     */
    private void setupActionsColumn() {

        colActions.setCellFactory(col -> new TableCell<Material, Void>() {

            private final Button btnEdit = new Button("\u270E Edit");
            private final Button btnDelete = new Button("\uD83D\uDDD1 Delete");
            private final HBox box = new HBox(8, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("table-action-btn");
                btnDelete.getStyleClass().addAll("table-action-btn", "table-action-btn-delete");
                box.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> {
                    Material material = getTableView().getItems().get(getIndex());
                    selectedMaterial = material;
                    fillForm(material);
                    tableMaterial.getSelectionModel().select(material);
                });

                btnDelete.setOnAction(e -> {
                    Material material = getTableView().getItems().get(getIndex());
                    handleDelete(material);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }

        });

    }

    // =========================================================
    // FORM
    // =========================================================

    private void fillForm(Material material) {

        txtMaterialName.setText(material.getMaterialName());
        txtCategory.setText(material.getCategory());
        txtUnit.setText(material.getUnit());
        txtStock.setText(String.valueOf(material.getStock()));
        txtMinimumStock.setText(String.valueOf(material.getMinimumStock()));

    }

    /**
     * Satu tombol untuk dua aksi: kalau sedang tidak mengedit material
     * manapun (form kosong / baru saja Clear Form), ini akan menambah
     * material baru. Kalau sebelumnya baris tertentu dipilih lewat tombol
     * Edit, ini akan meng-update material tersebut.
     */
    @FXML
    private void saveMaterial() {

        String name = txtMaterialName.getText().trim();

        if (name.isEmpty()) {
            AlertUtil.warning("Data is incomplete", "Material name is required.");
            return;
        }

        try {

            if (selectedMaterial != null) {
                updateExistingMaterial(name);
            } else {
                insertNewMaterial(name);
            }

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data is invalid", "Stock and Minimum Stock must be valid numbers.");
        }

    }

    private void insertNewMaterial(String name) {

        Material material = new Material();
        material.setMaterialName(name);
        material.setCategory(txtCategory.getText().trim());
        material.setUnit(txtUnit.getText().trim());
        material.setStock(parseIntOrZero(txtStock.getText()));

        // Staff tidak boleh menentukan ambang batas minimum stock - ini
        // kebijakan yang ditentukan owner. Diberlakukan di sini (bukan
        // cuma di UI) supaya tidak bisa dilewati.
        material.setMinimumStock(Session.isStaff() ? 0 : parseIntOrZero(txtMinimumStock.getText()));

        boolean success = materialDAO.addMaterial(material);

        if (success) {
            checkLowStock(material);
            loadTable();
            clearForm();
            AlertUtil.info("Success", "Material saved successfully.");
        } else {
            AlertUtil.error("Failed", "Failed to save material.");
        }

    }

    private void updateExistingMaterial(String name) {

        selectedMaterial.setMaterialName(name);
        selectedMaterial.setCategory(txtCategory.getText().trim());
        selectedMaterial.setUnit(txtUnit.getText().trim());
        selectedMaterial.setStock(parseIntOrZero(txtStock.getText()));

        // Staff tidak boleh mengubah minimum stock - dibiarkan memakai
        // nilai lama yang sudah ada di selectedMaterial (dari database),
        // apa pun isi field-nya. Diberlakukan di sini juga, bukan cuma
        // dengan menonaktifkan field di UI.
        if (!Session.isStaff()) {
            selectedMaterial.setMinimumStock(parseIntOrZero(txtMinimumStock.getText()));
        }

        boolean success = materialDAO.updateMaterial(selectedMaterial);

        if (success) {
            checkLowStock(selectedMaterial);
            loadTable();
            clearForm();
            AlertUtil.info("Success", "Material updated successfully.");
        } else {
            AlertUtil.error("Failed", "Failed to update material.");
        }

    }

    private void handleDelete(Material material) {

        boolean confirm = AlertUtil.confirm("Confirm Delete",
                "Delete material \"" + material.getMaterialName() + "\"?");

        if (!confirm) {
            return;
        }

        boolean success = materialDAO.deleteMaterial(material.getMaterialId());

        if (success) {
            loadTable();
            clearForm();
            AlertUtil.info("Success", "Material deleted successfully.");
        } else {
            AlertUtil.error("Failed", "Failed to delete material (it might be in use in other data).");
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
