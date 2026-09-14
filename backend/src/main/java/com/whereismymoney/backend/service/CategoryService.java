package com.whereismymoney.backend.service;

import com.whereismymoney.backend.entity.Category;

import java.util.List;

public interface CategoryService {

    Category createCategory(Category category);

    List<Category> getAllCategories();
}