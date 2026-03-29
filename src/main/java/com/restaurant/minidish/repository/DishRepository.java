package com.restaurant.minidish.repository;

import com.restaurant.minidish.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DishRepository {

    @Autowired
    private DataSource dataSource;

    public List<Dish> findAll() throws SQLException {
        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT id, name, dish_type, selling_price FROM dish ORDER BY id";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Dish dish = mapDish(rs);
                dish.setDishIngredients(loadDishIngredients(conn, dish.getId()));
                dishes.add(dish);
            }
        }
        return dishes;
    }

    public Optional<Dish> findById(int id) throws SQLException {
        String sql = "SELECT id, name, dish_type, selling_price FROM dish WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Dish dish = mapDish(rs);
                dish.setDishIngredients(loadDishIngredients(conn, id));
                return Optional.of(dish);
            }
        }
        return Optional.empty();
    }

    public void updateIngredientLinks(int dishId, List<Integer> ingredientIds) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM dish_ingredient WHERE id_dish = ?")) {
                    ps.setInt(1, dishId);
                    ps.executeUpdate();
                }
                for (int ingId : ingredientIds) {
                    String insert = "INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit) " +
                            "VALUES (?, ?, 1.0, 'KG')";
                    try (PreparedStatement ps = conn.prepareStatement(insert)) {
                        ps.setInt(1, dishId);
                        ps.setInt(2, ingId);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private List<DishIngredient> loadDishIngredients(Connection conn, int dishId) throws SQLException {
        List<DishIngredient> list = new ArrayList<>();
        String sql = "SELECT di.id, di.quantity_required, di.unit, " +
                "i.id AS ing_id, i.name AS ing_name, i.price AS ing_price, i.category AS ing_category " +
                "FROM dish_ingredient di JOIN ingredient i ON di.id_ingredient = i.id " +
                "WHERE di.id_dish = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DishIngredient di = new DishIngredient();
                di.setId(rs.getInt("id"));
                di.setQuantityRequired(rs.getDouble("quantity_required"));
                di.setUnit(UnitTypeEnum.valueOf(rs.getString("unit")));
                Ingredient ing = new Ingredient();
                ing.setId(rs.getInt("ing_id"));
                ing.setName(rs.getString("ing_name"));
                ing.setPrice(rs.getDouble("ing_price"));
                ing.setCategory(CategoryEnum.valueOf(rs.getString("ing_category")));
                di.setIngredient(ing);
                list.add(di);
            }
        }
        return list;
    }

    private Dish mapDish(ResultSet rs) throws SQLException {
        Dish dish = new Dish();
        dish.setId(rs.getInt("id"));
        dish.setName(rs.getString("name"));
        dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
        double sp = rs.getDouble("selling_price");
        dish.setSellingPrice(rs.wasNull() ? null : sp);
        return dish;
    }
}