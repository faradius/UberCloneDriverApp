package com.alex.uberclonedriverapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.alex.uberclonedriverapp.activities.MapActivity
import com.alex.uberclonedriverapp.activities.MapTripActivity
import com.alex.uberclonedriverapp.providers.BookingProvider

class AcceptReceiver: BroadcastReceiver() {

    val bookingProvider = BookingProvider()

    override fun onReceive(context: Context, intent: Intent) {
        val idBooking = intent.extras?.getString("idBooking")
        acceptBooking(context, idBooking!!)
    }

    private fun acceptBooking(context: Context, idBooking: String){
        bookingProvider.updateStatus(idBooking, "accept").addOnCompleteListener {

            if (it.isSuccessful){
                goToMapTrip(context)
            }
            else{
                Log.d("RECEIVER", "No se puede actualizar el estado del viaje: ")
            }
        }
    }

    private fun goToMapTrip(context: Context){
        val i = Intent(context, MapTripActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        //esto permite pasar a otra pantalla desde una notificación
        i.action = Intent.ACTION_RUN
        context.startActivity(i)
    }
}