package com.restaurant.minidish.controller;

import com.restaurant.minidish.entity.*;
import com.restaurant.minidish.repository.IngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ingredients")
public class IngredientController {

    @Autowired
    private IngredientRepository ingredientRepository;

    @GetMapping
    public ResponseEntity<?> getAllIngredients(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String dishName) {
        try {
            int p = (page != null) ? page : 1;
            int s = (size != null) ? size : Integer.MAX_VALUE;

            List<Ingredient> ingredients;
            if (name != null || category != null || dishName != null) {
                CategoryEnum cat = null;
                if (category != null) {
                    cat = CategoryEnum.valueOf(category.toUpperCase());
                }
                ingredients = ingredientRepository.findByCriteria(name, cat, dishName, p, s);
            } else {
                ingredients = ingredientRepository.findAll(p, s);
            }
            return ResponseEntity.ok(ingredients.stream().map(this::toMap).collect(Collectors.toList()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid parameter: " + e.getMessage());
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIngredientById(@PathVariable int id) {
        try {
            Optional<Ingredient> opt = ingredientRepository.findById(id);
            if (opt.isEmpty()) {
                return ResponseEntity.status(404).body("Ingredient.id=" + id + " is not found");
            }
            return ResponseEntity.ok(toMap(opt.get()));
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<?> getStockValue(
            @PathVariable int id,
            @RequestParam(required = false) String at,
            @RequestParam(required = false) String unit) {

        if (at == null || unit == null) {
            return ResponseEntity.badRequest()
                    .body("Either mandatory query parameter `at` or `unit` is not provided.");
        }

        UnitTypeEnum unitEnum;
        try {
            unitEnum = UnitTypeEnum.valueOf(unit.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid unit. Accepted values: PCS, KG, L");
        }

        Instant instant;
        try {
            instant = Instant.parse(at);
        } catch (Exception e1) {
            try {
                instant = LocalDateTime.parse(at,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[:ss]"))
                        .atZone(ZoneOffset.UTC).toInstant();
            } catch (Exception e2) {
                return ResponseEntity.badRequest()
                        .body("Invalid 'at' format. Use ISO-8601 or 'yyyy-MM-dd HH:mm:ss'");
            }
        }

        try {
            Optional<Ingredient> opt = ingredientRepository.findById(id);
            if (opt.isEmpty()) {
                return ResponseEntity.status(404).body("Ingredient.id=" + id + " is not found");
            }
            StockValue stock = opt.get().getStockValueAt(instant);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("unit", unitEnum);
            response.put("quantity", stock.getQuantity());
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    private Map<String, Object> toMap(Ingredient i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("name", i.getName());
        m.put("category", i.getCategory());
        m.put("price", i.getPrice());
        return m;
    }
}