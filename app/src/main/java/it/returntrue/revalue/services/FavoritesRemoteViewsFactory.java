package it.returntrue.revalue.services;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.returntrue.revalue.R;
import it.returntrue.revalue.RevalueApplication;
import it.returntrue.revalue.api.ItemModel;
import it.returntrue.revalue.api.RevalueServiceContract;
import it.returntrue.revalue.api.RevalueServiceGenerator;
import it.returntrue.revalue.preferences.SessionPreferences;
import retrofit2.Call;

class FavoritesRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final int MAX_ITEMS = 10;

    private final Context mContext;
    private List<ItemModel> mItems;

    public FavoritesRemoteViewsFactory(Context context) {
        mContext = context;
        mItems = new ArrayList<>();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onDataSetChanged() {
        SessionPreferences preferences = new SessionPreferences(mContext);
        RevalueApplication application = (RevalueApplication)mContext.getApplicationContext();

        String token = preferences.getToken();
        Double latestLatitude = application.getLocationLatitude();
        Double latestLongitude = application.getLocationLongitude();

        if (!TextUtils.isEmpty(token) && (latestLatitude != null) && (latestLongitude != null)) {
            RevalueServiceContract service = RevalueServiceGenerator.createService(preferences.getToken());
            Call<List<ItemModel>> call = service.GetNearestItems(latestLatitude, latestLongitude, null, null, null);

            try {
                List<ItemModel> items = call.execute().body();

                if (items != null && items.size() > 0) {
                    mItems = items;
                }
                else {
                    mItems.clear();
                }
            }
            catch (IOException e) {
                mItems.clear();
            }
        }
        else {
            mItems.clear();
        }
    }

    @Override
    public int getCount() {
        return Math.min(mItems.size(), MAX_ITEMS);
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (mItems.size() < position) {
            return null;
        }

        ItemModel item = mItems.get(position);

        final RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_favorite);

        // Intent to call activity
        Intent intent = new Intent();

        views.setTextViewText(R.id.text_title, item.Title);
        views.setTextViewText(R.id.text_location,
                String.format(mContext.getString(R.string.item_location), item.City, (int) (item.Distance / 1000)));
        views.setOnClickFillInIntent(R.id.widget_trigger, intent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(mContext.getPackageName(), R.layout.widget_favorite);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (mItems.size() > position) {
            return mItems.get(position).Id;
        }

        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}