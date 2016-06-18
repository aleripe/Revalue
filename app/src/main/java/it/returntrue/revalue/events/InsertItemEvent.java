package it.returntrue.revalue.events;

import it.returntrue.revalue.api.ItemModel;

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