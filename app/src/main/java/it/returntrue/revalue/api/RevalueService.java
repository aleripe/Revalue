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

    @GET("Items/GetNearestItems/{latitude}/{longitude}/{page}")
    Call<List<ItemModel>> GetNearestItems(@Path("latitude") double latitude,
                                          @Path("longitude") double longitude,
                                          @Path("page") int page,
                                          @Query("filterTitle") String filterTitle,
                                          @Query("filterCategory") Integer filterCategory,
                                          @Query("filterDistance") Integer filterDistance);

    @GET("Items/GetItem/{latitude}/{longitude}/{id}")
    Call<ItemModel> GetItem(@Path("latitude") double latitude,
                            @Path("longitude") double longitude,
                            @Path("id") long id);

    @GET("Items/InsertItem")
    Call InsertUser(@Body ItemModel itemModel);

    @GET("Items/GetAllCategories")
    Call<List<CategoryModel>> GetAllCategories();
}