package com.stockin.controller;

import java.io.IOException;

import com.stockin.model.User;
import com.stockin.util.Session;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Label lblUser;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnMaterial;

    @FXML
    private Button btnIncoming;

    @FXML
    private Button btnProduct;

    @FXML
    private Button btnProduction;

    @FXML
    private Button btnNotification;

    @FXML
    private Button btnReport;

    @FXML
    public void initialize() {

        applyUser(Session.getCurrentUser());

        openDashboard();

    }

    public void setLoggedInUser(User user) {
        applyUser(user);
    }

    private void applyUser(User user) {

        if (user == null) {
            return;
        }

        lblUser.setText(user.getUsername().toUpperCase() + " (" + user.getRole() + ")");

        boolean owner = user.isOwner();
        btnReport.setVisible(owner);
        btnReport.setManaged(owner);

    }

    private void loadPage(String page) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/" + page));

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {

        Session.clear();

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Login.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("StockIn - Login");
            stage.setMaximized(false);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void openDashboard() {
        loadPage("DashboardHome.fxml");
    }

    @FXML
    private void openMaterial() {
        loadPage("Material.fxml");
    }

    @FXML
    private void openIncoming() {
        loadPage("IncomingMaterial.fxml");
    }

    @FXML
    private void openProduct() {
        loadPage("Product.fxml");
    }

    @FXML
    private void openProduction() {
        loadPage("Production.fxml");
    }

    @FXML
    private void openNotification() {
        loadPage("Notification.fxml");
    }

    @FXML
    private void openReport() {
        loadPage("Report.fxml");
    }

}
