package com.example.localization;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.localization.requests.LoginRequest;
import com.example.localization.requests.RegisterRequest;
import com.example.localization.response.LoginResponse;
import com.example.localization.response.RegisterResponse;
import com.example.localization.services.AccountService;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    public static String myToken;

    public static String getMyToken() {
        return myToken;
    }

    EditText username;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.editTextUsernameLogin);
        password = findViewById(R.id.editTextPasswordLogin);


    }

    public void login(View v){
        String user = username.getText().toString();
        String pass = password.getText().toString();

        if(!user.isEmpty() && !pass.isEmpty()){
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUserName(user);
            loginRequest.setPassword(pass);

            postLogin(loginRequest);
        }
        else{
            Toast.makeText(LoginActivity.this, "Empty Fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void postLogin(LoginRequest loginRequest) {
        // esto el chabon lo hizo en la ApiClient como un metodo estatico
        AccountService accountService = ApiClient.getRetrofit().create(AccountService.class);

        Call<LoginResponse> loginResponseCall = accountService.loginAccount(loginRequest);

        loginResponseCall.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                int statusCode = response.code();

                if (statusCode == 400) {
                    try {
                        Toast.makeText(LoginActivity.this, response.errorBody().string(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else if(statusCode == 500){
                    try {
                        Toast.makeText(LoginActivity.this, response.errorBody().string(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    // Codigo 200

                    // aca ya tengo el token dentro del objeto loginResponse
                    Toast.makeText(LoginActivity.this, "SUCCESFULL", Toast.LENGTH_SHORT).show();

                    LoginResponse loginResponse = response.body();

                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);

                    myToken = loginResponse.getToken();
                    i.putExtra("token", loginResponse.getToken());
                    i.putExtra("expire", loginResponse.getExpire());
                    i.putExtra("username", username.getText().toString());

                    startActivity(i);

                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                System.out.println(t.getMessage());
                String messagge = t.getLocalizedMessage();

                Toast.makeText(LoginActivity.this, messagge, Toast.LENGTH_LONG).show();
            }
        });

    }

    public void goToRegisterForm(View v){
        Intent intent = new Intent(this, RegisterActivity.class);

        startActivity(intent);

    }
}