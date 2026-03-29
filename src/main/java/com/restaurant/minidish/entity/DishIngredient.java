package com.restaurant.minidish.entity;

public class DishIngredient {

    private int id;
    private Ingredient ingredient;
    private double quantityRequired;
    private UnitTypeEnum unit;

    public DishIngredient() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Ingredient getIngredient() { return ingredient; }
    public void setIngredient(Ingredient ingredient) { this.ingredient = ingredient; }

    public double getQuantityRequired() { return quantityRequired; }
    public void setQuantityRequired(double quantityRequired) { this.quantityRequired = quantityRequired; }

    public UnitTypeEnum getUnit() { return unit; }
    public void setUnit(UnitTypeEnum unit) { this.unit = unit; }
}