/*
 * Copyright (C) 2016 Alessandro Riperi
*/

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
import it.returntrue.revalue.events.GetUsersByIdsEvent;
import it.returntrue.revalue.events.InsertItemEvent;
import it.returntrue.revalue.events.RemoveFavoriteItemEvent;
import it.returntrue.revalue.events.SendMessageEvent;
import it.returntrue.revalue.events.SetItemAsRemovedEvent;
import it.returntrue.revalue.events.SetItemAsRevaluedEvent;
import it.returntrue.revalue.events.UpdateFcmTokenEvent;
import it.returntrue.revalue.utilities.Constants;
import it.returntrue.revalue.utilities.NetworkUtilities;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Encapsulate all calls to service using a bus provider for communication
 * */
@SuppressWarnings({"UnusedParameters", "unused"})
public class RevalueService {
    private final Context mContext;
    private final RevalueServiceContract mServiceContract;
    private final Bus mBus;

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
                if (response.isSuccessful()) {
                    mBus.post(new GetCategoriesEvent.OnSuccess(response.body()));
                }
                else {
                    mBus.post(new GetCategoriesEvent.OnFailure());
                }
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
                if (response.isSuccessful()) {
                    mBus.post(new GetItemsEvent.OnSuccess(response.body()));
                }
                else {
                    mBus.post(new GetItemsEvent.OnFailure());
                }
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
                if (response.isSuccessful()) {
                    mBus.post(new GetItemEvent.OnSuccess(response.body()));
                }
                else {
                    mBus.post(new GetItemEvent.OnFailure());
                }
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
                if (response.isSuccessful()) {
                    mBus.post(new InsertItemEvent.OnSuccess());
                }
                else {
                    mBus.post(new InsertItemEvent.OnFailure());
                }
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
                if (response.isSuccessful()) {
                    mBus.post(new AddFavoriteItemEvent.OnSuccess());
                }
                else {
                    mBus.post(new AddFavoriteItemEvent.OnFailure());
                }
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
                if (response.isSuccessful()) {
                    mBus.post(new RemoveFavoriteItemEvent.OnSuccess());
                }
                else {
                    mBus.post(new RemoveFavoriteItemEvent.OnFailure());
                }
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
                if (response.isSuccessful()) {
                    mBus.post(new SetItemAsRevaluedEvent.OnSuccess());
                }
                else {
                    mBus.post(new SetItemAsRevaluedEvent.OnFailure());
                }
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
                if (response.isSuccessful()) {
                    mBus.post(new SetItemAsRemovedEvent.OnSuccess());
                }
                else {
                    mBus.post(new SetItemAsRemovedEvent.OnFailure());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mBus.post(new SetItemAsRemovedEvent.OnFailure());
            }
        });
    }

    @Subscribe
    public void onGetUsersByIds(final GetUsersByIdsEvent.OnStart onStart) {
        Call<List<UserModel>> call = mServiceContract.GetUsersByIds(onStart.getUsersIds());

        call.enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful()) {
                    mBus.post(new GetUsersByIdsEvent.OnSuccess(response.body(), onStart.getCursor()));
                }
                else {
                    mBus.post(new GetUsersByIdsEvent.OnFailure());
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                mBus.post(new GetUsersByIdsEvent.OnFailure());
            }
        });
    }

    @Subscribe
    public void onUpdateGcmToken(final UpdateFcmTokenEvent.OnStart onStart) {
        Call<Void> call = mServiceContract.UpdateGcmToken(onStart.getTokenModel());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    mBus.post(new UpdateFcmTokenEvent.OnSuccess());
                }
                else {
                    mBus.post(new UpdateFcmTokenEvent.OnFailure());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mBus.post(new UpdateFcmTokenEvent.OnFailure());
            }
        });
    }

    @Subscribe
    public void onSendMessage(final SendMessageEvent.OnStart onStart) {
        Call<MessageModel> call = mServiceContract.SendMessage(onStart.getMessageModel());

        call.enqueue(new Callback<MessageModel>() {
            @Override
            public void onResponse(Call<MessageModel> call, Response<MessageModel> response) {
                if (response.isSuccessful()) {
                    mBus.post(new SendMessageEvent.OnSuccess(response.body()));
                }
                else {
                    mBus.post(new SendMessageEvent.OnFailure());
                }
            }

            @Override
            public void onFailure(Call<MessageModel> call, Throwable t) {
                mBus.post(new SendMessageEvent.OnFailure());
            }
        });
    }
}