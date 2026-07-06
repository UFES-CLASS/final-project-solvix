package com.stockin.model;

public class Product {

    private int productId;
    private String productName;
    private String productImage;   
    private String description;
    private double sellingPrice;
    private boolean isActive;

    public Product() {
    }

    public Product(int productId, String productName, String productImage, String description,
                    double sellingPrice, boolean isActive) {
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.description = description;
        this.sellingPrice = sellingPrice;
        this.isActive = isActive;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return productName;
    }

}
