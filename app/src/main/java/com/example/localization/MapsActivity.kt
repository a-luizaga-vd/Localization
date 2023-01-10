package com.example.localization

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.localization.response.LocationResponse
import com.example.localization.services.LocationService
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    lateinit var mGoogleMap: GoogleMap
    var mapFrag: SupportMapFragment? = null
    lateinit var mLocationRequest: LocationRequest

    var listLocation: ArrayList<LocationResponse>? = null

    fun getArrayLocations(): ArrayList<LocationResponse>? {
        return listLocation
    }

    var mLastLocation: LatLng? = null
    private var mapCircle: Circle? = null
    private var mapCircle2: Circle? = null
    private var isInfoWindowShown = false
    private var mapMarkers: HashMap<String, Marker?> = HashMap()
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
                    "Location: " + location.latitude + " " + location.longitude
                )
                mLastLocation = LatLng(location.latitude, location.longitude)
            }

            getLocations()

//                mapMarkers?.clear()

            //pone el marcador actual

            val locations = getArrayLocations()
            if (locations != null) {
                Log.v("retrofit", locations.size.toString())
            }
            if (locations != null) {
                for (i in 0 until locations.size) {

                    val latLng = LatLng(
                        locations[i].latitud.toDouble(), locations[i].logitud.toDouble()
                    )
                    placeMarker(latLng, locations[i].userName, locations[i].string1)

                    Log.v("retrofit", "****************************")
                    Log.v("retrofit8", locations[i].userName.toString())
                    Log.v("retrofit8", locations[i].latitud.toString())
                    Log.v("retrofit8", locations[i].logitud.toString())
                    Log.v("retrofit8", locations[i].string1.toString())
                    Log.v("retrofit", "****************************")
                }
            }
//                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions)
            //mueve la camara // probar otros zooms
//            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        supportActionBar?.title = "Mapa"

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapFrag = supportFragmentManager.findFragmentById(R.id.map1) as SupportMapFragment?
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
        mLocationRequest.interval = 1000 // 120000 two minute interval
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

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
            mGoogleMap.isMyLocationEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                if( mLastLocation != null )
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLocation!!, 13.5F))
            }, 3000)
        } else {
            //pedir permisos de locacion
            checkLocationPermission()
        }
        mGoogleMap.isMyLocationEnabled = false

        Handler(Looper.getMainLooper()).postDelayed({
            if( mLastLocation != null )
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLocation!!, 13.5F))
        }, 3000)

//        val latLng = LatLng(-34.6814086160595, -58.55807614030567)
//
//        val markerOption =
//            MarkerOptions()
//                .position(latLng)
//                .title("hola")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
//        mGoogleMap.addMarker(markerOption)

        mGoogleMap.setOnMarkerClickListener(this);

        mGoogleMap.setOnMapClickListener(object :GoogleMap.OnMapClickListener {
            override fun onMapClick(latlng :LatLng) {
                if (isInfoWindowShown) {
//                    marker.hideInfoWindow()
                    mapCircle?.remove();
                    mapCircle2?.remove();
//                mapCircle?.isVisible = false

                    Log.v("MARKERC", "********CLICK TRUE************")
                    Log.v("MARKERC", isInfoWindowShown.toString())
                    isInfoWindowShown = false
                }
            }
        })


    }

    private fun getLocations() {
        val locationService = ApiClient.getRetrofit().create(LocationService::class.java)

        val bearerToken = "Bearer " + LoginActivity.getMyToken()
        val result = locationService.getLocation(bearerToken)

        result.enqueue(object : Callback<ArrayList<LocationResponse>> {

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

    private fun placeMarker(location: LatLng, name: String, color: String) {
//        val markerOption = MarkerOptions().position(location)

        fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
            return ContextCompat.getDrawable(context, vectorResId)?.run {
                setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                val bitmap =
                    Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
                draw(Canvas(bitmap))
                BitmapDescriptorFactory.fromBitmap(bitmap)
            }
        }

        Log.v("MARKER", name)

        if (!mapMarkers.containsKey(name)) {
            Log.v("MARKER2", mapMarkers.containsKey(name).toString())
            Log.v("MARKER2", "ACA")
            val markerOption =
                MarkerOptions()
                    .position(location)
                    .title(name)
                    .snippet("hola!")
                    .draggable(true)
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED ))
                    .icon(bitmapDescriptorFromVector(this, R.drawable.ic_car_blue))
//                .icon(BitmapDescriptorFactory.fromPath(
//                    "C:\\Users\\Leandro\\Documents\\RepositorioVD\\vd\\app\\src\\main\\res\\drawable\\auto.png"))


            val mCurrLocationMarker = mGoogleMap.addMarker(markerOption)

            mCurrLocationMarker?.isVisible = true;
//            mCurrLocationMarker?.showInfoWindow();
            if (mCurrLocationMarker != null) {
                mapMarkers[name] = mCurrLocationMarker
                Log.v("MARKER2", "agrega")
                Log.v("MARKER2", mapMarkers.get(name).toString())
            }
        } else if (mapMarkers[name]?.position != location) {
            MarkerAnimation.animateMarkerToICS(
                mapMarkers, name, location,
                LatLngInterpolator.Spherical()
            )
            Log.v("MARKER2", mapMarkers.containsKey(name).toString())
        }

        if (mapMarkers.containsKey(name) && color == "0.0F")
            mapMarkers[name]?.setIcon(bitmapDescriptorFromVector(this, R.drawable.ic_car_red))
        else if (mapMarkers.containsKey(name))
            mapMarkers[name]?.setIcon(bitmapDescriptorFromVector(this, R.drawable.ic_car_blue))

//        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18.0F))
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
                        Handler(Looper.getMainLooper()).postDelayed({
                        if( mLastLocation != null )
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLocation!!, 13.5F))
                            }, 3000)
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

    override fun onMarkerClick(marker: Marker): Boolean {
//        mGoogleMap.setOnCameraIdleListener {
//            val midLatLng: LatLng = mGoogleMap.cameraPosition.target

//        Handler(Looper.getMainLooper()).postDelayed({
            //Do something after 100ms
            if (isInfoWindowShown) {
                marker.hideInfoWindow()
                mapCircle?.remove();
                mapCircle2?.remove();
//                mapCircle?.isVisible = false

                Log.v("MARKERC", "********CLICK TRUE************")
                Log.v("MARKERC", isInfoWindowShown.toString())
                isInfoWindowShown = false
            } else {
                Log.v("MARKERC", "********CLICK False************")
                Log.v("MARKERC", isInfoWindowShown.toString())
                isInfoWindowShown = true
                marker.showInfoWindow()
                mapCircle = mGoogleMap.addCircle(
                    CircleOptions()
                        .center(marker.position)
                        .radius(2000.0)
                        .strokeWidth(5f)
                        .strokeColor(Color.GREEN)
//                        .fillColor(Color.rgb(79,121,66))
                )
                mapCircle2 = mGoogleMap.addCircle(
                    CircleOptions()
                        .center(marker.position)
                        .radius(3000.0)
                        .strokeWidth(5f)
                        .strokeColor(Color.YELLOW)
//                        .fillColor(Color.rgb(79,121,66))
                )
                mapCircle?.isVisible = true
            }
//        }
            Log.v("MARKERC", "********CLICK************")


//        Toast.makeText(this, "Clicked location is ", Toast.LENGTH_SHORT).show()
        return false
    }
}