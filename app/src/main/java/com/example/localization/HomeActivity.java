package com.example.localization;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    public static String myToken;

    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent i = getIntent();
        token = i.getStringExtra("token");
        myToken = i.getStringExtra("token");

        System.out.println(token);



    }

    public void goToTrackingActivity(View v){

        Intent i = new Intent(HomeActivity.this, MainActivity.class);
        i.putExtra("token", token);
        startActivity(i);

    }

    public void goToTrackingHttpRequestsActivity(View v){

        Intent i = new Intent(HomeActivity.this, TrackingHttpRequests.class);
        i.putExtra("token", token);
        startActivity(i);

    }

    public void sendMessagge(View v){
        Intent i = new Intent(HomeActivity.this, TestSocket.class);
        startActivity(i);
    }
}