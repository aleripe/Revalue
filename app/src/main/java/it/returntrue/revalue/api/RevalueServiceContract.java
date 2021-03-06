/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.api;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Provides a contract for all API calls
 * */
public interface RevalueServiceContract {
    @POST("accounts/login/")
    Call<TokenModel> ExternalLogin(@Body ExternalTokenModel externalTokenModel);

    @POST("accounts/update/")
    Call<Void> UpdateGcmToken(@Body FcmTokenModel gcmTokenModel);

    @GET("categories/list/")
    Call<List<CategoryModel>> GetAllCategories();

    @GET("items/list/nearest/{latitude}/{longitude}/")
    Call<List<ItemModel>> GetNearestItems(@Path("latitude") double latitude,
                                          @Path("longitude") double longitude,
                                          @Query("filterTitle") String filterTitle,
                                          @Query("filterCategory") Integer filterCategory,
                                          @Query("filterDistance") Integer filterDistance);

    @GET("items/list/favorite/{latitude}/{longitude}/")
    Call<List<ItemModel>> GetFavoriteItems(@Path("latitude") double latitude,
                                           @Path("longitude") double longitude,
                                           @Query("filterTitle") String filterTitle,
                                           @Query("filterCategory") Integer filterCategory,
                                           @Query("filterDistance") Integer filterDistance);

    @GET("items/list/personal/{latitude}/{longitude}/")
    Call<List<ItemModel>> GetPersonalItems(@Path("latitude") double latitude,
                                           @Path("longitude") double longitude,
                                           @Query("filterTitle") String filterTitle,
                                           @Query("filterCategory") Integer filterCategory,
                                           @Query("filterDistance") Integer filterDistance);

    @GET("items/single/{id}/{latitude}/{longitude}/")
    Call<ItemModel> GetItem(@Path("id") int id,
                            @Path("latitude") double latitude,
                            @Path("longitude") double longitude);

    @POST("items/create")
    Call<Void> InsertItem(@Body ItemModel itemModel);

    @POST("items/single/{id}/revalue/")
    Call<Void> SetItemAsRevalued(@Path("id") int id);

    @POST("items/single/{id}/remove/")
    Call<Void> SetItemAsRemoved(@Path("id") int id);

    @POST("items/single/{id}/star/")
    Call<Void> AddFavoriteItem(@Path("id") int id);

    @POST("items/single/{id}/unstar/")
    Call<Void> RemoveFavoriteItem(@Path("id") int id);

    @POST("messages/send")
    Call<MessageModel> SendMessage(@Body MessageModel messageModel);

    @POST("users/list/ids")
    Call<List<UserModel>> GetUsersByIds(@Body ArrayList<Integer> ids);
}