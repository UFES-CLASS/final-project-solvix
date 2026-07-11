package com.stockin.controller;

import com.stockin.dao.ActivityLogDAO;
import com.stockin.dao.UserDAO;
import com.stockin.model.User;
import com.stockin.util.Session;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private ComboBox<String> cmbRole;

    @FXML
    private Label lblStatus;

    private final UserDAO userDAO = new UserDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    @FXML
    public void initialize() {

        cmbRole.setItems(FXCollections.observableArrayList(
                "OWNER",
                "STAFF"
        ));

    }

    @FXML
    private void login() {

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String role = cmbRole.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            lblStatus.setText("All fields must be completed.");
            return;
        }

        User user = userDAO.authenticate(username, password, role);

        if (user == null) {
            lblStatus.setText("The username, password, or role is incorrect.");
            return;
        }

        Session.setCurrentUser(user);

        activityLogDAO.log(Session.getCurrentUserLabel(), "Logged in", "LOGIN");

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Dashboard.fxml"));

            Parent root = loader.load();

            DashboardController dashboardController = loader.getController();
            dashboardController.setLoggedInUser(user);

            Stage stage = (Stage) txtUsername.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("StockIn Dashboard");
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
            lblStatus.setText("An error occurred while opening the dashboard: " + e.getMessage());

        }

    }
}
