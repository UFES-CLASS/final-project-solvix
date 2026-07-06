package com.stockin.controller;

import com.stockin.dao.IncomingMaterialDAO;
import com.stockin.dao.MaterialDAO;
import com.stockin.dao.ProductDAO;
import com.stockin.model.Material;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.time.LocalDate;
import java.util.List;

public class DashboardHomeController {

    @FXML
    private Label lblMaterial;

    @FXML
    private Label lblProduct;

    @FXML
    private Label lblIncoming;

    @FXML
    private Label lblLowStock;

    @FXML
    private ListView<String> activityList;

    private final MaterialDAO materialDAO = new MaterialDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final IncomingMaterialDAO incomingMaterialDAO = new IncomingMaterialDAO();

    @FXML
    public void initialize() {

        lblMaterial.setText(String.valueOf(materialDAO.countAll()));
        lblProduct.setText(String.valueOf(productDAO.countAll()));
        lblIncoming.setText(String.valueOf(incomingMaterialDAO.countToday(LocalDate.now().toString())));

        List<Material> lowStock = materialDAO.getLowStockMaterials();
        lblLowStock.setText(String.valueOf(lowStock.size()));

        if (lowStock.isEmpty()) {

            activityList.setItems(FXCollections.observableArrayList(
                    "Tidak ada bahan dengan stok menipis saat ini."));

        } else {

            activityList.setItems(FXCollections.observableArrayList(
                    lowStock.stream()
                            .map(m -> "Stok menipis: " + m.getMaterialName()
                                    + " (" + m.getStock() + " " + m.getUnit()
                                    + ", minimum " + m.getMinimumStock() + ")")
                            .toList()));

        }

    }

}
