package com.stockin.controller;

import java.io.File;

import com.stockin.dao.ProductDAO;
import com.stockin.model.Product;
import com.stockin.util.AlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class ProductController {

    @FXML
    private ImageView imgPreview;

    @FXML
    private TextField txtProductName;

    @FXML
    private TextField txtSellingPrice;

    @FXML
    private TextField txtDescription;

    @FXML
    private CheckBox chkActive;

    @FXML
    private TableView<Product> tableProduct;

    @FXML
    private TableColumn<Product, Integer> colId;

    @FXML
    private TableColumn<Product, String> colName;

    @FXML
    private TableColumn<Product, String> colDescription;

    @FXML
    private TableColumn<Product, Double> colPrice;

    @FXML
    private TableColumn<Product, Boolean> colActive;

    private final ProductDAO productDAO = new ProductDAO();

    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    private Product selectedProduct;
    private String selectedImagePath;

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));

        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        colActive.setCellFactory(col -> new TableCell<Product, Boolean>() {
            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : (value ? "Aktif" : "Nonaktif"));
            }
        });

        tableProduct.setItems(productList);

        tableProduct.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedProduct = newVal;
                fillForm(newVal);
            }
        });

        loadTable();

    }

    private void loadTable() {
        productList.setAll(productDAO.getAllProducts());
    }

    private void fillForm(Product product) {

        txtProductName.setText(product.getProductName());
        txtSellingPrice.setText(String.valueOf(product.getSellingPrice()));
        txtDescription.setText(product.getDescription());
        chkActive.setSelected(product.isActive());

        selectedImagePath = product.getProductImage();
        showPreview(selectedImagePath);

    }

    @FXML
    private void chooseImage() {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Pilih Gambar Produk");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Gambar", "*.png", "*.jpg", "*.jpeg"));

        File file = chooser.showOpenDialog(imgPreview.getScene().getWindow());

        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            showPreview(selectedImagePath);
        }

    }

    private void showPreview(String path) {

        try {

            if (path != null && !path.isEmpty()) {
                imgPreview.setImage(new Image(new File(path).toURI().toString()));
            } else {
                imgPreview.setImage(null);
            }

        } catch (Exception e) {
            imgPreview.setImage(null);
        }

    }

    @FXML
    private void saveProduct() {

        String name = txtProductName.getText().trim();

        if (name.isEmpty()) {
            AlertUtil.warning("Data belum lengkap", "Nama produk wajib diisi.");
            return;
        }

        try {

            Product product = new Product();
            product.setProductName(name);
            product.setProductImage(selectedImagePath);
            product.setDescription(txtDescription.getText().trim());
            product.setSellingPrice(parseDoubleOrZero(txtSellingPrice.getText()));
            product.setActive(chkActive.isSelected());

            boolean success = productDAO.addProduct(product);

            if (success) {
                loadTable();
                clearForm();
                AlertUtil.info("Berhasil", "Produk berhasil ditambahkan.");
            } else {
                AlertUtil.error("Gagal", "Produk gagal ditambahkan.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data tidak valid", "Harga jual harus berupa angka.");
        }

    }

    @FXML
    private void updateProduct() {

        if (selectedProduct == null) {
            AlertUtil.warning("Belum ada pilihan", "Pilih produk pada tabel terlebih dahulu.");
            return;
        }

        try {

            selectedProduct.setProductName(txtProductName.getText().trim());
            selectedProduct.setProductImage(selectedImagePath);
            selectedProduct.setDescription(txtDescription.getText().trim());
            selectedProduct.setSellingPrice(parseDoubleOrZero(txtSellingPrice.getText()));
            selectedProduct.setActive(chkActive.isSelected());

            boolean success = productDAO.updateProduct(selectedProduct);

            if (success) {
                loadTable();
                clearForm();
                AlertUtil.info("Berhasil", "Produk berhasil diperbarui.");
            } else {
                AlertUtil.error("Gagal", "Produk gagal diperbarui.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.warning("Data tidak valid", "Harga jual harus berupa angka.");
        }

    }

    @FXML
    private void deleteProduct() {

        if (selectedProduct == null) {
            AlertUtil.warning("Belum ada pilihan", "Pilih produk pada tabel terlebih dahulu.");
            return;
        }

        boolean confirm = AlertUtil.confirm("Konfirmasi Hapus",
                "Hapus produk \"" + selectedProduct.getProductName() + "\"?");

        if (!confirm) {
            return;
        }

        boolean success = productDAO.deleteProduct(selectedProduct.getProductId());

        if (success) {
            loadTable();
            clearForm();
            AlertUtil.info("Berhasil", "Produk berhasil dihapus.");
        } else {
            AlertUtil.error("Gagal", "Produk gagal dihapus (mungkin masih dipakai di data produksi).");
        }

    }

    @FXML
    private void clearForm() {

        selectedProduct = null;
        selectedImagePath = null;

        tableProduct.getSelectionModel().clearSelection();

        txtProductName.clear();
        txtSellingPrice.clear();
        txtDescription.clear();
        chkActive.setSelected(true);
        imgPreview.setImage(null);

    }

    private double parseDoubleOrZero(String text) {

        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        return Double.parseDouble(text.trim());

    }

}
