package com.stockin.controller;

import java.io.IOException;

import com.stockin.dao.ActivityLogDAO;
import com.stockin.dao.NotificationDAO;
import com.stockin.model.User;
import com.stockin.util.Session;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private StackPane contentArea;

    @FXML
    private VBox sidebarBox;

    @FXML
    private Button btnOpenSidebar;

    @FXML
    private Button btnCollapseSidebar;

    @FXML
    private MenuButton menuUser;

    @FXML
    private Button btnNotificationBell;

    @FXML
    private Label lblNotificationBadge;

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
    private Button btnReport;

    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

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

        menuUser.setText(user.getUsername().toUpperCase() + " (" + user.getRole() + ")");

        boolean canViewReport = user.canViewFinancialReport();
        btnReport.setVisible(canViewReport);
        btnReport.setManaged(canViewReport);

        refreshNotificationBadge();

    }

    /**
     * Menampilkan jumlah notifikasi belum dibaca sebagai badge merah kecil
     * di atas ikon lonceng. Badge disembunyikan total kalau tidak ada
     * notifikasi yang belum dibaca.
     */
    private void refreshNotificationBadge() {

        int unread = notificationDAO.countUnread();

        lblNotificationBadge.setText(unread > 99 ? "99+" : String.valueOf(unread));
        lblNotificationBadge.setVisible(unread > 0);
        lblNotificationBadge.setManaged(unread > 0);

    }

    private void loadPage(String page) {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + page));
            Parent root = loader.load();

            // Halaman Dashboard butuh referensi balik ke controller ini
            // supaya tombol "Restock Now" bisa berpindah ke halaman
            // Incoming Material.
            Object pageController = loader.getController();
            if (pageController instanceof DashboardHomeController) {
                ((DashboardHomeController) pageController).setDashboardController(this);
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

            // Refresh badge setiap kali pindah halaman, supaya kalau user
            // baru saja menandai notifikasi sebagai dibaca, badge-nya
            // langsung ter-update tanpa perlu logout/login ulang.
            refreshNotificationBadge();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Berpindah ke halaman Incoming Material. Dipakai oleh tombol
     * "Restock Now" pada panel Notifications di Dashboard.
     */
    public void goToIncoming() {
        loadPage("IncomingMaterial.fxml");
    }

    @FXML
    private void logout() {

        User user = Session.getCurrentUser();
        if (user != null) {
            activityLogDAO.log("Logout", user.getRole() != null ? user.getRole().toUpperCase() : "", "LOGOUT");
        }

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
    private void openChangePassword() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/ChangePassword.fxml"));

            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Change Password");
            dialog.getIcons().add(new Image(getClass().getResourceAsStream("/images/stockin_icon.png")));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(contentArea.getScene().getWindow());
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();

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
    private void openSell() {
        loadPage("Sell.fxml");
    }

    @FXML
    private void openNotification() {
        loadPage("Notification.fxml");
    }

    @FXML
    private void openReport() {
        loadPage("Report.fxml");
    }

    /**
     * Menutup / membuka sidebar. Saat ditutup, sidebar disembunyikan total
     * (visible + managed = false) supaya area konten melebar penuh, dan
     * tombol hamburger kecil muncul di topbar untuk membukanya lagi.
     */
    @FXML
    private void toggleSidebar() {

        boolean currentlyShowing = sidebarBox.isVisible();

        sidebarBox.setVisible(!currentlyShowing);
        sidebarBox.setManaged(!currentlyShowing);

        btnOpenSidebar.setVisible(currentlyShowing);
        btnOpenSidebar.setManaged(currentlyShowing);

    }

}
