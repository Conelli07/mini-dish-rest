package com.restaurant.minidish.controller;

import com.restaurant.minidish.entity.*;
import com.restaurant.minidish.repository.DishRepository;
import com.restaurant.minidish.repository.IngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dishes")
public class DishController {

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @GetMapping
    public ResponseEntity<?> getAllDishes() {
        try {
            List<Dish> dishes = dishRepository.findAll();
            return ResponseEntity.ok(dishes.stream().map(this::toMap).collect(Collectors.toList()));
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/ingredients")
    public ResponseEntity<?> updateDishIngredients(
            @PathVariable int id,
            @RequestBody(required = false) List<Map<String, Object>> body) {

        if (body == null) {
            return ResponseEntity.badRequest()
                    .body("Request body is required and must contain a list of ingredients.");
        }
        try {
            Optional<Dish> dishOpt = dishRepository.findById(id);
            if (dishOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Dish.id=" + id + " is not found");
            }

            List<Integer> validIds = new ArrayList<>();
            for (Map<String, Object> ingMap : body) {
                Object idObj = ingMap.get("id");
                if (idObj != null) {
                    int ingId = ((Number) idObj).intValue();
                    try {
                        if (ingredientRepository.findById(ingId).isPresent()) {
                            validIds.add(ingId);
                        }
                    } catch (SQLException ignored) {}
                }
            }

            dishRepository.updateIngredientLinks(id, validIds);
            Dish updated = dishRepository.findById(id).orElseThrow();
            return ResponseEntity.ok(toMap(updated));
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    private Map<String, Object> toMap(Dish d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("name", d.getName());
        m.put("dishType", d.getDishType());
        m.put("sellingPrice", d.getSellingPrice());
        m.put("ingredients", d.getIngredients().stream().map(i -> {
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("id", i.getId());
            im.put("name", i.getName());
            im.put("category", i.getCategory());
            im.put("price", i.getPrice());
            return im;
        }).collect(Collectors.toList()));
        return m;
    }
}