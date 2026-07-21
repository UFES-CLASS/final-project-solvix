package com.stockin.controller;

import java.time.LocalDate;

import com.stockin.dao.ActivityLogDAO;
import com.stockin.dao.IncomingMaterialDAO;
import com.stockin.dao.MaterialDAO;
import com.stockin.dao.NotificationDAO;
import com.stockin.model.IncomingMaterial;
import com.stockin.model.Material;
import com.stockin.util.AlertUtil;
import com.stockin.util.Session;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class IncomingMaterialController {

    @FXML
    private ComboBox<Material> cmbMaterial;

    @FXML
    private DatePicker dateIncoming;

    @FXML
    private TextField txtQuantity;

    @FXML
    private TextField txtUnit;

    @FXML
    private TextField txtUnitPrice;

    @FXML
    private TextField txtTotalPrice;

    @FXML
    private TextField txtSupplier;

    @FXML
    private TextArea txtNote;

    @FXML
    private TableView<IncomingMaterial> tableIncoming;

    @FXML
    private TableColumn<IncomingMaterial, Integer> colId;

    @FXML
    private TableColumn<IncomingMaterial, String> colMaterial;

    @FXML
    private TableColumn<IncomingMaterial, String> colDate;

    @FXML
    private TableColumn<IncomingMaterial, Integer> colQty;

    @FXML
    private TableColumn<IncomingMaterial, String> colUnit;

    @FXML
    private TableColumn<IncomingMaterial, Double> colUnitPrice;

    @FXML
    private TableColumn<IncomingMaterial, Double> colTotalPrice;

    @FXML
    private TableColumn<IncomingMaterial, String> colSupplier;

    @FXML
    private TableColumn<IncomingMaterial, String> colNote;

    @FXML
    private TableColumn<IncomingMaterial, Void> colActions;

    private final IncomingMaterialDAO incomingDAO = new IncomingMaterialDAO();
    private final MaterialDAO materialDAO = new MaterialDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    private final ObservableList<IncomingMaterial> incomingList = FXCollections.observableArrayList();

    private IncomingMaterial selectedIncoming;

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("incomingId"));
        colMaterial.setCellValueFactory(new PropertyValueFactory<>("materialName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("incomingDate"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        setupActionsColumn();

        tableIncoming.setItems(incomingList);

        cmbMaterial.setItems(FXCollections.observableArrayList(materialDAO.getAllMaterials()));

        cmbMaterial.valueProperty().addListener((obs, oldVal, newVal) -> {
            txtUnit.setText(newVal != null ? newVal.getUnit() : "");
        });

        dateIncoming.setValue(LocalDate.now());

        txtQuantity.textProperty().addListener((obs, oldVal, newVal) -> recalculateTotal());
        txtUnitPrice.textProperty().addListener((obs, oldVal, newVal) -> recalculateTotal());

        tableIncoming.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedIncoming = newVal;
                fillForm(newVal);
            }
        });

        loadTable();

    }

    private void loadTable() {
        incomingList.setAll(incomingDAO.getAllIncomingMaterials());
    }

    private void recalculateTotal() {

        try {

            double qty = txtQuantity.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtQuantity.getText().trim());
            double price = txtUnitPrice.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtUnitPrice.getText().trim());

            txtTotalPrice.setText(String.valueOf(qty * price));

        } catch (NumberFormatException e) {
            txtTotalPrice.setText("");
        }

    }

    /**
     * Kolom Actions berisi ikon Edit dan Delete per baris, konsisten dengan
     * tabel Material supaya user bisa langsung mengedit / menghapus baris
     * tanpa harus klik baris dulu baru tekan tombol global di atas.
     */
    private void setupActionsColumn() {

        colActions.setCellFactory(col -> new TableCell<IncomingMaterial, Void>() {

            private final Button btnEdit = new Button("\u270E");
            private final Button btnDelete = new Button("\uD83D\uDDD1");
            private final HBox box = new HBox(6, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().addAll("table-action-icon-btn", "table-action-icon-edit");
                btnDelete.getStyleClass().addAll("table-action-icon-btn", "table-action-icon-delete");
                box.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> {
                    IncomingMaterial incoming = getTableView().getItems().get(getIndex());
                    selectedIncoming = incoming;
                    fillForm(incoming);
                    tableIncoming.getSelectionModel().select(incoming);
                });

                btnDelete.setOnAction(e -> {
                    IncomingMaterial incoming = getTableView().getItems().get(getIndex());
                    selectedIncoming = incoming;
                    deleteIncoming();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }

        });

    }

    private void fillForm(IncomingMaterial incoming) {

        for (Material material : cmbMaterial.getItems()) {
            if (material.getMaterialId() == incoming.getMaterialId()) {
                cmbMaterial.setValue(material);
                break;
            }
        }

        dateIncoming.setValue(LocalDate.parse(incoming.getIncomingDate()));
        txtQuantity.setText(String.valueOf(incoming.getQuantity()));
        txtUnit.setText(incoming.getUnit());
        txtUnitPrice.setText(String.valueOf(incoming.getUnitPrice()));
        txtTotalPrice.setText(String.valueOf(incoming.getTotalPrice()));
        txtSupplier.setText(incoming.getSupplier());
        txtNote.setText(incoming.getNote());

    }

    @FXML
    private void saveIncoming() {

        Material material = cmbMaterial.getValue();

        if (material == null || dateIncoming.getValue() == null) {
            AlertUtil.warning("Data is incomplete", "Material and date are required.");
            return;
        }

        try {

            IncomingMaterial incoming = new IncomingMaterial();
            incoming.setMaterialId(material.getMaterialId());
            incoming.setIncomingDate(dateIncoming.getValue().toString());
            incoming.setQuantity(Integer.parseInt(txtQuantity.getText().trim()));
            incoming.setUnitPrice(Double.parseDouble(txtUnitPrice.getText().trim()));
            incoming.setTotalPrice(incoming.getQuantity() * incoming.getUnitPrice());
            incoming.setSupplier(txtSupplier.getText().trim());
            incoming.setNote(txtNote.getText().trim());

            boolean success = incomingDAO.addIncomingMaterial(incoming);

            if (success) {
                refreshMaterialCombo();
                loadTable();

                activityLogDAO.log(Session.getCurrentUserLabel(),
                        "Received " + incoming.getQuantity() + material.getUnit() + " " + material.getMaterialName(),
                        "INCOMING");

                clearForm();
                AlertUtil.info("Success", "Incoming material saved successfully. Material stock updated automatically.");
            } else {
                AlertUtil.error("Failed", "Failed to save incoming material.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data is invalid", "Quantity and Unit Price must be valid numbers.");
        }

    }

    @FXML
    private void updateIncoming() {

        if (selectedIncoming == null) {
            AlertUtil.warning("No selection", "Please select a record from the table first.");
            return;
        }

        Material material = cmbMaterial.getValue();

        if (material == null || dateIncoming.getValue() == null) {
            AlertUtil.warning("Data is incomplete", "Material and date are required.");
            return;
        }

        try {

            selectedIncoming.setMaterialId(material.getMaterialId());
            selectedIncoming.setIncomingDate(dateIncoming.getValue().toString());
            selectedIncoming.setQuantity(Integer.parseInt(txtQuantity.getText().trim()));
            selectedIncoming.setUnitPrice(Double.parseDouble(txtUnitPrice.getText().trim()));
            selectedIncoming.setTotalPrice(selectedIncoming.getQuantity() * selectedIncoming.getUnitPrice());
            selectedIncoming.setSupplier(txtSupplier.getText().trim());
            selectedIncoming.setNote(txtNote.getText().trim());

            boolean success = incomingDAO.updateIncomingMaterial(selectedIncoming);

            if (success) {
                refreshMaterialCombo();
                loadTable();
                clearForm();
                AlertUtil.info("Success", "Incoming material updated successfully.");
            } else {
                AlertUtil.error("Failed", "Failed to update incoming material.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data is invalid", "Quantity and Unit Price must be valid numbers.");
        }

    }

    @FXML
    private void deleteIncoming() {

        if (selectedIncoming == null) {
            AlertUtil.warning("No selection", "Please select a record from the table first.");
            return;
        }

        boolean confirm = AlertUtil.confirm("Confirm Delete",
                "Delete this incoming material record? Material stock will be reduced accordingly.");

        if (!confirm) {
            return;
        }

        boolean success = incomingDAO.deleteIncomingMaterial(selectedIncoming.getIncomingId());

        if (success) {
            refreshMaterialCombo();
            loadTable();
            clearForm();
            AlertUtil.info("Success", "Incoming material deleted successfully.");
        } else {
            AlertUtil.error("Failed", "Failed to delete incoming material.");
        }

    }

    @FXML
    private void clearForm() {

        selectedIncoming = null;
        tableIncoming.getSelectionModel().clearSelection();

        cmbMaterial.setValue(null);
        dateIncoming.setValue(LocalDate.now());
        txtQuantity.clear();
        txtUnit.clear();
        txtUnitPrice.clear();
        txtTotalPrice.clear();
        txtSupplier.clear();
        txtNote.clear();

    }

    private void refreshMaterialCombo() {

        Material currentSelection = cmbMaterial.getValue();

        cmbMaterial.setItems(FXCollections.observableArrayList(materialDAO.getAllMaterials()));

        // Peringatkan apabila material rendah
        for (Material material : cmbMaterial.getItems()) {

            if (MaterialDAO.isLowStock(material)) {
                notificationDAO.createLowStockNotificationIfNeeded(
                        material.getMaterialId(),
                        material.getMaterialName(),
                        material.getStock(),
                        material.getMinimumStock());
            }

        }

        if (currentSelection != null) {
            cmbMaterial.setValue(currentSelection);
        }

    }

}
