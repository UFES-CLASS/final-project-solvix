package com.stockin.model;

public class Production {

    private int productionId;
    private int productId;
    private String productionDate;  
    private String expiredDate;      
    private int quantityProduced;
    private int quantitySold;
    private double sellingPrice;
    private String note;

    private String productName;
    private String productImage;

    public Production() {
    }

    public Production(int productionId, int productId, String productionDate, String expiredDate,
                       int quantityProduced, int quantitySold, double sellingPrice, String note) {
        this.productionId = productionId;
        this.productId = productId;
        this.productionDate = productionDate;
        this.expiredDate = expiredDate;
        this.quantityProduced = quantityProduced;
        this.quantitySold = quantitySold;
        this.sellingPrice = sellingPrice;
        this.note = note;
    }

    public int getProductionId() {
        return productionId;
    }

    public void setProductionId(int productionId) {
        this.productionId = productionId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(String productionDate) {
        this.productionDate = productionDate;
    }

    public String getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(String expiredDate) {
        this.expiredDate = expiredDate;
    }

    public int getQuantityProduced() {
        return quantityProduced;
    }

    public void setQuantityProduced(int quantityProduced) {
        this.quantityProduced = quantityProduced;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getRevenue() {
        return quantitySold * sellingPrice;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

}
