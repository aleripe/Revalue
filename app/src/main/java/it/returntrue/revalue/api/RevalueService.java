package it.returntrue.revalue.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RevalueService {
    @POST("Account/ExternalLogin")
    Call<TokenModel> ExternalLogin(@Body ExternalTokenModel externalTokenModel);

    @GET("Items/GetNearestItems/{latitude}/{longitude}/{page}")
    Call<List<ItemModel>> GetNearestItems(@Path("latitude") double latitude,
                                          @Path("longitude") double longitude,
                                          @Path("page") int page);

    @GET("Items/GetItemById/{id}")
    Call<ItemModel> GetItemById(@Path("id") int id);

    @GET("Items/InsertItem")
    Call InsertUser(@Body ItemModel itemModel);

    @GET("Items/GetAllCategories")
    Call<List<CategoryModel>> GetAllCategories();
}