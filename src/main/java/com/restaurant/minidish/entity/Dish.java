package com.restaurant.minidish.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Dish {

    private int id;
    private String name;
    private DishTypeEnum dishType;
    private Double sellingPrice;
    private List<DishIngredient> dishIngredients = new ArrayList<>();

    public Dish() {}

    public List<Ingredient> getIngredients() {
        return dishIngredients.stream()
                .map(DishIngredient::getIngredient)
                .collect(Collectors.toList());
    }

    public Double getDishCost() {
        return dishIngredients.stream()
                .mapToDouble(di -> di.getIngredient().getPrice() * di.getQuantityRequired())
                .sum();
    }

    public Double getGrossMargin() {
        if (sellingPrice == null) {
            throw new RuntimeException("Dish \"" + name + "\" has no selling price defined.");
        }
        return sellingPrice - getDishCost();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DishTypeEnum getDishType() { return dishType; }
    public void setDishType(DishTypeEnum dishType) { this.dishType = dishType; }

    public Double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(Double sellingPrice) { this.sellingPrice = sellingPrice; }

    public List<DishIngredient> getDishIngredients() { return dishIngredients; }
    public void setDishIngredients(List<DishIngredient> dishIngredients) {
        this.dishIngredients = dishIngredients;
    }
}