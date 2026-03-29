package com.restaurant.minidish.entity;

import java.time.Instant;

public class StockMovement {

    private int id;
    private StockValue value;
    private MovementTypeEnum type;
    private Instant creationDatetime;

    public StockMovement() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public StockValue getValue() { return value; }
    public void setValue(StockValue value) { this.value = value; }

    public MovementTypeEnum getType() { return type; }
    public void setType(MovementTypeEnum type) { this.type = type; }

    public Instant getCreationDatetime() { return creationDatetime; }
    public void setCreationDatetime(Instant creationDatetime) { this.creationDatetime = creationDatetime; }
}