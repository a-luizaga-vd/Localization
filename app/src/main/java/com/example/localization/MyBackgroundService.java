package com.example.localization;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.localization.requests.LocationRequest;
import com.example.localization.services.LocationService;

import java.text.DecimalFormat;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBackgroundService extends Service implements LocationListener{
    String latitude;
    String longitude;
    String token;

    public static boolean isRunning = false;
    LocationManager locationManager;
    boolean isNetworkEnabled, isGPSEnabled;
    Notification notification;

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            createNotificationChanel();
        else startForeground(
                1,
                new Notification()
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChanel() {
        // Para que el servicio corra en segundo plano debemos realizar una notificiacion al usuario, de que este se va ejecutar en segundo plano
        // de esta manera con la notificacion, el sistema no te mata el servicio cuando la minimizamos o bloquiamos la pantalla, entonces creamos la notificacion


        // Primero necesitamos un canal de notificacion
        CharSequence name = "Location Channel";
        String description = "New channel de notification to...";

        int importance = NotificationManager.IMPORTANCE_NONE;

        NotificationChannel channel = new NotificationChannel("Location ID", name, importance);
        channel.setDescription(description);

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        // Creamos la notficacion vinculando el ID CANAL
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Location ID")
                //.setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Location Notification")
                .setContentText("The App is running in background to get location updates")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Obtnemos la notficiaion
        notification = builder.build();

        // Lanzamos el sercvicio en seguindo plano
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        System.out.println("NO PASAODASJDNASODNASLDNAS");
        DecimalFormat df = new DecimalFormat("#.##");
        latitude = String.valueOf(df.format(location.getLatitude()));
        longitude = String.valueOf(df.format(location.getLongitude()));



        //Log.e("GPStracker", String.valueOf(location.getLatitude())+", "+String.valueOf(location.getLongitude()));
        Log.e("GPStracker", latitude+", "+longitude);

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setLatitud(latitude);
        locationRequest.setLogitud(longitude);

        // Variables random
        locationRequest.setString1("Hola");
        locationRequest.setString2("Chau");
        locationRequest.setString3("Buenas");

        LocationService locationService = ApiClient.getRetrofit().create(LocationService.class);

        //String bearerToken = "Bearer "+LoginActivity.getMyToken();
        String bearerToken = "Bearer "+HomeActivity.myToken;

        Call<ResponseBody> stringResponseCall = locationService.updateLocation(locationRequest, bearerToken);

        stringResponseCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int statusCode = response.code();
                System.out.println("Status Code:"+statusCode);

                if(statusCode == 401){
                    Toast.makeText(getApplicationContext(), "Error Token, Unauthorized", Toast.LENGTH_LONG).show();
                }
                if(statusCode == 200){
                    // Ok....
                    //Toast.makeText(getApplicationContext(), "Location Update..", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println(t.getMessage());
                String messagge = t.getLocalizedMessage();
                Log.e("ERROR", messagge);
            }
        });
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        System.out.println("Status changued");
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        System.out.println("The provider " +provider+" is enabled");
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        System.out.println("The provider " +provider+" is enabled");
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        token = intent.getStringExtra("token");
        System.out.println("token servicio:"+token);

        isRunning = true;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // getting GPS status
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        if (!isGPSEnabled && !isNetworkEnabled) {
            Toast.makeText(this, "The providers are disabled, please enable the GPS sensor.", Toast.LENGTH_SHORT).show();
            this.onDestroy();
        }
        else {
            if (isNetworkEnabled) {
                System.out.println("Nertwork Provider is using..");
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);
            }

            if (isGPSEnabled) {
                System.out.println("GPS Provider is using..");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
            } /*else {
                System.out.println("The providers is disabled");
                Toast.makeText(this, "Please enable the GPS sensors", Toast.LENGTH_SHORT).show();
                this.onDestroy();
            }*/
            startForeground(2, notification);
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        System.out.println("The Service is destroyed");
        locationManager.removeUpdates(this);
        isRunning = false;

    }
}
