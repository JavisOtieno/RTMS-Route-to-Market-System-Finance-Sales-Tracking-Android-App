package com.scg.sf;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ApiService {
    @GET("api/sales")
//    Call<MyResponse> getData(
    Call<ResponseBody> getData(
//            @Body RequestBody body,
//            @Query("param") String param
            @Header("Authorization") String authToken,  // Set the Authorization header dynamically
            @Header("Accept") String acceptHeader
    );
}
