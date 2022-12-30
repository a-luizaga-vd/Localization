package com.example.localization

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Chronometer
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.log

//import kotlin.coroutines.jvm.internal.CompletedContinuation.context


class BiddingStatusActivity : AppCompatActivity(), OnChronometerTickListener {

    private lateinit var counter : Chronometer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_state_bidding)

        counter = findViewById( R.id.chronometerBid)

        startCounter()
    }

    private fun startCounter(){
        counter.isCountDown = true
        counter.base = SystemClock.elapsedRealtime() + 10000
        counter.start()

        counter.setOnChronometerTickListener {
            this
        }
    }
//    OnChronometerTickListener{
//        chronometer ->
//        Log.i("TIMER", chronometer.text.toString())
//        if (chronometer.text.toString().equals("00:00", ignoreCase = true))
//            Toast.makeText(
//                this,
//                "time reached", Toast.LENGTH_SHORT).show()
//    }

    override fun onChronometerTick(p0: Chronometer?) {
        Log.i("TIMER", counter.text.toString())
        Log.i("TIMER", "CALLED")
        if (counter.text.toString().equals("00:00", ignoreCase = true))
            Toast.makeText(
                this,
                "time reached", Toast.LENGTH_SHORT).show()
    }
}