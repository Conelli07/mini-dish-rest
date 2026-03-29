package com.restaurant.minidish.repository;

import com.restaurant.minidish.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StockRepository {

    @Autowired
    private DataSource dataSource;

    public List<StockMovement> findByIngredientId(int ingredientId) throws SQLException {
        List<StockMovement> list = new ArrayList<>();
        String sql = "SELECT id, quantity, type, unit, creation_datetime FROM stock_movement " +
                "WHERE id_ingredient = ? ORDER BY creation_datetime";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
                list.add(sm);
            }
        }
        return list;
    }
}