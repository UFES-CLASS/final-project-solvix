package com.stockin.model;

public class Material {

    private int materialId;
    private String materialName;
    private String category;
    private String unit;
    private int stock;
    private int minimumStock;

    public Material() {
    }

    public Material(int materialId, String materialName, String category,
                    String unit, int stock, int minimumStock) {

        this.materialId = materialId;
        this.materialName = materialName;
        this.category = category;
        this.unit = unit;
        this.stock = stock;
        this.minimumStock = minimumStock;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(int minimumStock) {
        this.minimumStock = minimumStock;
    }

    @Override
    public String toString() {
        return materialName;
    }
}