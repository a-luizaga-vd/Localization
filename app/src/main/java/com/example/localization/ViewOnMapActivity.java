package com.example.localization;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.localization.response.LocationResponse;
import com.example.localization.services.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewOnMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    EditText editTextMetters;

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 5000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_on_map);
        editTextMetters = findViewById(R.id.editTextNumberDecimalMetters);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(MyBackgroundService.isRunning){
            //LatLng myLocation = new LatLng(Double.parseDouble(MyBackgroundService.latitude), Double.parseDouble(MyBackgroundService.longitude));

            //mMap.addMarker(new MarkerOptions().position(MyBackgroundService.myLocation).title("Marker in My location"));
            mMap.addMarker(MyBackgroundService.markerOptions.icon(BitmapDescriptorFactory
                    .defaultMarker(210.0F)));
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MyBackgroundService.loc, 15.0f), 10000, null);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MyBackgroundService.myLocation, 10));
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
        else{
            LatLng SanJusto = new LatLng(-34.68668029836901, -58.563553532921745);
            mMap.addMarker(new MarkerOptions()
                    .position(SanJusto)
                    .title("Marker in San Justo"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SanJusto,10));
        }

    }

    public void viewOtherWithMetters(View v){

        if(editTextMetters.getText().toString().isEmpty()){
            editTextMetters.setError("Empty field");
        }
        else{

            handler.postDelayed(runnable = new Runnable() {
                public void run() {
                    //mMap.clear();

                    handler.postDelayed(runnable, delay);
                    //Toast.makeText(ViewOnMapActivity.this, "This method is run every 10 seconds", Toast.LENGTH_SHORT).show();

                    // Tratar de obtener el Get api/location

                    LocationService locationService = ApiClient.getRetrofit().create(LocationService.class);

                    String token = HomeActivity.myToken;
                    String bearerToken = "Bearer "+ token;

                    Call<ArrayList<LocationResponse>> locationResponseCall = locationService.getLocation(bearerToken);

                    locationResponseCall.enqueue(new Callback<ArrayList<LocationResponse>>() {
                        @Override
                        public void onResponse(Call<ArrayList<LocationResponse>> call, Response<ArrayList<LocationResponse>> response) {
                            int statusCode = response.code();
                            System.out.println("Status Code:"+statusCode);

                            ArrayList<LocationResponse> locationResponse;
                            if(statusCode == 200){
                                locationResponse = response.body();

                                int size =locationResponse.size();

                                if(size >= 2){

                                    int i=0, indexMyLocation;
                                    while(i<size && !locationResponse.get(i).getUserName().equalsIgnoreCase(HomeActivity.username)){
                                        i++;
                                    }
                                    indexMyLocation = i;

                                    Location startPoint=new Location("locationA");
                                    startPoint.setLatitude(Double.parseDouble(locationResponse.get(indexMyLocation).getLatitud()));
                                    startPoint.setLongitude(Double.parseDouble(locationResponse.get(indexMyLocation).getLogitud()));

                                    double lat, lng;
                                    mMap.clear();
                                    for(int j=0; j<size;j++){
                                        if(j != indexMyLocation){
                                            Location endPoint=new Location("locationB");
                                            endPoint.setLatitude(Double.parseDouble(locationResponse.get(j).getLatitud()));
                                            endPoint.setLongitude(Double.parseDouble(locationResponse.get(j).getLogitud()));

                                            double distance=startPoint.distanceTo(endPoint)/1000;// en km

                                            if((distance)<Double.parseDouble(editTextMetters.getText().toString())){

                                                /**Toast.makeText(getApplicationContext(), "Distance: "+ distance, Toast.LENGTH_SHORT).show();*/

                                                // lo marco en el mapa
                                                lat = Double.parseDouble(locationResponse.get(j).getLatitud());
                                                lng = Double.parseDouble(locationResponse.get(j).getLogitud());
                                                LatLng loc = new LatLng(lat, lng);
                                                mMap.addMarker(new MarkerOptions()
                                                        .position(loc)
                                                        .title(locationResponse.get(j).getUserName())
                                                        .icon(BitmapDescriptorFactory.defaultMarker(Float.parseFloat(locationResponse.get(j).getString1()))));
                                                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,10));
                                            }
                                            else{
                                                Toast.makeText(getApplicationContext(), "Not users nears", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ArrayList<LocationResponse>> call, Throwable t) {

                        }
                    });
                }
            }, delay);



        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); //stop handler when activity not visible super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); //stop handler when activity not visible super.onPause();
    }

    public void goToSettingBiddingActivity(View v) {
        Intent intent = new Intent( this, SettingBiddingActivity.class);
        startActivity(intent);
    }

}