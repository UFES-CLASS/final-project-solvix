package com.stockin.controller;

import java.time.LocalDate;
import java.util.List;

import com.stockin.dao.ActivityLogDAO;
import com.stockin.dao.MaterialDAO;
import com.stockin.dao.NotificationDAO;
import com.stockin.dao.ProductDAO;
import com.stockin.dao.ProductMaterialDAO;
import com.stockin.dao.ProductionDAO;
import com.stockin.dao.ProductionMaterialDAO;
import com.stockin.model.Material;
import com.stockin.model.MaterialUsageItem;
import com.stockin.model.Product;
import com.stockin.model.Production;
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
    private TextArea txtNote;

    @FXML
    private TableView<MaterialUsageItem> tableMaterialUsed;

    @FXML
    private TableColumn<MaterialUsageItem, String> colMaterialUsedName;

    @FXML
    private TableColumn<MaterialUsageItem, String> colMaterialUsedQty;

    @FXML
    private TableColumn<MaterialUsageItem, String> colMaterialUsedUnit;

    @FXML
    private ComboBox<Material> cmbMaterialUsed;

    @FXML
    private TextField txtMaterialQtyUsed;

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

    @FXML
    private TableColumn<Production, Void> colActions;

    private final ProductionDAO productionDAO = new ProductionDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final MaterialDAO materialDAO = new MaterialDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final ProductionMaterialDAO productionMaterialDAO = new ProductionMaterialDAO();
    private final ProductMaterialDAO productMaterialDAO = new ProductMaterialDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    private final ObservableList<Production> productionList = FXCollections.observableArrayList();
    private final ObservableList<MaterialUsageItem> materialUsageList = FXCollections.observableArrayList();

    private Production selectedProduction;

    // Menyimpan materialId mana saja pada materialUsageList yang berasal
    // dari BOM (bukan bahan ekstra yang ditambahkan manual oleh user),
    // supaya recalcMaterialsFromBom() bisa menghitung ulang baris BOM
    // tanpa menghapus baris ekstra yang sengaja ditambahkan manual.
    private final java.util.Set<Integer> bomMaterialIds = new java.util.HashSet<>();

    // true saat form sedang menampilkan data lama (mode lihat/update) -
    // dalam mode ini daftar bahan hanya untuk ditampilkan (riwayat),
    // tidak untuk diedit, karena tombol Update belum menyesuaikan ulang stok.
    private boolean viewingExistingUsage = false;

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
        colMaterialUsedQty.setCellValueFactory(new PropertyValueFactory<>("quantityUsed"));
        colMaterialUsedUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        tableMaterialUsed.setItems(materialUsageList);
        cmbMaterialUsed.setItems(FXCollections.observableArrayList(materialDAO.getAllMaterials()));

        setupActionsColumn();

        cmbProduct.setItems(FXCollections.observableArrayList(productDAO.getAllProducts()));


        cmbProduct.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtSellingPrice.setText(String.valueOf(newVal.getSellingPrice()));
            }
            recalcMaterialsFromBom();
        });

        // Setiap kali Quantity Produced diketik ulang, kebutuhan bahan
        // dihitung ulang otomatis dari BOM produk yang dipilih (quantity
        // per unit x jumlah produksi), sehingga user tidak perlu memilih
        // bahan satu per satu secara manual.
        txtQuantityProduced.textProperty().addListener((obs, oldVal, newVal) -> recalcMaterialsFromBom());

        dateProduction.setValue(LocalDate.now());

        // DatePicker dibuat non-editable (tidak bisa diketik manual).
        // Sebelumnya walau tanggal yang tidak valid sudah dinonaktifkan di
        // kalender, user masih bisa mengetik tanggal hari ini/tanggal lain
        // langsung di kotak teksnya dan melewati batasan itu. Dengan
        // non-editable, satu-satunya cara mengisi tanggal adalah lewat
        // kalender yang sudah menonaktifkan tanggal yang tidak valid.
        dateExpired.setEditable(false);

        // Expired Date tidak boleh lebih awal (atau sama) dari Production
        // Date. Tanggal yang tidak valid dinonaktifkan langsung di kalender
        // DatePicker supaya tidak bisa dipilih sama sekali, bukan cuma
        // divalidasi setelah disimpan.
        setupExpiredDateConstraint();

        dateProduction.valueProperty().addListener((obs, oldVal, newVal) -> {

            // Kalau expired date yang sudah dipilih jadi tidak valid lagi
            // (karena production date diubah jadi lebih baru/lebih akhir),
            // kosongkan supaya user tidak sadar menyimpan data yang salah.
            if (dateExpired.getValue() != null && newVal != null
                    && !dateExpired.getValue().isAfter(newVal)) {
                dateExpired.setValue(null);
            }

        });

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

    /**
     * Menghitung ulang baris BOM pada materialUsageList berdasarkan Bill
     * of Materials (BOM) produk yang dipilih dikalikan Quantity Produced.
     * Baris "ekstra" yang ditambahkan manual oleh user (di luar BOM) tetap
     * dipertahankan - hanya baris yang berasal dari BOM yang dihitung
     * ulang setiap kali produk/jumlah produksi berubah.
     */
    private void recalcMaterialsFromBom() {

        if (viewingExistingUsage) {
            return;
        }

        Product product = cmbProduct.getValue();
        int quantityProduced = parseIntSafely(txtQuantityProduced.getText());

        // Simpan dulu baris ekstra (bukan dari BOM) sebelum list dibersihkan.
        List<MaterialUsageItem> extras = new java.util.ArrayList<>();
        for (MaterialUsageItem item : materialUsageList) {
            if (!bomMaterialIds.contains(item.getMaterialId())) {
                extras.add(item);
            }
        }

        materialUsageList.clear();
        bomMaterialIds.clear();

        if (product != null && quantityProduced > 0) {

            for (MaterialUsageItem bomItem : productMaterialDAO.getBomByProduct(product.getProductId())) {

                materialUsageList.add(new MaterialUsageItem(
                        bomItem.getMaterialId(),
                        bomItem.getMaterialName(),
                        bomItem.getUnit(),
                        bomItem.getQuantityUsed() * quantityProduced));

                bomMaterialIds.add(bomItem.getMaterialId());

            }

        }

        materialUsageList.addAll(extras);

    }

    /**
     * Menambah bahan EKSTRA yang dipakai di batch produksi ini tapi tidak
     * ada di resep (BOM) produk-nya - misalnya bahan tambahan dadakan.
     * Baris ini tidak akan hilang / dihitung ulang otomatis seperti baris
     * BOM, karena materialId-nya tidak dicatat di bomMaterialIds.
     */
    @FXML
    private void addMaterialUsage() {

        Material material = cmbMaterialUsed.getSelectionModel().getSelectedItem();
        String qtyText = txtMaterialQtyUsed.getText() == null ? "" : txtMaterialQtyUsed.getText().trim();

        if (material == null) {
            AlertUtil.warning("Data is incomplete", "Please select a material first.");
            return;
        }

        if (qtyText.isEmpty()) {
            AlertUtil.warning("Data is incomplete", "Please enter the quantity used.");
            return;
        }

        try {

            int qty = Integer.parseInt(qtyText);

            if (qty <= 0) {
                AlertUtil.warning("Data is invalid", "Quantity used must be greater than 0.");
                return;
            }

            for (int i = 0; i < materialUsageList.size(); i++) {

                MaterialUsageItem existing = materialUsageList.get(i);

                if (existing.getMaterialId() == material.getMaterialId() && !bomMaterialIds.contains(existing.getMaterialId())) {
                    materialUsageList.set(i, new MaterialUsageItem(
                            existing.getMaterialId(), existing.getMaterialName(), existing.getUnit(), qty));
                    cmbMaterialUsed.setValue(null);
                    txtMaterialQtyUsed.clear();
                    return;
                }

            }

            materialUsageList.add(new MaterialUsageItem(
                    material.getMaterialId(), material.getMaterialName(), material.getUnit(), qty));

            cmbMaterialUsed.setValue(null);
            txtMaterialQtyUsed.clear();

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data is invalid", "Quantity used must be a valid number.");
        }

    }

    @FXML
    private void removeMaterialUsage() {

        MaterialUsageItem selected = tableMaterialUsed.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.warning("No selection", "Please select a material row from the list first.");
            return;
        }

        materialUsageList.remove(selected);
        bomMaterialIds.remove(selected.getMaterialId());

    }

    private int parseIntSafely(String text) {
        try {
            return parseIntOrZero(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Menerapkan dayCellFactory pada dateExpired supaya tanggal sebelum
     * atau sama dengan Production Date tampil abu-abu / tidak bisa
     * diklik di kalender. Mencegah input data yang salah (expired date
     * lebih awal dari production date) sejak dari sumbernya, bukan
     * cuma lewat validasi setelah tombol Save ditekan.
     */
    private void setupExpiredDateConstraint() {

        dateExpired.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                LocalDate productionDate = dateProduction.getValue();

                // PENTING: DateCell di JavaFX dipakai ulang (recycled) saat
                // kalender berpindah bulan/tampilan. Kalau kita hanya
                // men-set disable=true tanpa pernah mengembalikannya ke
                // false untuk tanggal yang valid, sebuah cell yang tadinya
                // dinonaktifkan bisa "nyangkut" statusnya saat dipakai
                // ulang untuk tanggal lain (termasuk membuat tanggal hari
                // ini / tanggal valid lain jadi tidak konsisten). Karena
                // itu kedua kondisi (valid maupun tidak valid) harus
                // sama-sama di-set eksplisit setiap kali updateItem dipanggil.
                boolean invalid = !empty && productionDate != null && date != null
                        && !date.isAfter(productionDate);

                setDisable(invalid);
                setStyle(invalid ? "-fx-background-color: #f0f0f0; -fx-opacity: 0.5;" : "");
            }
        });

    }

    private void setupActionsColumn() {

        colActions.setCellFactory(col -> new TableCell<Production, Void>() {

            private final Button btnEdit = new Button("\u270E");
            private final Button btnDelete = new Button("\uD83D\uDDD1");
            private final HBox box = new HBox(6, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().addAll("table-action-icon-btn", "table-action-icon-edit");
                btnDelete.getStyleClass().addAll("table-action-icon-btn", "table-action-icon-delete");
                box.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> {
                    Production production = getTableView().getItems().get(getIndex());
                    selectedProduction = production;
                    fillForm(production);
                    tableProduction.getSelectionModel().select(production);
                });

                btnDelete.setOnAction(e -> {
                    Production production = getTableView().getItems().get(getIndex());
                    selectedProduction = production;
                    tableProduction.getSelectionModel().select(production);
                    deleteProduction();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }

        });

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
        txtNote.setText(production.getNote());

        // Untuk produksi yang sudah ada, tampilkan riwayat bahan yang benar-benar
        // tersimpan di database (bukan form input baru). Daftar ini hanya untuk
        // dilihat karena tombol Update belum menyesuaikan ulang stok bahan.
        viewingExistingUsage = true;
        materialUsageList.setAll(
                productionMaterialDAO.getUsageByProduction(production.getProductionId()));

    }

    @FXML
    private void saveProduction() {

        Product product = cmbProduct.getValue();

        if (product == null || dateProduction.getValue() == null) {
            AlertUtil.warning("Data is incomplete", "Product and production date are required.");
            return;
        }

        if (viewingExistingUsage) {
            AlertUtil.warning("Invalid action",
                    "You are viewing an existing production record. Press Reset to add a new one.");
            return;
        }

        if (!isExpiredDateValid()) {
            return;
        }

        if (!hasSufficientStock()) {
            return;
        }

        try {

            Production production = new Production();
            production.setProductId(product.getProductId());
            production.setProductionDate(dateProduction.getValue().toString());
            production.setExpiredDate(dateExpired.getValue() != null ? dateExpired.getValue().toString() : null);
            production.setQuantityProduced(parseIntOrZero(txtQuantityProduced.getText()));
            production.setQuantitySold(0);
            production.setSellingPrice(parseDoubleOrZero(txtSellingPrice.getText()));
            production.setNote(txtNote.getText().trim());

            boolean success = productionDAO.addProduction(production);

            if (!success) {
                AlertUtil.error("Failed", "Failed to save production data.");
                return;
            }

            applyMaterialUsage(production.getProductionId());

            activityLogDAO.log(Session.getCurrentUserLabel(),
                    "Started Production Run #" + production.getProductionId(), "PRODUCTION");

            loadTable();
            clearForm();
            AlertUtil.info("Success", "Production data saved successfully.");

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data is invalid", "Quantity and price must be valid numbers.");
        }

    }

    /**
     * Memastikan Expired Date (kalau diisi) benar-benar berada setelah
     * Production Date. Dipanggil di sisi kode juga (bukan cuma lewat
     * dayCellFactory di kalender) supaya aturan ini tetap berlaku walau
     * datanya sempat diisi lewat cara lain.
     */
    private boolean isExpiredDateValid() {

        LocalDate productionDate = dateProduction.getValue();
        LocalDate expiredDate = dateExpired.getValue();

        if (expiredDate != null && productionDate != null && !expiredDate.isAfter(productionDate)) {
            AlertUtil.warning("Data is invalid",
                    "Expiration date must be after the production date.");
            return false;
        }

        return true;

    }

    /**
     * Mengecek apakah stok setiap bahan pada materialUsageList masih cukup
     * sebelum benar-benar dikurangi. Mencegah stok menjadi negatif.
     */
    private boolean hasSufficientStock() {

        for (MaterialUsageItem item : materialUsageList) {

            Material current = materialDAO.getMaterialById(item.getMaterialId());

            if (current == null) {
                AlertUtil.error("Failed", "Material \"" + item.getMaterialName() + "\" was not found.");
                return false;
            }

            if (current.getStock() < item.getQuantityUsed()) {
                AlertUtil.warning("Insufficient stock",
                        "Stock for \"" + item.getMaterialName() + "\" is only " + current.getStock()
                                + " " + current.getUnit() + ", but " + item.getQuantityUsed()
                                + " " + current.getUnit() + " is needed.");
                return false;
            }

        }

        return true;

    }

    @FXML
    private void updateProduction() {

        if (selectedProduction == null) {
            AlertUtil.warning("No selection", "Please select a production record from the table first.");
            return;
        }

        Product product = cmbProduct.getValue();

        if (product == null || dateProduction.getValue() == null) {
            AlertUtil.warning("Data is incomplete", "Product and production date are required.");
            return;
        }

        if (!isExpiredDateValid()) {
            return;
        }

        try {

            selectedProduction.setProductId(product.getProductId());
            selectedProduction.setProductionDate(dateProduction.getValue().toString());
            selectedProduction.setExpiredDate(dateExpired.getValue() != null ? dateExpired.getValue().toString() : null);
            selectedProduction.setQuantityProduced(parseIntOrZero(txtQuantityProduced.getText()));
            // quantitySold TIDAK diubah di sini - jumlah terjual sekarang
            // dikelola lewat halaman Sell yang terpisah.
            selectedProduction.setSellingPrice(parseDoubleOrZero(txtSellingPrice.getText()));
            selectedProduction.setNote(txtNote.getText().trim());

            boolean success = productionDAO.updateProduction(selectedProduction);

            if (success) {
                loadTable();
                clearForm();
                AlertUtil.info("Success", "Production data updated successfully.");
            } else {
                AlertUtil.error("Failed", "Failed to update production data.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data is invalid", "Quantity and price must be valid numbers.");
        }

    }

    @FXML
    private void deleteProduction() {

        if (selectedProduction == null) {
            AlertUtil.warning("No selection", "Please select a production record from the table first.");
            return;
        }

        boolean confirm = AlertUtil.confirm("Confirm Delete",
                "Delete this production record? Materials used will be returned to stock.");

        if (!confirm) {
            return;
        }

        int productionId = selectedProduction.getProductionId();

        // Kembalikan stok bahan yang dulu dipakai batch ini sebelum recordnya dihapus,
        // supaya stok material tetap konsisten (tidak "hilang" begitu saja).
        List<MaterialUsageItem> usedMaterials = productionMaterialDAO.getUsageByProduction(productionId);

        for (MaterialUsageItem item : usedMaterials) {
            materialDAO.updateStock(item.getMaterialId(), item.getQuantityUsed());
        }

        boolean success = productionDAO.deleteProduction(productionId);

        if (success) {
            productionMaterialDAO.deleteByProduction(productionId);
            loadTable();
            clearForm();
            AlertUtil.info("Success", "Production data deleted successfully.");
        } else {
            // Rollback pengembalian stok kalau ternyata production gagal dihapus
            for (MaterialUsageItem item : usedMaterials) {
                materialDAO.updateStock(item.getMaterialId(), -item.getQuantityUsed());
            }
            AlertUtil.error("Failed", "Failed to delete production data.");
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
        txtNote.clear();
        viewingExistingUsage = false;
        materialUsageList.clear();
        bomMaterialIds.clear();
        cmbMaterialUsed.setValue(null);
        txtMaterialQtyUsed.clear();

    }

    /**
     * Menyimpan setiap baris bahan yang dipakai ke tabel production_materials
     * (agar riwayatnya tidak hilang) dan mengurangi stok masing-masing bahan.
     */
    private void applyMaterialUsage(int productionId) {

        if (materialUsageList.isEmpty()) {
            return;
        }

        for (MaterialUsageItem item : materialUsageList) {

            productionMaterialDAO.insertUsage(productionId, item.getMaterialId(), item.getQuantityUsed());

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