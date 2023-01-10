package com.example.localization;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.localization.bids.ActiveBids;
//import com.example.localization.bids.Bidup;
import com.example.localization.bids.Bidup;
import com.example.localization.response.LocationResponse;
import com.example.localization.services.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewOnMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    EditText editTextMetters;

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 5000;

    ArrayList<Marker> newMarkers;
    ArrayList<Marker> oldMarkers = new ArrayList<>();

    ArrayList<Marker> redMarkers;
    Map<String, Marker> hashMapRed = new HashMap<>();

    HubConnection hubConnection;
    String token;
    Button btJoinLeaveGroup;
    ArrayList<ActiveBids> listActiveBids = new ArrayList<>();

    Button bt10Mas;
    Button bt25Mas;
    String idSendBid;

    Bidup bidup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_on_map);
        editTextMetters = findViewById(R.id.editTextNumberDecimalMetters);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btJoinLeaveGroup = findViewById(R.id.buttonJoinLeave);
        bt10Mas = findViewById(R.id.button10Mas);
        bt25Mas = findViewById(R.id.button25Mas);

        /** Subastas */
        token = HomeActivity.myToken;

        hubConnection = HubConnectionBuilder.create("http://35.239.225.98:443/hubs/bids")
                .withHeader("Authorization", "Bearer " + token)
                .build();

//        hubConnection = HubConnectionBuilder.create("http://10.0.2.2:5004/hubs/bids")
//                .withHeader("Authorization", "Bearer "+token)
//                .build();

        hubConnection.start();
        Toast.makeText(this, "Conexion exitosa", Toast.LENGTH_SHORT).show();

        if(hubConnection.getConnectionState() == HubConnectionState.CONNECTED){
            hubConnection.send("JoinGroup");
        }

        hubConnection.on("NewUser", (msg) -> {
            Gson g = new Gson();

            Type listType = new TypeToken<ArrayList<ActiveBids>>() {}.getType();
            listActiveBids = g.fromJson(msg , listType);

            System.out.println("Message: "+(msg));
        }, String.class);

        hubConnection.on("StartBid", (message) -> {
            // Llega una subasta
            String[] messageSplit = message.split(",");

            String idSubasta = messageSplit[0].substring(3);
            String price = messageSplit[1].substring(7).trim();
            String time = messageSplit[2].substring(9).trim();
            String description = messageSplit[3].substring(14);
            String owner = messageSplit[4].substring(8);

            parseTime(time);
            //LocalDateTime myDateObj = LocalDateTime.of();

            ActiveBids activeBids = new ActiveBids();
            activeBids.setId(idSubasta);
            activeBids.setFinalPrice(price);
            activeBids.setDescription(description);
            activeBids.setUsername(owner);

            listActiveBids.add(activeBids);
        }, String.class);


        hubConnection.on("Bid", (message) -> {

            Log.e("Response Bid", message);

            // parsear response puja
            String [] messageSplit = message.split(",");
            if(messageSplit.length == 3){

                String username = messageSplit[0].substring(21); //creo que es el q va ganando, ponele, preguntar
                String price = messageSplit[1].substring(9);

                String [] idMalo = messageSplit[2].split(" ");
                String id = idMalo[3];

                //aca tendria que entrar al hashmapRed y setearle un nuevo objeto al marker (cambia noma el precio final)
                // primero busco en el araylist de subastas activas por username que mapee con el hashmapred.
                for(ActiveBids activeBids:listActiveBids){
                    if(activeBids.getId().equals(id)){
                        activeBids.setFinalPrice(price);

                        hashMapRed.get(activeBids.getUsername()).setTag(null);
                        hashMapRed.get(activeBids.getUsername()).setTag(activeBids);
                    }
                }
            }
            else{
                Log.e("Response Bid", "No hacer nada, por ahora");
            }

        }, String.class);

        hubConnection.on("BidError", (msg) -> {
            Log.e("Response Bid", msg);
        }, String.class);



        /**************************************/
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(MyBackgroundService.isRunning){
            //LatLng myLocation = new LatLng(Double.parseDouble(MyBackgroundService.latitude), Double.parseDouble(MyBackgroundService.longitude));

            //mMap.addMarker(new MarkerOptions().position(MyBackgroundService.myLocation).title("Marker in My location"));
            mMap.addMarker(MyBackgroundService.markerOptions.icon(BitmapDescriptorFactory
                    .defaultMarker(210.0F)));//.setTag("210.0F");
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

        // adding on click listener to marker of google maps.
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                /*String tag = marker.getTag().toString();
                if(tag.equals("0.0F")){
                    // on marker click we are getting the title of our marker
                    // which is clicked and displaying it in a toast message.
                    String markerName = marker.getTitle();
                    Toast.makeText(ViewOnMapActivity.this, "Clicked location is " + markerName +"Color:"+marker.getTag(), Toast.LENGTH_SHORT).show();
                }*/
                ActiveBids activeBids;

                if(marker.getTag()!=null){

                    bt10Mas.setVisibility(View.VISIBLE);
                    bt25Mas.setVisibility(View.VISIBLE);

                    // declaro un IdSubasta y cuando oprimo sobre los marcadores le seteo el id que tiene el marker al idSubasta
                    // entonces los botones ya le pujan a ese iD y fue

                    activeBids = (ActiveBids) marker.getTag();

                    idSendBid = activeBids.getId();

                    Toast.makeText(ViewOnMapActivity.this, "IdSubasta: " + activeBids.getId() +"\nFinal Price: "+activeBids.getFinalPrice(), Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                bt10Mas.setVisibility(View.INVISIBLE);
                bt25Mas.setVisibility(View.INVISIBLE);
            }
        });

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

                                    //mMap.clear();
                                    for(Marker marker:oldMarkers){
                                        marker.remove();
                                    }

                                    // creo de 0 el nuevo arrary de marcker con las nuevas ubicaciones
                                    newMarkers = new ArrayList<>();
                                    for(int j=0; j<size;j++){
                                        if(j != indexMyLocation){
                                            Location endPoint=new Location("locationB");
                                            endPoint.setLatitude(Double.parseDouble(locationResponse.get(j).getLatitud()));
                                            endPoint.setLongitude(Double.parseDouble(locationResponse.get(j).getLogitud()));

                                            double distance = startPoint.distanceTo(endPoint)/1000;// in km

                                            if((distance)<Double.parseDouble(editTextMetters.getText().toString())){

                                                String color = locationResponse.get(j).getString1();
                                                if(color.equals("0.0F")){ // si esta en rojo
                                                    if(!hashMapRed.containsKey(locationResponse.get(j).getUserName())){
                                                        // lo marco en el mapa
                                                        lat = Double.parseDouble(locationResponse.get(j).getLatitud());
                                                        lng = Double.parseDouble(locationResponse.get(j).getLogitud());
                                                        LatLng loc = new LatLng(lat, lng);
                                                        Marker marker = mMap.addMarker(new MarkerOptions()
                                                                .position(loc)
                                                                .title(locationResponse.get(j).getUserName())
                                                                .icon(BitmapDescriptorFactory.defaultMarker(Float.parseFloat(locationResponse.get(j).getString1()))));
                                                                //.icon(BitmapDescriptorFactory.defaultMarker((locationResponse.get(j).getString1()==null)?Float.parseFloat("210.0F"):Float.parseFloat(locationResponse.get(j).getString1()))));

                                                        // Si esta en rojo es xq inició una subasta, entonces ahora lo busco por userName (solo puede tener una subasta iniciada)
                                                        if(!listActiveBids.isEmpty()){
                                                            for (ActiveBids bids : listActiveBids){
                                                                if(bids.getUsername().equals(locationResponse.get(j).getUserName())){
                                                                    bids.setUsername(locationResponse.get(j).getUserName());
                                                                    marker.setTag(bids); // le
                                                                }
                                                            }
                                                        }

                                                        //marker.setTag(color);
                                                        hashMapRed.put(locationResponse.get(j).getUserName(), marker);
                                                    }
                                                }
                                                else{// esta en azul
                                                    // si esta en el hashmap de los markers rojos lo tengo que sacar xq ahora esta azul
                                                    if(hashMapRed.containsKey(locationResponse.get(j).getUserName())){
                                                        hashMapRed.get(locationResponse.get(j).getUserName()).remove();
                                                        hashMapRed.remove(locationResponse.get(j).getUserName());
                                                    }

                                                    // lo marco en el mapa
                                                    lat = Double.parseDouble(locationResponse.get(j).getLatitud());
                                                    lng = Double.parseDouble(locationResponse.get(j).getLogitud());
                                                    LatLng loc = new LatLng(lat, lng);
                                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                                            .position(loc)
                                                            .title(locationResponse.get(j).getUserName())
                                                            .icon(BitmapDescriptorFactory.defaultMarker(Float.parseFloat(locationResponse.get(j).getString1()))));
                                                            //.icon(BitmapDescriptorFactory.defaultMarker((locationResponse.get(j).getString1()!=null)?Float.parseFloat("210.0F"):Float.parseFloat(locationResponse.get(j).getString1()))));

                                                    //marker.setTag(color);
                                                    newMarkers.add(marker);
                                                }
                                            }
                                            else{
                                                Toast.makeText(getApplicationContext(), "Not users nears", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                    //termina el for
                                    oldMarkers.clear();;
                                    oldMarkers.addAll(newMarkers);

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
        //handler.removeCallbacks(runnable); //stop handler when activity not visible super.onPause();
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

    public void joinLeaveGroup(View v){

        if(btJoinLeaveGroup.getText().toString().equalsIgnoreCase("join")){
            if(hubConnection.getConnectionState() == HubConnectionState.CONNECTED){
                hubConnection.send("JoinGroup");
                btJoinLeaveGroup.setText("leave");
            }
            else{
                Toast.makeText(this, "You are not connected", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            if(btJoinLeaveGroup.getText().toString().equalsIgnoreCase("leave")){
                if(hubConnection.getConnectionState() == HubConnectionState.CONNECTED){
                    hubConnection.send("LeaveGroup");
                    btJoinLeaveGroup.setText("join");
                }
                else{
                    Toast.makeText(this, "You are not connected", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void sendBid10(View v){

        int amount = 10, priceToBid ;

        bidup = new Bidup();

        bidup.setId(idSendBid);
        bidup.setUsername(HomeActivity.username);

        for(ActiveBids activeBids:listActiveBids){
            if(activeBids.getId().equals(idSendBid)){
                priceToBid = Integer.valueOf(activeBids.getFinalPrice()) + amount;
                bidup.setPrice(String.valueOf(priceToBid));
            }
        }

        if(hubConnection.getConnectionState() == HubConnectionState.CONNECTED){
            hubConnection.send("SendBid", bidup);
            Toast.makeText(this, "You submitted a bid", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "You are not connected", Toast.LENGTH_SHORT).show();
        }


    }

    public void parseTime(String dateTimeString){
        String[] dateTimeSplit = dateTimeString.split(" ");

        String[] date = dateTimeSplit[0].split("/");
        int day = Integer.valueOf(date[0]);
        int month = Integer.valueOf(date[1]);
        int year = Integer.valueOf(date[2]);

        String[] time = dateTimeSplit[1].split(":");
        int hour = Integer.valueOf(time[0]);
        int minute = Integer.valueOf(time[1]);
        int second = Integer.valueOf(time[2]);

        LocalDateTime l =LocalDateTime.of(year, month, day,hour,minute,second);

        LocalDateTime now = LocalDateTime.now();

        Duration duration = Duration.between(now, l);

        int min = (int) (duration.getSeconds()/ 60);
        int sec =  (int) (duration.getSeconds() % 60);

        Log.e("Duration", ""+ min);
        Log.e("Duration: " , ""+ sec);


    }
}