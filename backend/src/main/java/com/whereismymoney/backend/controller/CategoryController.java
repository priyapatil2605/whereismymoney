package com.whereismymoney.backend.controller;

import com.whereismymoney.backend.entity.Category;
import com.whereismymoney.backend.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(
            @RequestBody Category category) {
        return ResponseEntity.ok(
                categoryService.createCategory(category));
    }

    @GetMapping
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(
                categoryService.getAllCategories());
    }
}