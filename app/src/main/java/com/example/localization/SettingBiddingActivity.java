package com.example.localization;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

public class SettingBiddingActivity extends AppCompatActivity {

    HubConnection hubConnection;
    String token;

    EditText et_price;
    EditText et_time;
    EditText et_product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        token = HomeActivity.myToken;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_bidding);

        et_price = findViewById(R.id.openingBinding);
        et_time = findViewById(R.id.limitNumber);
        et_product = findViewById(R.id.product);

        hubConnection = HubConnectionBuilder.create("http://35.239.225.98:443/hubs/bids")
                .withHeader("Authorization", "Bearer " + token)
                .build();

        hubConnection.start();
        Toast.makeText(this, "Conexion exitosa", Toast.LENGTH_SHORT).show();

//        hubConnection.send("JoinGroup");

        hubConnection.on("NewUser", (msg) -> {
            System.out.println("Message: "+(msg));
        }, String.class);

        hubConnection.on("StartBid", (message) -> {
            System.out.println("Message: "+(message));
        }, String.class);
    }

    public void onDestroy() {

        super.onDestroy();
        hubConnection.stop();
        Toast.makeText(this, "Te desconectaste chau...", Toast.LENGTH_SHORT).show();
    }


    public void starBidding(View v){
        hubConnection.send("JoinGroup");
        //faltan validaciones de los edit text
        NewBid newBid = new NewBid();
        newBid.setPrice(et_price.getText().toString());
        newBid.setMinutes(et_time.getText().toString());
        hubConnection.send("startBidding", newBid);

        Intent i = new Intent(this, BiddingStatusActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("et_price", et_price.getText().toString());
        bundle.putString("et_time", et_time.getText().toString());
        bundle.putString("et_product", et_product.getText().toString());
        i.putExtras(bundle);

        startActivity(i);
    }
}