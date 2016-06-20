/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.events;

import java.util.List;
import it.returntrue.revalue.api.CategoryModel;

/**
 * Represents a data bus event for GetCategories API call
 * */
@SuppressWarnings("ALL")
public class GetCategoriesEvent {
    public static class OnStart {
        public OnStart() { }
    }

    public static class OnSuccess {
        private final List<CategoryModel> mCategories;

        public OnSuccess(List<CategoryModel> categories) {
            mCategories = categories;
        }

        public List<CategoryModel> getCategories() {
            return mCategories;
        }
    }

    public static class OnFailure {
        public OnFailure() { }
    }
}