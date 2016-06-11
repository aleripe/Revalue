package it.returntrue.revalue.events;

import it.returntrue.revalue.api.ItemModel;

public class InsertItemEvent {
    public static class OnStart {
        private ItemModel mItemModel;

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