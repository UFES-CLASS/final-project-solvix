package com.stockin.model;

public class IncomingMaterial {

    private int incomingId;
    private int materialId;
    private String incomingDate;   
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private String supplier;
    private String note;

    private String materialName;
    private String unit;

    public IncomingMaterial() {
    }

    public IncomingMaterial(int incomingId, int materialId, String incomingDate, int quantity,
                             double unitPrice, double totalPrice, String supplier, String note) {
        this.incomingId = incomingId;
        this.materialId = materialId;
        this.incomingDate = incomingDate;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.supplier = supplier;
        this.note = note;
    }

    public int getIncomingId() {
        return incomingId;
    }

    public void setIncomingId(int incomingId) {
        this.incomingId = incomingId;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public String getIncomingDate() {
        return incomingDate;
    }

    public void setIncomingDate(String incomingDate) {
        this.incomingDate = incomingDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

}
