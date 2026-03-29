package com.restaurant.minidish.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Ingredient {

    private int id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private List<StockMovement> stockMovementList = new ArrayList<>();

    public Ingredient() {}

    public StockValue getStockValueAt(Instant t) {
        double quantity = 0;
        UnitTypeEnum unit = UnitTypeEnum.KG;
        for (StockMovement sm : stockMovementList) {
            if (!sm.getCreationDatetime().isAfter(t)) {
                if (sm.getType() == MovementTypeEnum.IN) {
                    quantity += sm.getValue().getQuantity();
                } else {
                    quantity -= sm.getValue().getQuantity();
                }
                unit = sm.getValue().getUnit();
            }
        }
        return new StockValue(quantity, unit);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public CategoryEnum getCategory() { return category; }
    public void setCategory(CategoryEnum category) { this.category = category; }

    public List<StockMovement> getStockMovementList() { return stockMovementList; }
    public void setStockMovementList(List<StockMovement> list) { this.stockMovementList = list; }
}