package com.stockin.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.stockin.dao.NotificationDAO;
import com.stockin.model.Notification;
import com.stockin.util.AlertUtil;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class NotificationController {

    private static final DateTimeFormatter DB_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH);

    @FXML
    private Label lblUnreadBadge;

    @FXML
    private CheckBox chkSelectAll;

    @FXML
    private ComboBox<String> cmbFilterMaterial;

    @FXML
    private ComboBox<String> cmbFilterStatus;

    @FXML
    private VBox notificationContainer;

    private final NotificationDAO notificationDAO = new NotificationDAO();

    private List<Notification> allNotifications = new ArrayList<>();
    private List<Notification> currentFiltered = new ArrayList<>();
    private final Set<Integer> selectedIds = new LinkedHashSet<>();

    @FXML
    public void initialize() {

        cmbFilterStatus.setItems(FXCollections.observableArrayList("All Status", "Unread", "Read"));
        cmbFilterStatus.setValue("All Status");

        loadNotifications();

    }

    // =========================================================
    // LOADING / FILTERING
    // =========================================================

    private void loadNotifications() {

        allNotifications = notificationDAO.getAllNotifications();
        populateMaterialFilter();
        applyFilters();
        updateUnreadBadge();

    }

    private void populateMaterialFilter() {

        String current = cmbFilterMaterial.getValue();

        List<String> materials = new ArrayList<>();
        materials.add("All Materials");

        for (Notification n : allNotifications) {
            String name = n.getMaterialName();
            if (name != null && !materials.contains(name)) {
                materials.add(name);
            }
        }

        cmbFilterMaterial.setItems(FXCollections.observableArrayList(materials));

        if (current != null && materials.contains(current)) {
            cmbFilterMaterial.setValue(current);
        } else {
            cmbFilterMaterial.setValue("All Materials");
        }

    }

    @FXML
    private void applyFiltersAction() {
        applyFilters();
    }

    private void applyFilters() {

        String material = cmbFilterMaterial.getValue();
        String status = cmbFilterStatus.getValue();

        currentFiltered = new ArrayList<>();

        for (Notification n : allNotifications) {

            boolean materialMatch = material == null || material.equals("All Materials")
                    || material.equals(n.getMaterialName());

            boolean statusMatch = status == null || status.equals("All Status")
                    || (status.equals("Unread") && !n.isRead())
                    || (status.equals("Read") && n.isRead());

            if (materialMatch && statusMatch) {
                currentFiltered.add(n);
            }

        }

        renderList();

    }

    private void renderList() {

        notificationContainer.getChildren().clear();

        if (currentFiltered.isEmpty()) {
            Label empty = new Label("No notifications found.");
            empty.getStyleClass().add("empty-state-label");
            notificationContainer.getChildren().add(empty);
            return;
        }

        for (Notification n : currentFiltered) {
            notificationContainer.getChildren().add(buildNotificationRow(n));
        }

    }

    private void updateUnreadBadge() {

        int unread = notificationDAO.countUnread();
        lblUnreadBadge.setText(unread + " Unread Notification" + (unread == 1 ? "" : "s"));

    }

    // =========================================================
    // ROW BUILDING
    // =========================================================

    private VBox buildNotificationRow(Notification n) {

        CheckBox chk = new CheckBox();
        chk.setSelected(selectedIds.contains(n.getNotificationId()));
        chk.selectedProperty().addListener((obs, was, isNow) -> {
            if (isNow) {
                selectedIds.add(n.getNotificationId());
            } else {
                selectedIds.remove(n.getNotificationId());
            }
            syncSelectAllCheckbox();
        });

        Label idLabel = new Label(String.valueOf(n.getNotificationId()));
        idLabel.getStyleClass().add("notification-id-label");

        Label icon = new Label(iconFor(n.getMaterialName()));
        icon.getStyleClass().add("notification-icon-box");
        icon.setStyle("-fx-background-color:" + colorFor(n.getMaterialName()) + ";");

        Label title = new Label("Low Stock Alert: "
                + (n.getMaterialName() != null ? n.getMaterialName() : "Material"));
        title.getStyleClass().add("notification-item-title");

        Label message = new Label(n.getMessage());
        message.getStyleClass().add("notification-message-label");
        message.setWrapText(true);

        Label meta = new Label("ID: " + n.getNotificationId() + ", Created: " + formatDate(n.getCreatedAt()));
        meta.getStyleClass().add("notification-meta-label");

        VBox textCol = new VBox(6, title, message, meta);
        HBox.setHgrow(textCol, Priority.ALWAYS);

        Label badge = new Label(n.isRead() ? "Read" : "Unread");
        badge.getStyleClass().add(n.isRead() ? "notification-badge-read" : "notification-badge-unread");

        VBox actionCol = new VBox(10, badge);
        actionCol.setAlignment(Pos.TOP_RIGHT);
        actionCol.setPrefWidth(150);

        if (!n.isRead()) {
            Button markBtn = new Button("Mark as Read");
            markBtn.getStyleClass().add("btn-success");
            markBtn.setOnAction(e -> markSingleAsRead(n));
            actionCol.getChildren().add(markBtn);
        }

        HBox row = new HBox(16, chk, idLabel, icon, textCol, actionCol);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(row);
        card.getStyleClass().add("notification-row-card");

        return card;

    }

    private String iconFor(String materialName) {

        String n = materialName == null ? "" : materialName.toLowerCase(Locale.ROOT);

        if (n.contains("salmon") || n.contains("tuna") || n.contains("ikan")) {
            return "\uD83D\uDC1F";
        }
        if (n.contains("mentimun") || n.contains("cucumber") || n.contains("timun")) {
            return "\uD83E\uDD52";
        }
        if (n.contains("wasabi")) {
            return "\uD83C\uDF31";
        }
        if (n.contains("beras") || n.contains("rice") || n.contains("nasi")) {
            return "\uD83C\uDF5A";
        }
        if (n.contains("udang") || n.contains("shrimp")) {
            return "\uD83E\uDD90";
        }
        if (n.contains("telur") || n.contains("egg")) {
            return "\uD83E\uDD5A";
        }
        if (n.contains("ayam") || n.contains("chicken")) {
            return "\uD83C\uDF57";
        }
        if (n.contains("nori") || n.contains("seaweed") || n.contains("rumput laut")) {
            return "\uD83C\uDF59";
        }

        return "\uD83D\uDCE6";

    }

    private String colorFor(String materialName) {

        String n = materialName == null ? "" : materialName.toLowerCase(Locale.ROOT);

        if (n.contains("salmon") || n.contains("tuna") || n.contains("ikan")) {
            return "#FBD8C0";
        }
        if (n.contains("mentimun") || n.contains("cucumber") || n.contains("timun")) {
            return "#D7ECD2";
        }
        if (n.contains("wasabi")) {
            return "#DCEFC8";
        }
        if (n.contains("beras") || n.contains("rice") || n.contains("nasi")) {
            return "#F5F0DC";
        }
        if (n.contains("udang") || n.contains("shrimp")) {
            return "#FBE0D8";
        }

        return "#EDEDE7";

    }

    private String formatDate(String raw) {

        if (raw == null || raw.trim().isEmpty()) {
            return "-";
        }

        try {
            LocalDateTime dt = LocalDateTime.parse(raw.trim(), DB_FORMAT);
            return dt.format(DISPLAY_FORMAT) + " WIB";
        } catch (Exception e) {
            return raw;
        }

    }

    // =========================================================
    // SELECTION
    // =========================================================

    @FXML
    private void toggleSelectAll() {

        if (chkSelectAll.isSelected()) {
            for (Notification n : currentFiltered) {
                selectedIds.add(n.getNotificationId());
            }
        } else {
            for (Notification n : currentFiltered) {
                selectedIds.remove(n.getNotificationId());
            }
        }

        renderList();

    }

    private void syncSelectAllCheckbox() {

        boolean allSelected = !currentFiltered.isEmpty();

        for (Notification n : currentFiltered) {
            if (!selectedIds.contains(n.getNotificationId())) {
                allSelected = false;
                break;
            }
        }

        chkSelectAll.setSelected(allSelected);

    }

    // =========================================================
    // ACTIONS
    // =========================================================

    private void markSingleAsRead(Notification n) {
        notificationDAO.markAsRead(n.getNotificationId());
        loadNotifications();
    }

    @FXML
    private void markAsRead() {

        if (selectedIds.isEmpty()) {
            AlertUtil.warning("No selection", "Please select at least one notification first.");
            return;
        }

        for (int id : selectedIds) {
            notificationDAO.markAsRead(id);
        }

        selectedIds.clear();
        chkSelectAll.setSelected(false);
        loadNotifications();

    }

    @FXML
    private void deleteNotification() {

        if (selectedIds.isEmpty()) {
            AlertUtil.warning("No selection", "Please select at least one notification first.");
            return;
        }

        boolean confirm = AlertUtil.confirm("Confirm Delete",
                "Delete " + selectedIds.size() + " selected notification(s)?");

        if (!confirm) {
            return;
        }

        for (int id : selectedIds) {
            notificationDAO.deleteNotification(id);
        }

        selectedIds.clear();
        chkSelectAll.setSelected(false);
        loadNotifications();

    }

    @FXML
    private void refresh() {

        selectedIds.clear();
        chkSelectAll.setSelected(false);
        loadNotifications();

    }

    @FXML
    private void addNotificationChannel() {

        AlertUtil.info("Coming Soon",
                "Notification channel integrations (e.g. email or WhatsApp alerts) will be available in a future update.");

    }

}
