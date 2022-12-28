package com.example.localization;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

public class SettingBiddingActivity extends AppCompatActivity {

    HubConnection hubConnection;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        token = HomeActivity.myToken;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_bidding);

        hubConnection = HubConnectionBuilder.create("http://35.239.225.98:443/hubs/")
                .withHeader("Authorization", "Bearer " + token)
                .build();

        hubConnection.start();
        Toast.makeText(this, "Conexion exitosa", Toast.LENGTH_SHORT).show();


    }

    public void onDestroy() {

        super.onDestroy();
    }


}