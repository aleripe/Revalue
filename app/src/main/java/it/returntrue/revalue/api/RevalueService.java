package it.returntrue.revalue.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RevalueService {
    @POST("Account/ExternalLogin")
    Call<TokenModel> ExternalLogin(@Body ExternalTokenModel externalTokenModel);

    @POST("Account/UpdateGcmRegistrationId")
    Call<Void> UpdateGcmRegistrationId(@Body GcmTokenModel gcmTokenModel);

    @GET("Items/GetNearestItems/{latitude}/{longitude}/")
    Call<List<ItemModel>> GetNearestItems(@Path("latitude") double latitude,
                                          @Path("longitude") double longitude,
                                          @Query("filterTitle") String filterTitle,
                                          @Query("filterCategory") Integer filterCategory,
                                          @Query("filterDistance") Integer filterDistance);

    @GET("Items/GetFavoriteItems/{latitude}/{longitude}/")
    Call<List<ItemModel>> GetFavoriteItems(@Path("latitude") double latitude,
                                           @Path("longitude") double longitude,
                                           @Query("filterTitle") String filterTitle,
                                           @Query("filterCategory") Integer filterCategory,
                                           @Query("filterDistance") Integer filterDistance);

    @GET("Items/GetPersonalItems/{latitude}/{longitude}/")
    Call<List<ItemModel>> GetPersonalItems(@Path("latitude") double latitude,
                                           @Path("longitude") double longitude,
                                           @Query("filterTitle") String filterTitle,
                                           @Query("filterCategory") Integer filterCategory,
                                           @Query("filterDistance") Integer filterDistance);

    @GET("Items/GetItem/{latitude}/{longitude}/{id}")
    Call<ItemModel> GetItem(@Path("latitude") double latitude,
                            @Path("longitude") double longitude,
                            @Path("id") long id);

    @POST("Items/InsertItem")
    Call<Void> InsertItem(@Body ItemModel itemModel);

    @GET("Items/SetItemAsRevalued/{id}")
    Call<Void> SetItemAsRevalued(@Path("id") int id);

    @POST("Messages/SendMessage")
    Call<Void> SendMessage(@Body MessageModel messageModel);

    @GET("Items/AddFavorite/{id}")
    Call<Void> AddFavorite(@Path("id") int id);

    @GET("Items/RemoveFavorite/{id}")
    Call<Void> RemoveFavorite(@Path("id") int id);

    @GET("Items/GetAllCategories")
    Call<List<CategoryModel>> GetAllCategories();
}