package it.returntrue.revalue.api;

import android.content.Context;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import it.returntrue.revalue.R;
import it.returntrue.revalue.events.AddFavoriteItemEvent;
import it.returntrue.revalue.events.ExternalLoginEvent;
import it.returntrue.revalue.events.GetCategoriesEvent;
import it.returntrue.revalue.events.GetItemEvent;
import it.returntrue.revalue.events.GetItemsEvent;
import it.returntrue.revalue.events.RemoveFavoriteItemEvent;
import it.returntrue.revalue.events.InsertItemEvent;
import it.returntrue.revalue.events.SetItemAsRemovedEvent;
import it.returntrue.revalue.events.SetItemAsRevaluedEvent;
import it.returntrue.revalue.utilities.Constants;
import it.returntrue.revalue.utilities.NetworkUtilities;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RevalueService {
    private Context mContext;
    private RevalueServiceContract mServiceContract;
    private Bus mBus;

    public RevalueService(Context context, Bus bus, String token) {
        mContext = context;
        mServiceContract = RevalueServiceGenerator.createService(token);
        mBus = bus;
    }

    @Subscribe
    public void onExternalLogin(ExternalLoginEvent.OnStart onStart) {
        Call<TokenModel> call = mServiceContract.ExternalLogin(onStart.getExternalTokenModel());
        call.enqueue(new Callback<TokenModel>() {
            @Override
            public void onResponse(Call<TokenModel> call, Response<TokenModel> response) {
                if (response.isSuccessful()) {
                    mBus.post(new ExternalLoginEvent.OnSuccess(response.body()));
                } else {
                    mBus.post(new ExternalLoginEvent.OnFailure(
                            NetworkUtilities.parseError(mContext, response)));
                }
            }

            @Override
            public void onFailure(Call<TokenModel> call, Throwable t) {
                mBus.post(new ExternalLoginEvent.OnFailure(mContext.getString(R.string.call_failed)));
            }
        });
    }

    @Subscribe
    public void onGetAllCategories(GetCategoriesEvent.OnStart onStart) {
        Call<List<CategoryModel>> call = mServiceContract.GetAllCategories();

        call.enqueue(new Callback<List<CategoryModel>>() {
            @Override
            public void onResponse(Call<List<CategoryModel>> call, Response<List<CategoryModel>> response) {
                mBus.post(new GetCategoriesEvent.OnSuccess(response.body()));
            }

            @Override
            public void onFailure(Call<List<CategoryModel>> call, Throwable t) {
                mBus.post(new GetCategoriesEvent.OnFailure());
            }
        });
    }

    @Subscribe
    public void onGetItems(GetItemsEvent.OnStart onStart) {
        Call<List<ItemModel>> call;

        switch (onStart.getMode()) {
            case Constants.FAVORITE_ITEMS_MODE:
                call = mServiceContract.GetFavoriteItems(
                        onStart.getLatitude(),
                        onStart.getLongitude(),
                        onStart.getTitle(),
                        onStart.getCategory(),
                        onStart.getDistance());
                break;
            case Constants.PERSONAL_MOVIES_MODE:
                call = mServiceContract.GetPersonalItems(
                        onStart.getLatitude(),
                        onStart.getLongitude(),
                        onStart.getTitle(),
                        onStart.getCategory(),
                        onStart.getDistance());
                break;
            default:
                call = mServiceContract.GetNearestItems(
                        onStart.getLatitude(),
                        onStart.getLongitude(),
                        onStart.getTitle(),
                        onStart.getCategory(),
                        onStart.getDistance());
        }

        call.enqueue(new Callback<List<ItemModel>>() {
            @Override
            public void onResponse(Call<List<ItemModel>> call, Response<List<ItemModel>> response) {
                mBus.post(new GetItemsEvent.OnSuccess(response.body()));
            }

            @Override
            public void onFailure(Call<List<ItemModel>> call, Throwable t) {
                mBus.post(new GetItemsEvent.OnFailure());
            }
        });
    }

    @Subscribe
    public void onGetItem(GetItemEvent.OnStart onStart) {
        Call<ItemModel> call = mServiceContract.GetItem(
                onStart.getId(),
                onStart.getLatitude(),
                onStart.getLongitude());

        call.enqueue(new Callback<ItemModel>() {
            @Override
            public void onResponse(Call<ItemModel> call, Response<ItemModel> response) {
                mBus.post(new GetItemEvent.OnSuccess(response.body()));
            }

            @Override
            public void onFailure(Call<ItemModel> call, Throwable t) {
                mBus.post(new GetItemEvent.OnFailure());
            }
        });
    }

    @Subscribe
    public void onInsertItem(InsertItemEvent.OnStart onStart) {
        Call<Void> call = mServiceContract.InsertItem(onStart.getItemModel());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                mBus.post(new InsertItemEvent.OnSuccess());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mBus.post(new InsertItemEvent.OnFailure());
            }
        });
    }

    @Subscribe
    public void onAddFavoriteItem(AddFavoriteItemEvent.OnStart onStart) {
        Call<Void> call = mServiceContract.AddFavoriteItem(onStart.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                mBus.post(new AddFavoriteItemEvent.OnSuccess());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mBus.post(new AddFavoriteItemEvent.OnFailure());
            }
        });
    }

    @Subscribe
    public void onRemoveFavoriteItem(RemoveFavoriteItemEvent.OnStart onStart) {
        Call<Void> call = mServiceContract.RemoveFavoriteItem(onStart.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                mBus.post(new RemoveFavoriteItemEvent.OnSuccess());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mBus.post(new RemoveFavoriteItemEvent.OnFailure());
            }
        });
    }

    @Subscribe
    public void onSetItemAsRevalued(SetItemAsRevaluedEvent.OnStart onStart) {
        Call<Void> call = mServiceContract.SetItemAsRevalued(onStart.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                mBus.post(new SetItemAsRevaluedEvent.OnSuccess());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mBus.post(new SetItemAsRevaluedEvent.OnFailure());
            }
        });
    }

    @Subscribe
    public void onSetItemAsRemoved(SetItemAsRemovedEvent.OnStart onStart) {
        Call<Void> call = mServiceContract.SetItemAsRemoved(onStart.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                mBus.post(new SetItemAsRemovedEvent.OnSuccess());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mBus.post(new SetItemAsRemovedEvent.OnFailure());
            }
        });
    }
}