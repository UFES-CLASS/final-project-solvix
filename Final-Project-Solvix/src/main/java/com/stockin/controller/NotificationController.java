package com.stockin.controller;

import com.stockin.dao.NotificationDAO;
import com.stockin.model.Notification;
import com.stockin.util.AlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class NotificationController {

    @FXML
    private Label lblUnreadCount;

    @FXML
    private TableView<Notification> tableNotification;

    @FXML
    private TableColumn<Notification, Integer> colId;

    @FXML
    private TableColumn<Notification, String> colMaterial;

    @FXML
    private TableColumn<Notification, String> colMessage;

    @FXML
    private TableColumn<Notification, String> colCreatedAt;

    @FXML
    private TableColumn<Notification, Boolean> colStatus;

    private final NotificationDAO notificationDAO = new NotificationDAO();

    private final ObservableList<Notification> notificationList = FXCollections.observableArrayList();

    private Notification selectedNotification;

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("notificationId"));
        colMaterial.setCellValueFactory(new PropertyValueFactory<>("materialName"));
        colMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        colStatus.setCellValueFactory(new PropertyValueFactory<>("read"));
        colStatus.setCellFactory(col -> new TableCell<Notification, Boolean>() {
            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : (value ? "Have been read" : "Not read yet"));
            }
        });

        tableNotification.setRowFactory(tv -> new TableRow<Notification>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else if (!item.isRead()) {
                    setStyle("-fx-background-color:#FEF3C7; -fx-font-weight:bold;");
                } else {
                    setStyle("");
                }
            }
        });

        tableNotification.setItems(notificationList);

        tableNotification.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedNotification = newVal;
        });

        loadTable();

    }

    private void loadTable() {

        notificationList.setAll(notificationDAO.getAllNotifications());
        lblUnreadCount.setText(notificationDAO.countUnread() + " unread notifications");

    }

    @FXML
    private void markAsRead() {

        if (selectedNotification == null) {
            AlertUtil.warning("No selection", "Please select a notification from the table first.");
            return;
        }

        notificationDAO.markAsRead(selectedNotification.getNotificationId());
        loadTable();

    }

    @FXML
    private void markAllAsRead() {

        notificationDAO.markAllAsRead();
        loadTable();

    }

    @FXML
    private void deleteNotification() {

        if (selectedNotification == null) {
            AlertUtil.warning("No selection", "Please select a notification from the table first.");
            return;
        }

        boolean confirm = AlertUtil.confirm("Confirm Delete", "Delete this notification?");

        if (!confirm) {
            return;
        }

        notificationDAO.deleteNotification(selectedNotification.getNotificationId());
        loadTable();

    }

    @FXML
    private void refresh() {
        loadTable();
    }

}
