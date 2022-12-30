package com.example.localization

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Chronometer
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

//import kotlin.coroutines.jvm.internal.CompletedContinuation.context


class BiddingStatusActivity : AppCompatActivity(), OnChronometerTickListener {


    private lateinit var counter: Chronometer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_state_bidding)

        startCounter()

    }

    override fun onChronometerTick(chronometer: Chronometer) {
        Log.e("onChronometerTick", "called")
        if (chronometer.text.toString().equals("00:00", ignoreCase = true)) {
            chronometer.stop();
            Toast.makeText(
                this,
                "time reached", Toast.LENGTH_SHORT
            ).show()
        }
    }

    //
    private fun startCounter() {

        counter = findViewById(R.id.chronometerBid)
        counter.isCountDown = true

        //para cambiar el tiempo base
        counter.base = SystemClock.elapsedRealtime() + 10000

        counter.onChronometerTickListener = this
//        counter.setOnChronometerTickListener {
//            this
//        }
        counter.start()
        Log.v("TIMER", "Started")
    }
}