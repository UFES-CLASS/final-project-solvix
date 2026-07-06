package com.stockin.controller;

import java.time.LocalDate;

import com.stockin.dao.IncomingMaterialDAO;
import com.stockin.dao.MaterialDAO;
import com.stockin.dao.NotificationDAO;
import com.stockin.model.IncomingMaterial;
import com.stockin.model.Material;
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

public class IncomingMaterialController {

    @FXML
    private ComboBox<Material> cmbMaterial;

    @FXML
    private DatePicker dateIncoming;

    @FXML
    private TextField txtQuantity;

    @FXML
    private TextField txtUnitPrice;

    @FXML
    private TextField txtTotalPrice;

    @FXML
    private TextField txtSupplier;

    @FXML
    private TextField txtNote;

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

    private final IncomingMaterialDAO incomingDAO = new IncomingMaterialDAO();
    private final MaterialDAO materialDAO = new MaterialDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

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

        tableIncoming.setItems(incomingList);

        cmbMaterial.setItems(FXCollections.observableArrayList(materialDAO.getAllMaterials()));

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

    private void fillForm(IncomingMaterial incoming) {

        for (Material material : cmbMaterial.getItems()) {
            if (material.getMaterialId() == incoming.getMaterialId()) {
                cmbMaterial.setValue(material);
                break;
            }
        }

        dateIncoming.setValue(LocalDate.parse(incoming.getIncomingDate()));
        txtQuantity.setText(String.valueOf(incoming.getQuantity()));
        txtUnitPrice.setText(String.valueOf(incoming.getUnitPrice()));
        txtTotalPrice.setText(String.valueOf(incoming.getTotalPrice()));
        txtSupplier.setText(incoming.getSupplier());
        txtNote.setText(incoming.getNote());

    }

    @FXML
    private void saveIncoming() {

        Material material = cmbMaterial.getValue();

        if (material == null || dateIncoming.getValue() == null) {
            AlertUtil.warning("Data belum lengkap", "Material dan tanggal wajib diisi.");
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
                clearForm();
                AlertUtil.info("Berhasil", "Bahan masuk berhasil disimpan. Stok material otomatis bertambah.");
            } else {
                AlertUtil.error("Gagal", "Bahan masuk gagal disimpan.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data tidak valid", "Jumlah dan Harga Satuan harus berupa angka.");
        }

    }

    @FXML
    private void updateIncoming() {

        if (selectedIncoming == null) {
            AlertUtil.warning("Belum ada pilihan", "Pilih data pada tabel terlebih dahulu.");
            return;
        }

        Material material = cmbMaterial.getValue();

        if (material == null || dateIncoming.getValue() == null) {
            AlertUtil.warning("Data belum lengkap", "Material dan tanggal wajib diisi.");
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
                AlertUtil.info("Berhasil", "Bahan masuk berhasil diperbarui.");
            } else {
                AlertUtil.error("Gagal", "Bahan masuk gagal diperbarui.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data tidak valid", "Jumlah dan Harga Satuan harus berupa angka.");
        }

    }

    @FXML
    private void deleteIncoming() {

        if (selectedIncoming == null) {
            AlertUtil.warning("Belum ada pilihan", "Pilih data pada tabel terlebih dahulu.");
            return;
        }

        boolean confirm = AlertUtil.confirm("Konfirmasi Hapus",
                "Hapus data bahan masuk ini? Stok material akan dikurangi kembali.");

        if (!confirm) {
            return;
        }

        boolean success = incomingDAO.deleteIncomingMaterial(selectedIncoming.getIncomingId());

        if (success) {
            refreshMaterialCombo();
            loadTable();
            clearForm();
            AlertUtil.info("Berhasil", "Data bahan masuk berhasil dihapus.");
        } else {
            AlertUtil.error("Gagal", "Data bahan masuk gagal dihapus.");
        }

    }

    @FXML
    private void clearForm() {

        selectedIncoming = null;
        tableIncoming.getSelectionModel().clearSelection();

        cmbMaterial.setValue(null);
        dateIncoming.setValue(LocalDate.now());
        txtQuantity.clear();
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
