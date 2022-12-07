package com.example.localization.services;

import com.example.localization.requests.LoginRequest;
import com.example.localization.requests.RegisterRequest;
import com.example.localization.response.LoginResponse;
import com.example.localization.response.RegisterResponse;
import com.example.localization.response.TodosResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AccountService {

    @POST("account/register")
    Call<RegisterResponse> registerAccount(@Body RegisterRequest registerRequest);

    @POST("account/login")
    Call<LoginResponse> loginAccount(@Body LoginRequest loginRequest);

    @GET("/todos/1")
    public Call<TodosResponse> listTodos();
}
