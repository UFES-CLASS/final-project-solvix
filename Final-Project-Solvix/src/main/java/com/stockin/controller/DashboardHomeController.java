package com.stockin.controller;

import java.time.LocalDate;
import java.util.List;

import com.stockin.dao.IncomingMaterialDAO;
import com.stockin.dao.MaterialDAO;
import com.stockin.dao.ProductDAO;
import com.stockin.model.Material;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

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
                    "No low stock items."));

        } else {

            activityList.setItems(FXCollections.observableArrayList(
                    lowStock.stream()
                            .map(m -> "Low Stock Item: " + m.getMaterialName()
                                    + " (" + m.getStock() + " " + m.getUnit()
                                    + ", minimum " + m.getMinimumStock() + ")")
                            .toList()));

        }

    }

}
