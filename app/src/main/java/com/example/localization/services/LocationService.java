package com.example.localization.services;

import com.example.localization.requests.LocationRequest;
import com.example.localization.requests.LoginRequest;
import com.example.localization.requests.RegisterRequest;
import com.example.localization.response.LoginResponse;
import com.example.localization.response.RegisterResponse;
import com.example.localization.response.TodosResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.PUT;

public interface LocationService {

    //@PUT("location/api/update")
    @PUT("location/update")
    Call<ResponseBody> updateLocation(@Body LocationRequest locationRequest, @Header("Authorization") String authHeader);

}
