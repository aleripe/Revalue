/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.events;

import it.returntrue.revalue.api.ItemModel;

/**
 * Represents a data bus event for InsertItem API call
 * */
@SuppressWarnings("ALL")
public class InsertItemEvent {
    public static class OnStart {
        private final ItemModel mItemModel;

        public OnStart(ItemModel itemModel) {
            mItemModel = itemModel;
        }

        public ItemModel getItemModel() {
            return mItemModel;
        }
    }

    public static class OnSuccess {
        public OnSuccess() { }
    }

    public static class OnFailure {
        public OnFailure() { }
    }
}