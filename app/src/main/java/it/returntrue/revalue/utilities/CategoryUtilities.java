/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.utilities;

import java.util.List;

import it.returntrue.revalue.api.CategoryModel;

/**
 * Provides utilities to access category data
 * */
public class CategoryUtilities {
    public static int getCategoryPosition(List<CategoryModel> categories, Integer id) {
        if (id != null) {
            for (int i = 0; i < categories.size(); i++) {
                CategoryModel category = categories.get(i);
                if (category.Id == id) return i + 1;
            }
        }

        return 0;
    }

    public static String getCategoryName(List<CategoryModel> categories, Integer id) {
        if (id != null) {
            for (CategoryModel category : categories) {
                if (category.Id == id) {
                    return category.Name;
                }
            }
        }

        return null;
    }

    public static int getCategoryId(List<CategoryModel> categories, Integer position) {
        if (position != null && position > 0 && position <= categories.size()) {
            return categories.get(position - 1).Id;
        }

        return -1;
    }
}