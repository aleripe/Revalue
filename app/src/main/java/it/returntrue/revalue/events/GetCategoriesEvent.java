package it.returntrue.revalue.events;

import java.util.List;
import it.returntrue.revalue.api.CategoryModel;

public class GetCategoriesEvent {
    public static class OnStart {
        public OnStart() { }
    }

    public static class OnSuccess {
        private List<CategoryModel> mCategories;

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