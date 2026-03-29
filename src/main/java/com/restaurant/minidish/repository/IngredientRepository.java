package com.restaurant.minidish.repository;

import com.restaurant.minidish.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class IngredientRepository {

    @Autowired
    private DataSource dataSource;

    public List<Ingredient> findAll(int page, int size) throws SQLException {
        List<Ingredient> list = new ArrayList<>();
        String sql = "SELECT id, name, price, category FROM ingredient ORDER BY id LIMIT ? OFFSET ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, size);
            ps.setInt(2, (page - 1) * size);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapIngredient(rs));
        }
        return list;
    }

    public Optional<Ingredient> findById(int id) throws SQLException {
        String sql = "SELECT id, name, price, category FROM ingredient WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Ingredient ingredient = mapIngredient(rs);
                ingredient.setStockMovementList(findStockMovements(conn, id));
                return Optional.of(ingredient);
            }
        }
        return Optional.empty();
    }

    public List<Ingredient> findByCriteria(String ingredientName, CategoryEnum category,
                                           String dishName, int page, int size) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT i.id, i.name, i.price, i.category FROM ingredient i");
        List<Object> params = new ArrayList<>();

        if (dishName != null) {
            sql.append(" JOIN dish_ingredient di ON i.id = di.id_ingredient")
                    .append(" JOIN dish d ON di.id_dish = d.id");
        }
        sql.append(" WHERE 1=1");

        if (ingredientName != null) {
            sql.append(" AND i.name ILIKE ?");
            params.add("%" + ingredientName + "%");
        }
        if (category != null) {
            sql.append(" AND i.category::text = ?");
            params.add(category.name());
        }
        if (dishName != null) {
            sql.append(" AND d.name ILIKE ?");
            params.add("%" + dishName + "%");
        }
        sql.append(" ORDER BY i.id LIMIT ? OFFSET ?");
        params.add(size);
        params.add((page - 1) * size);

        List<Ingredient> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapIngredient(rs));
        }
        return list;
    }

    public List<Ingredient> saveAll(List<Ingredient> ingredients) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (Ingredient ing : ingredients) {
                    String check = "SELECT 1 FROM ingredient WHERE LOWER(name) = LOWER(?)";
                    try (PreparedStatement ps = conn.prepareStatement(check)) {
                        ps.setString(1, ing.getName());
                        if (ps.executeQuery().next()) {
                            conn.rollback();
                            throw new RuntimeException("Ingredient already exists: " + ing.getName());
                        }
                    }
                }
                List<Ingredient> saved = new ArrayList<>();
                for (Ingredient ing : ingredients) {
                    String insert = "INSERT INTO ingredient (name, price, category) " +
                            "VALUES (?, ?, ?::ingredient_category) RETURNING id";
                    try (PreparedStatement ps = conn.prepareStatement(insert)) {
                        ps.setString(1, ing.getName());
                        ps.setDouble(2, ing.getPrice());
                        ps.setString(3, ing.getCategory().name());
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) ing.setId(rs.getInt("id"));
                        saved.add(ing);
                    }
                }
                conn.commit();
                return saved;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Ingredient saveIngredient(Ingredient ingredient) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (ingredient.getId() == 0) {
                    String insert = "INSERT INTO ingredient (name, price, category) " +
                            "VALUES (?, ?, ?::ingredient_category) RETURNING id";
                    try (PreparedStatement ps = conn.prepareStatement(insert)) {
                        ps.setString(1, ingredient.getName());
                        ps.setDouble(2, ingredient.getPrice());
                        ps.setString(3, ingredient.getCategory().name());
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) ingredient.setId(rs.getInt("id"));
                    }
                } else {
                    String update = "UPDATE ingredient SET name = ?, price = ?, category = ?::ingredient_category WHERE id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(update)) {
                        ps.setString(1, ingredient.getName());
                        ps.setDouble(2, ingredient.getPrice());
                        ps.setString(3, ingredient.getCategory().name());
                        ps.setInt(4, ingredient.getId());
                        ps.executeUpdate();
                    }
                }
                if (ingredient.getStockMovementList() != null) {
                    for (StockMovement sm : ingredient.getStockMovementList()) {
                        String upsert =
                                "INSERT INTO stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime) " +
                                        "VALUES (COALESCE(NULLIF(?,0), nextval('stock_movement_id_seq')), ?, ?, ?::mouvement_type, ?::unit_type, ?) " +
                                        "ON CONFLICT (id) DO NOTHING";
                        try (PreparedStatement ps = conn.prepareStatement(upsert)) {
                            ps.setInt(1, sm.getId());
                            ps.setInt(2, ingredient.getId());
                            ps.setDouble(3, sm.getValue().getQuantity());
                            ps.setString(4, sm.getType().name());
                            ps.setString(5, sm.getValue().getUnit().name());
                            ps.setTimestamp(6, Timestamp.from(sm.getCreationDatetime()));
                            ps.executeUpdate();
                        }
                    }
                }
                conn.commit();
                return ingredient;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private List<StockMovement> findStockMovements(Connection conn, int ingredientId) throws SQLException {
        List<StockMovement> movements = new ArrayList<>();
        String sql = "SELECT id, quantity, type, unit, creation_datetime FROM stock_movement " +
                "WHERE id_ingredient = ? ORDER BY creation_datetime";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ingredientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                StockMovement sm = new StockMovement();
                sm.setId(rs.getInt("id"));
                sm.setValue(new StockValue(
                        rs.getDouble("quantity"),
                        UnitTypeEnum.valueOf(rs.getString("unit"))
                ));
                sm.setType(MovementTypeEnum.valueOf(rs.getString("type")));
                sm.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
                movements.add(sm);
            }
        }
        return movements;
    }

    private Ingredient mapIngredient(ResultSet rs) throws SQLException {
        Ingredient i = new Ingredient();
        i.setId(rs.getInt("id"));
        i.setName(rs.getString("name"));
        i.setPrice(rs.getDouble("price"));
        i.setCategory(CategoryEnum.valueOf(rs.getString("category")));
        return i;
    }
}