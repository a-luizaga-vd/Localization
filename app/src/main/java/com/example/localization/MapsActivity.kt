package com.example.localization

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.localization.response.LocationResponse
import com.example.localization.services.LocationService
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var mGoogleMap: GoogleMap
    var mapFrag: SupportMapFragment? = null
    lateinit var mLocationRequest: LocationRequest

    var listLocation : ArrayList<LocationResponse>? = null

    fun getArrayLocations(): ArrayList<LocationResponse>? {
        return listLocation
    }

    //    lateinit var locationService : LocationService
    var mLastLocation: Location? = null
    internal var mCurrLocationMarker: Marker? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                // Returns locations computed, ordered from oldest to newest.
                // The last location in the list is the newest
                val location = locationList.last()
                //esto mas que nada para ver info en el log
                Log.i(
                    "MapsActivity",
                    "Location: " + location.getLatitude() + " " + location.getLongitude()
                )
                mLastLocation = location
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker?.remove()
                }

                //pone el marcador actual
                getLocations()
                val locations = getArrayLocations()
                if (locations != null) {
                    Log.v("retrofit", locations.size.toString())
                }
                if (locations != null) {
                    for( i in 0 until locations.size){

//                        val user = locations[i].userName
//                        val lat  = locations[i].latitud
//                        val long = locations[i].logitud
                        val latLng = LatLng(
                            locations[i].latitud.toDouble(), locations[i].logitud.toDouble())
                        placeMarker(latLng, locations[i].userName )

                        Log.v("retrofit", "****************************")
                        Log.v("retrofit8", locations[i].userName.toString())
                        Log.v("retrofit", "****************************")
                    }
                }
//                val latLng = LatLng(location.latitude, location.longitude)
//                val markerOptions = MarkerOptions()
//                markerOptions.position(latLng)
//                markerOptions.title("Aca estoy en bici").snippet("hola!")
//                markerOptions.icon(
//                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
//                )
//                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions)
                //mueve la camara // probar otros zooms
//                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0F))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        supportActionBar?.title = "Mapa"

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapFrag = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFrag?.getMapAsync(this)

//        listLocation = null
//        getLocations()
    }


    public override fun onPause() {
        super.onPause()

//        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mGoogleMap.uiSettings.isZoomControlsEnabled = true

        mLocationRequest = LocationRequest.create()
        mLocationRequest.interval = 3000 // 120000 two minute interval
        mLocationRequest.fastestInterval = 3000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //ya tiene permisos
                mFusedLocationClient?.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    Looper.myLooper()
                )
                mGoogleMap.isMyLocationEnabled = true
            } else {
                //pedir permisos de locacion
                checkLocationPermission()
            }
        } else {
            mFusedLocationClient?.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.myLooper()
            )
            mGoogleMap.isMyLocationEnabled = true
        }
    }

    private fun getLocations() {
        val locationService = ApiClient.getRetrofit().create(LocationService::class.java)

        val bearerToken = "Bearer " + LoginActivity.getMyToken()
        val result = locationService.getLocation(bearerToken)

        result.enqueue(object : Callback<ArrayList<LocationResponse>> {

//            @Override
//            fun onResponse(call: Call<RegisterResponse?>?, response: Response<RegisterResponse?>?) {
//                val statusCode = response?.code();
//                val registerResponse: RegisterResponse? = response?.body()
//            }

            override fun onResponse(
                call: Call<ArrayList<LocationResponse>>,
                response: Response<ArrayList<LocationResponse>>
            ) {

                when (response.code()) {
                    400 -> try {
                        Toast.makeText(
                            this@MapsActivity,
                            response.errorBody()!!.string(),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    500 -> try {
                        Toast.makeText(
                            this@MapsActivity,
                            response.errorBody()!!.string(),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    200 -> try {
                        listLocation = response.body()
                        if (listLocation != null) {
                            Log.v("retrofit", listLocation.toString())
//                            Log.v("retrofit1", listLocation[1].userName.toString())
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }


            override fun onFailure(call: Call<ArrayList<LocationResponse>>, t: Throwable) {

                Log.v("retrofit", "call failed")
                println(t.message)
                val messagge = t.localizedMessage

                Toast.makeText(this@MapsActivity, messagge, Toast.LENGTH_LONG).show()
            }

        }
        )
    }

    private fun placeMarker( location: LatLng, name : String) {
//        val markerOption = MarkerOptions().position(location)

        fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
            return ContextCompat.getDrawable(context, vectorResId)?.run {
                setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
                draw(Canvas(bitmap))
                BitmapDescriptorFactory.fromBitmap(bitmap)
            }
        }

        val markerOption =
            MarkerOptions()
                .position(location)
                .title(name)
                .snippet("hola!")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED ))
                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_car))
//                .icon(BitmapDescriptorFactory.fromFile("C:\\Users\\Virtual Dreams\\Desktop\\vd\\app\\src\\main\\res\\drawable-v24\\ic_car_f.png"))
//                .icon(BitmapDescriptorFactory.fromPath(
//                    "C:\\Users\\Leandro\\Documents\\RepositorioVD\\vd\\app\\src\\main\\res\\drawable\\auto.png"))

        mCurrLocationMarker = mGoogleMap.addMarker(markerOption)
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {

                AlertDialog.Builder(this)
                    .setTitle("Permisos necesarios")
                    .setMessage("Esta app necesita los permisos de ubicación")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(
                            this@MapsActivity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    }
                    .create()
                    .show()


            } else {
                // pedir permisos
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // me dio los permisos
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {

                        mFusedLocationClient?.requestLocationUpdates(
                            mLocationRequest,
                            mLocationCallback,
                            Looper.myLooper()
                        )
                        mGoogleMap.isMyLocationEnabled = true
                    }

                } else {

                    // permisos denegados
                    Toast.makeText(this, "permiso denegado", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    companion object {
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }
}