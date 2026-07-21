package com.stockin.controller;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.stockin.dao.MaterialDAO;
import com.stockin.dao.ProductDAO;
import com.stockin.dao.ProductMaterialDAO;
import com.stockin.model.Material;
import com.stockin.model.MaterialUsageItem;
import com.stockin.model.Product;
import com.stockin.util.AlertUtil;
import com.stockin.util.Session;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

public class ProductController {

    private static final int PAGE_SIZE = 6;

    @FXML
    private ImageView imgPreview;

    @FXML
    private TextField txtProductName;

    @FXML
    private TextField txtSku;

    @FXML
    private ComboBox<String> cmbCategory;

    @FXML
    private TextField txtSellingPrice;

    @FXML
    private TextArea txtDescription;

    @FXML
    private StackPane toggleTrack;

    @FXML
    private Circle toggleKnob;

    @FXML
    private Label lblStatusText;

    @FXML
    private TextField txtSearch;

    @FXML
    private ComboBox<String> cmbCategoryFilter;

    @FXML
    private TableView<Product> tableProduct;

    @FXML
    private TableColumn<Product, Integer> colId;

    @FXML
    private TableColumn<Product, String> colImage;

    @FXML
    private TableColumn<Product, String> colName;

    @FXML
    private TableColumn<Product, String> colSku;

    @FXML
    private TableColumn<Product, String> colDescription;

    @FXML
    private TableColumn<Product, Double> colPrice;

    @FXML
    private TableColumn<Product, Boolean> colActive;

    @FXML
    private TableColumn<Product, Void> colActions;

    @FXML
    private ComboBox<Material> cmbBomMaterial;

    @FXML
    private TextField txtBomQty;

    @FXML
    private TableView<MaterialUsageItem> tableBom;

    @FXML
    private TableColumn<MaterialUsageItem, String> colBomMaterial;

    @FXML
    private TableColumn<MaterialUsageItem, String> colBomQty;

    @FXML
    private TableColumn<MaterialUsageItem, String> colBomUnit;

    @FXML
    private Button btnFirstPage;

    @FXML
    private Button btnSaveProduct;

    @FXML
    private Button btnDeleteProduct;

    @FXML
    private Label lblFormHint;

    @FXML
    private Button btnPrevPage;

    @FXML
    private Button btnNextPage;

    @FXML
    private Button btnLastPage;

    private final ProductDAO productDAO = new ProductDAO();
    private final MaterialDAO materialDAO = new MaterialDAO();
    private final ProductMaterialDAO productMaterialDAO = new ProductMaterialDAO();

    // Semua produk (belum difilter), hasil filter search+kategori, dan
    // potongan halaman yang sedang tampil di tabel.
    private final ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private final ObservableList<Product> filteredProducts = FXCollections.observableArrayList();
    private final ObservableList<Product> pageProducts = FXCollections.observableArrayList();

    // Bill of Materials (resep) untuk produk yang sedang diedit di form.
    private final ObservableList<MaterialUsageItem> bomList = FXCollections.observableArrayList();

    private Product selectedProduct;
    private String selectedImagePath;
    private boolean activeStatus = true;
    private int currentPage = 0;

    private final DecimalFormat currencyFormat;

    public ProductController() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        currencyFormat = new DecimalFormat("#,##0.00", symbols);
    }

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colSku.setCellValueFactory(new PropertyValueFactory<>("sku"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        setupImageColumn();
        setupDescriptionColumn();
        setupPriceColumn();
        setupStatusColumn();
        setupActionsColumn();

        tableProduct.setItems(pageProducts);

        // Bill of Materials (BOM)
        colBomMaterial.setCellValueFactory(new PropertyValueFactory<>("materialName"));
        colBomQty.setCellValueFactory(new PropertyValueFactory<>("quantityUsed"));
        colBomUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        tableBom.setItems(bomList);
        cmbBomMaterial.setItems(FXCollections.observableArrayList(materialDAO.getAllMaterials()));

        tableProduct.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedProduct = newVal;
                fillForm(newVal);
            }
        });

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cmbCategoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Menambah produk baru & menghapus produk hanya boleh Owner.
        // Staff tetap bisa membuka produk yang sudah ada dan mengubah
        // detail/BOM-nya lewat tombol Save yang sama (mode "Update").
        btnDeleteProduct.setVisible(Session.canManageProducts());
        btnDeleteProduct.setManaged(Session.canManageProducts());

        setToggle(true);
        updateFormModeUi();

        loadTable();

    }

    /**
     * Menyesuaikan tampilan form (judul, hint, teks tombol Save) sesuai
     * mode saat ini - menambah produk baru (selectedProduct == null) atau
     * mengubah produk yang sudah dipilih - sekaligus mengunci tombol Save
     * kalau yang sedang login adalah Staff dan sedang dalam mode "tambah
     * baru" (Staff tidak boleh membuat produk baru dari nol).
     */
    private void updateFormModeUi() {

        boolean isNewProductMode = selectedProduct == null;
        boolean blockedForStaff = isNewProductMode && !Session.canManageProducts();

        if (isNewProductMode) {
            btnSaveProduct.setText("\u2714  Save Product");
            lblFormHint.setText(blockedForStaff
                    ? "Only the Owner can add a new product. Select an existing product on the right to update its details or recipe (BOM)."
                    : "Fill in the details below to add a new product, or select a row on the right to edit one.");
        } else {
            btnSaveProduct.setText("\u2714  Update Product");
            lblFormHint.setText("Editing \"" + selectedProduct.getProductName()
                    + "\". You can update its details and Bill of Materials (recipe) below.");
        }

        btnSaveProduct.setDisable(blockedForStaff);

    }

    // =========================================================
    // TABLE LOADING, FILTERING & PAGINATION
    // =========================================================

    private void loadTable() {

        // Kotak Search ikut dikosongkan setiap tabel di-refresh (habis
        // Save/Update/Delete). Sebelumnya kalau user sempat mencari
        // sesuatu lalu meng-update produk itu sampai namanya berubah,
        // hasil pencarian lama jadi tidak cocok lagi dengan data baru dan
        // tabel terlihat kosong, padahal datanya sudah tersimpan di
        // database - cuma "ketutup" filter pencarian yang basi.
        txtSearch.clear();

        allProducts.setAll(productDAO.getAllProducts());
        refreshCategoryOptions();
        applyFilters();

    }

    private void refreshCategoryOptions() {

        List<String> categories = productDAO.getAllCategories();

        String currentFormValue = cmbCategory.getValue();
        cmbCategory.setItems(FXCollections.observableArrayList(categories));
        if (currentFormValue != null) {
            cmbCategory.setValue(currentFormValue);
        }

        ObservableList<String> filterOptions = FXCollections.observableArrayList();
        filterOptions.add("Category: All");
        filterOptions.addAll(categories);
        cmbCategoryFilter.setItems(filterOptions);
        cmbCategoryFilter.setValue("Category: All");

    }

    private void applyFilters() {

        String query = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        String category = cmbCategoryFilter.getValue();

        List<Product> filtered = allProducts.stream()
                .filter(p -> query.isEmpty()
                        || (p.getProductName() != null && p.getProductName().toLowerCase().contains(query))
                        || (p.getSku() != null && p.getSku().toLowerCase().contains(query)))
                .filter(p -> category == null || category.equals("Category: All")
                        || category.equalsIgnoreCase(p.getCategory()))
                .collect(Collectors.toList());

        filteredProducts.setAll(filtered);
        currentPage = 0;
        renderPage();

    }

    private void renderPage() {

        int totalPages = Math.max(1, (int) Math.ceil(filteredProducts.size() / (double) PAGE_SIZE));

        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }

        int fromIndex = Math.min(currentPage * PAGE_SIZE, filteredProducts.size());
        int toIndex = Math.min(fromIndex + PAGE_SIZE, filteredProducts.size());

        pageProducts.setAll(filteredProducts.subList(fromIndex, toIndex));

        btnFirstPage.setDisable(currentPage == 0);
        btnPrevPage.setDisable(currentPage == 0);
        btnNextPage.setDisable(currentPage >= totalPages - 1);
        btnLastPage.setDisable(currentPage >= totalPages - 1);

    }

    @FXML
    private void goFirstPage() {
        currentPage = 0;
        renderPage();
    }

    @FXML
    private void goPrevPage() {
        currentPage--;
        renderPage();
    }

    @FXML
    private void goNextPage() {
        currentPage++;
        renderPage();
    }

    @FXML
    private void goLastPage() {
        currentPage = Integer.MAX_VALUE;
        renderPage();
    }

    // =========================================================
    // CUSTOM COLUMNS
    // =========================================================

    private void setupImageColumn() {

        colImage.setCellFactory(col -> new TableCell<Product, String>() {

            private final ImageView thumb = new ImageView();

            {
                thumb.setFitWidth(44);
                thumb.setFitHeight(44);
                thumb.setPreserveRatio(true);
                thumb.getStyleClass().add("table-thumb");
            }

            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                Product product = (Product) getTableRow().getItem();
                Image image = loadImage(product.getProductImage());

                if (image != null) {
                    thumb.setImage(image);
                    setGraphic(thumb);
                } else {
                    setGraphic(null);
                }

            }
        });

    }

    private void setupDescriptionColumn() {

        colDescription.setCellFactory(col -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                    return;
                }

                setText(value.length() > 60 ? value.substring(0, 57) + "..." : value);

            }
        });

    }

    private void setupPriceColumn() {

        colPrice.setCellFactory(col -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : formatCurrency(value));
            }
        });

    }

    private void setupStatusColumn() {

        colActive.setCellFactory(col -> new TableCell<Product, Boolean>() {

            private final Label dot = new Label("\u2713");

            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setGraphic(null);
                    return;
                }

                dot.getStyleClass().removeAll("status-dot-active", "status-dot-inactive");
                dot.getStyleClass().add(value ? "status-dot-active" : "status-dot-inactive");
                setGraphic(dot);
                setAlignment(Pos.CENTER);

            }
        });

    }

    private void setupActionsColumn() {

        colActions.setCellFactory(col -> new TableCell<Product, Void>() {

            private final Button btnEdit = new Button("\u270E");
            private final Button btnDelete = new Button("\uD83D\uDDD1");
            private final HBox box = new HBox(6, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().addAll("table-action-icon-btn", "table-action-icon-edit");
                btnDelete.getStyleClass().addAll("table-action-icon-btn", "table-action-icon-delete");
                box.setAlignment(Pos.CENTER);

                // Ikon hapus per-baris juga disembunyikan untuk Staff,
                // konsisten dengan tombol "Delete Product" di form (hanya
                // Owner yang boleh menghapus produk dari katalog).
                btnDelete.setVisible(Session.canManageProducts());
                btnDelete.setManaged(Session.canManageProducts());

                btnEdit.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    selectedProduct = product;
                    fillForm(product);
                    tableProduct.getSelectionModel().select(product);
                });

                btnDelete.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleDelete(product);
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

    private void fillForm(Product product) {

        txtProductName.setText(product.getProductName());
        txtSku.setText(product.getSku());
        cmbCategory.setValue(product.getCategory());
        txtSellingPrice.setText(formatCurrency(product.getSellingPrice()));
        txtDescription.setText(product.getDescription());
        setToggle(product.isActive());

        selectedImagePath = product.getProductImage();
        showPreview(selectedImagePath);

        bomList.setAll(productMaterialDAO.getBomByProduct(product.getProductId()));

        updateFormModeUi();

    }

    // =========================================================
    // BILL OF MATERIALS (BOM)
    // =========================================================

    @FXML
    private void addBomItem() {

        Material material = cmbBomMaterial.getSelectionModel().getSelectedItem();
        String qtyText = txtBomQty.getText().trim();

        if (material == null) {
            AlertUtil.warning("Data is incomplete", "Please select a material first.");
            return;
        }

        if (qtyText.isEmpty()) {
            AlertUtil.warning("Data is incomplete", "Please enter the quantity required per unit.");
            return;
        }

        try {

            int qty = Integer.parseInt(qtyText);

            if (qty <= 0) {
                AlertUtil.warning("Data is invalid", "Quantity required must be greater than 0.");
                return;
            }

            for (int i = 0; i < bomList.size(); i++) {

                MaterialUsageItem existing = bomList.get(i);

                if (existing.getMaterialId() == material.getMaterialId()) {
                    bomList.set(i, new MaterialUsageItem(
                            existing.getMaterialId(), existing.getMaterialName(), existing.getUnit(), qty));
                    txtBomQty.clear();
                    cmbBomMaterial.setValue(null);
                    return;
                }

            }

            bomList.add(new MaterialUsageItem(
                    material.getMaterialId(), material.getMaterialName(), material.getUnit(), qty));

            cmbBomMaterial.setValue(null);
            txtBomQty.clear();

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data is invalid", "Quantity required must be a valid number.");
        }

    }

    @FXML
    private void removeBomItem() {

        MaterialUsageItem selected = tableBom.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.warning("No selection", "Please select a material row from the recipe list first.");
            return;
        }

        bomList.remove(selected);

    }

    @FXML
    private void chooseImage() {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Product Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));

        File file = chooser.showOpenDialog(imgPreview.getScene().getWindow());

        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            showPreview(selectedImagePath);
        }

    }

    private void showPreview(String path) {

        Image image = loadImage(path);
        imgPreview.setImage(image);

    }

    private Image loadImage(String path) {

        try {

            if (path != null && !path.isEmpty()) {
                return new Image(new File(path).toURI().toString(), true);
            }

        } catch (Exception e) {
            return null;
        }

        return null;

    }

    @FXML
    private void toggleActiveStatus() {
        setToggle(!activeStatus);
    }

    private void setToggle(boolean active) {

        activeStatus = active;

        toggleTrack.getStyleClass().removeAll("switch-track-on", "switch-track-off");
        toggleTrack.getStyleClass().add(active ? "switch-track-on" : "switch-track-off");

        toggleKnob.setTranslateX(active ? 23 : 3);

        lblStatusText.setText(active ? "Active" : "Inactive");

    }

    @FXML
    private void saveProduct() {

        if (selectedProduct == null && !Session.canManageProducts()) {
            AlertUtil.warning("Not allowed",
                    "Only the Owner can add a new product. Select an existing product from the table "
                            + "to update its details or Bill of Materials (recipe) instead.");
            return;
        }

        String name = txtProductName.getText().trim();

        if (name.isEmpty()) {
            AlertUtil.warning("Data is incomplete", "Product name is required.");
            return;
        }

        try {

            double price = parseCurrency(txtSellingPrice.getText());

            if (selectedProduct != null) {
                updateExistingProduct(name, price);
            } else {
                insertNewProduct(name, price);
            }

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data is invalid", "Selling price must be a valid number.");
        }

    }

    private void insertNewProduct(String name, double price) {

        Product product = new Product();
        product.setProductName(name);
        product.setProductImage(selectedImagePath);
        product.setSku(txtSku.getText().trim());
        product.setCategory(cmbCategory.getValue() == null ? "" : cmbCategory.getValue().trim());
        product.setDescription(txtDescription.getText().trim());
        product.setSellingPrice(price);
        product.setActive(activeStatus);

        boolean success = productDAO.addProduct(product);

        if (success) {

            boolean bomSaved = productMaterialDAO.replaceBom(product.getProductId(), bomList);

            loadTable();
            clearForm();

            if (bomSaved) {
                AlertUtil.info("Success", "Product added successfully.");
            } else {
                AlertUtil.warning("Product saved, but recipe failed",
                        "The product was saved, but its Bill of Materials (recipe) failed to save. "
                                + "Please re-open this product and re-add the recipe.");
            }

        } else {
            AlertUtil.error("Failed", "Failed to add product.");
        }

    }

    private void updateExistingProduct(String name, double price) {

        selectedProduct.setProductName(name);
        selectedProduct.setProductImage(selectedImagePath);
        selectedProduct.setSku(txtSku.getText().trim());
        selectedProduct.setCategory(cmbCategory.getValue() == null ? "" : cmbCategory.getValue().trim());
        selectedProduct.setDescription(txtDescription.getText().trim());
        selectedProduct.setSellingPrice(price);
        selectedProduct.setActive(activeStatus);

        boolean success = productDAO.updateProduct(selectedProduct);

        if (success) {

            boolean bomSaved = productMaterialDAO.replaceBom(selectedProduct.getProductId(), bomList);

            loadTable();
            clearForm();

            if (bomSaved) {
                AlertUtil.info("Success", "Product updated successfully.");
            } else {
                AlertUtil.warning("Product saved, but recipe failed",
                        "The product was updated, but its Bill of Materials (recipe) failed to save. "
                                + "Please re-open this product and re-add the recipe.");
            }

        } else {
            AlertUtil.error("Failed", "Failed to update product.");
        }

    }

    @FXML
    private void deleteProduct() {

        if (!Session.canManageProducts()) {
            AlertUtil.warning("Not allowed", "Only the Owner can delete a product.");
            return;
        }

        if (selectedProduct == null) {
            AlertUtil.warning("No selection", "Please select a product from the table first.");
            return;
        }

        handleDelete(selectedProduct);

    }

    private void handleDelete(Product product) {

        if (!Session.canManageProducts()) {
            AlertUtil.warning("Not allowed", "Only the Owner can delete a product.");
            return;
        }

        boolean confirm = AlertUtil.confirm("Confirm Delete",
                "Delete product \"" + product.getProductName() + "\"?");

        if (!confirm) {
            return;
        }

        boolean success = productDAO.deleteProduct(product.getProductId());

        if (success) {
            loadTable();
            clearForm();
            AlertUtil.info("Success", "Product deleted successfully.");
        } else {
            AlertUtil.error("Failed", "Failed to delete product (it might be in use in other data).");
        }

    }

    @FXML
    private void clearForm() {

        selectedProduct = null;
        selectedImagePath = null;

        tableProduct.getSelectionModel().clearSelection();

        txtProductName.clear();
        txtSku.clear();
        cmbCategory.setValue(null);
        txtSellingPrice.clear();
        txtDescription.clear();
        setToggle(true);
        imgPreview.setImage(null);

        bomList.clear();
        cmbBomMaterial.setValue(null);
        txtBomQty.clear();

        updateFormModeUi();

    }

    // =========================================================
    // CURRENCY HELPERS
    // =========================================================

    private String formatCurrency(double value) {
        return "Rp " + currencyFormat.format(value);
    }

    private double parseCurrency(String text) {

        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        String cleaned = text.replace("Rp", "").replace(",", "").trim();

        return cleaned.isEmpty() ? 0 : Double.parseDouble(cleaned);

    }

}
