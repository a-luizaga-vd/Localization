package com.example.localization

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Chronometer
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.signalr.Action1
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder

class BiddingStatusActivity : AppCompatActivity(), OnChronometerTickListener {

    private lateinit var hubConnection: HubConnection
    var token: String? = null
    private lateinit var counter: Chronometer
    private var idSubasta : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_state_bidding)

        token = HomeActivity.myToken

        //Se crea la conexión
//        hubConnection = HubConnectionBuilder.create("http://35.239.225.98:443/hubs/bids")
//            .withHeader("Authorization", "Bearer $token")
//            .build()

        hubConnection = HubConnectionBuilder.create("http://10.0.2.2:5004/hubs/bids")
            .withHeader("Authorization", "Bearer "+token)
            .build();

        hubConnection.start()

        hubConnection.on(
            "FinishBid",
            Action1 { msg: String -> println("Message: $msg") },
            String::class.java
        )

        val bundle = intent.extras
        val etPrice = bundle?.getString("et_price");
        val etTime = bundle?.getString("et_time");
        val etProduct = bundle?.getString("et_product");
        idSubasta = bundle?.getString("id_subasta")

//        Log.i("ID SUBASTA", bundle?.getString("id_subasta"))
        Log.i("ID SUBASTA", idSubasta.toString())

        val textPrice = findViewById<TextView>(R.id.price_bid)
        val textProduct = findViewById<TextView>(R.id.product_bid)
        textPrice.text = etPrice.toString()
        textProduct.text = etProduct.toString()
        startCounter(etTime)

    }

    override fun onChronometerTick(chronometer: Chronometer) {
        Log.i("TIMER", "called")
        if (chronometer.text.toString().equals("00:00", ignoreCase = true)) {
            chronometer.stop();
            Toast.makeText(
                this,
                "time reached", Toast.LENGTH_SHORT
            ).show()
            Log.v("TIMER", "Stopped")

        }
    }


    private fun startCounter( time : String?) {

        counter = findViewById(R.id.chronometerBid)
        counter.isCountDown = true

        //para cambiar el tiempo base
        if (time != null) {
            counter.base = (SystemClock.elapsedRealtime() + (time.toDouble() * 60000)).toLong()
        }

        counter.onChronometerTickListener = this

        counter.start()
        Log.v("TIMER", "Started")
    }

    fun cancelBidding(view: View) {
        hubConnection.send("finishBid", idSubasta, false)

        MyBackgroundService.alert="210.0F"

        //hubConnection.send("LeaveGroup")
    }
}