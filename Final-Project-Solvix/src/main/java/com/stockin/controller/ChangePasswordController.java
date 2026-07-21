package com.stockin.controller;

import com.stockin.dao.UserDAO;
import com.stockin.model.User;
import com.stockin.util.AlertUtil;
import com.stockin.util.PasswordUtil;
import com.stockin.util.Session;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ChangePasswordController {

    @FXML
    private PasswordField txtCurrentPassword;

    @FXML
    private PasswordField txtNewPassword;

    @FXML
    private PasswordField txtConfirmPassword;

    private final UserDAO userDAO = new UserDAO();

    private static final int MIN_PASSWORD_LENGTH = 5;

    @FXML
    private void savePassword() {

        User currentUser = Session.getCurrentUser();

        if (currentUser == null) {
            AlertUtil.error("Failed", "No active session. Please log in again.");
            return;
        }

        String currentPassword = txtCurrentPassword.getText();
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            AlertUtil.warning("Data is incomplete", "Please fill in all password fields.");
            return;
        }

        if (!PasswordUtil.verify(currentPassword, currentUser.getPassword())) {
            AlertUtil.warning("Incorrect password", "Your current password is incorrect.");
            return;
        }

        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            AlertUtil.warning("Password too short",
                    "New password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            AlertUtil.warning("Password does not match", "New password and confirmation do not match.");
            return;
        }

        if (newPassword.equals(currentPassword)) {
            AlertUtil.warning("Invalid password", "New password must be different from the current password.");
            return;
        }

        boolean success = userDAO.updatePassword(currentUser.getUserId(), newPassword);

        if (!success) {
            AlertUtil.error("Failed", "Failed to update password.");
            return;
        }

        // Sinkronkan hash password yang baru ke Session, supaya kalau user
        // membuka dialog ini lagi di sesi yang sama, verifikasi current
        // password memakai hash yang terbaru (bukan hash lama yang basi).
        User refreshed = userDAO.findByUsername(currentUser.getUsername());

        if (refreshed != null) {
            Session.setCurrentUser(refreshed);
        }

        AlertUtil.info("Success", "Password changed successfully.");

        closeWindow();

    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtCurrentPassword.getScene().getWindow();
        stage.close();
    }

}
