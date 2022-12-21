package com.example.localization;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.localization.requests.RegisterRequest;
import com.example.localization.response.ErrorListClass;
import com.example.localization.response.ErrorPojoClass;
import com.example.localization.response.RegisterResponse;
import com.example.localization.response.TodosResponse;
import com.example.localization.services.AccountService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegisterActivity extends AppCompatActivity {

    public static String myToken;

    Button singUp;
    EditText username, mail, password, password2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        singUp = findViewById(R.id.buttonSignUpRegisterForm);
        username = findViewById(R.id.editTextUsernameRegisterForm);
        mail = findViewById(R.id.editTextEmailRegisterForm);
        password = findViewById(R.id.editTextTextPasswordRegisterForm);
        password2 = findViewById(R.id.editTextTextPasswordAgainRegisterForm);


    }

    public void singUp(View v){

        if(username.getText().toString().isEmpty()){
            username.setError("Field username can not be empty");
        }
        boolean resEmail = validateEmail();
        boolean resPass = validatePassword();
        boolean resComaprePasswords = password.getText().toString().equals(password2.getText().toString());

        if(!resComaprePasswords){
            password2.setError("No coniciden las passwords");
        }

        if(resEmail && resPass && resComaprePasswords){
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUserName(username.getText().toString());
            registerRequest.setEmail(mail.getText().toString());
            registerRequest.setPassword(password.getText().toString());

            registerAccount(registerRequest);
        }

    }

    public void registerAccount(RegisterRequest registerRequest){

        // esto el chabon lo hizo en la ApiClient como un metodo estatico
        AccountService accountService = ApiClient.getRetrofit().create(AccountService.class);

        Call<RegisterResponse> registerResponseCall = accountService.registerAccount(registerRequest);

        registerResponseCall.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                int statusCode = response.code();

                if (statusCode == 400) {
                    try {
                        Toast.makeText(RegisterActivity.this, response.errorBody().string(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else if(statusCode == 500){
                    try {
                        Toast.makeText(RegisterActivity.this, response.errorBody().string(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    /// codigo 200

                    // aca ya tengo el token dentro del objeto registerResponse
                    Toast.makeText(RegisterActivity.this, "SUCCESFULL", Toast.LENGTH_SHORT).show();
                    RegisterResponse registerResponse = response.body();

                    Intent i = new Intent(RegisterActivity.this, HomeActivity.class);

                    myToken = registerResponse.getToken();

                    i.putExtra("token", registerResponse.getToken());
                    i.putExtra("expire", registerResponse.getExpire());

                    startActivity(i);

                }


            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {

                System.out.println(t.getMessage());
                String messagge = t.getLocalizedMessage();

                Toast.makeText(RegisterActivity.this, messagge, Toast.LENGTH_LONG).show();
            }
        });


    }

    private boolean validateEmail() {
        String val = mail.getText().toString().trim();

        String checkEmail = "[a-zA-Z0-9._-]+@[a-z]+.+[a-z]+";
        if (val.isEmpty()) {
            mail.setError("Field can not be empty");
            return false;
        } else if (!val.matches(checkEmail)) {
            mail.setError("Invalid Email!");
            return false;
        } else {
            mail.setError(null);
            //mail.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword() {
        String val = password.getText().toString().trim();
        String checkPassword = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$";

        Pattern pattern = Pattern.compile(checkPassword);
        Matcher matcher = pattern.matcher(password.getText().toString());

        if (val.isEmpty()) {
            password.setError("Field can not be empty");
            return false;
        } else if (!matcher.matches()) {
            password.setError("Password should contain 6 characters!");
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }

}