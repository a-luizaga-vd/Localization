package com.example.localization

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Chronometer
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BiddingStatusActivity : AppCompatActivity(), OnChronometerTickListener {


    private lateinit var counter: Chronometer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_state_bidding)

        val bundle = intent.extras
        val etPrice = bundle?.getString("et_price");
        val etTime = bundle?.getString("et_time");
        val etProduct = bundle?.getString("et_product");

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
}