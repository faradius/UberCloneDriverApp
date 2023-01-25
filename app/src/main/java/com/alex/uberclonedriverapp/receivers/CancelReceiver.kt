package com.alex.uberclonedriverapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.alex.uberclonedriverapp.activities.MapActivity
import com.alex.uberclonedriverapp.activities.MapTripActivity
import com.alex.uberclonedriverapp.providers.BookingProvider

class CancelReceiver: BroadcastReceiver() {

    val bookingProvider = BookingProvider()

    override fun onReceive(context: Context, intent: Intent) {
        val idBooking = intent.extras?.getString("idBooking")
        cancelBooking(idBooking!!)
    }

    private fun cancelBooking(idBooking: String){
        bookingProvider.updateStatus(idBooking, "cancel").addOnCompleteListener {

            if (it.isSuccessful){
                Log.d("RECEIVER", "El viaje fue cancelado")
            }
            else{
                Log.d("RECEIVER", "No se puede actualizar el estado del viaje: ")
            }
        }
    }

}