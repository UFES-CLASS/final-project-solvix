package com.stockin.model;

public class MaterialUsageItem {

    private final int materialId;
    private final String materialName;
    private final String unit;
    private final int quantityUsed;

    public MaterialUsageItem(int materialId, String materialName, String unit, int quantityUsed) {
        this.materialId = materialId;
        this.materialName = materialName;
        this.unit = unit;
        this.quantityUsed = quantityUsed;
    }

    public int getMaterialId() {
        return materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public String getUnit() {
        return unit;
    }

    public int getQuantityUsed() {
        return quantityUsed;
    }

    public String getQuantityDisplay() {
        return unit != null && !unit.isEmpty()
                ? quantityUsed + " " + unit
                : String.valueOf(quantityUsed);
    }

}
