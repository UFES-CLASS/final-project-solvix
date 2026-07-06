package com.stockin.model;

public class Notification {

    private int notificationId;
    private int materialId;
    private String message;
    private String createdAt;   
    private boolean isRead;

    private String materialName;

    public Notification() {
    }

    public Notification(int notificationId, int materialId, String message, String createdAt, boolean isRead) {
        this.notificationId = notificationId;
        this.materialId = materialId;
        this.message = message;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

}
