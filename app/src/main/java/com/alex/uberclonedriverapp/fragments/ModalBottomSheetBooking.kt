package com.alex.uberclonedriverapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.alex.uberclonedriverapp.R
import com.alex.uberclonedriverapp.activities.MapActivity
import com.alex.uberclonedriverapp.models.Booking
import com.alex.uberclonedriverapp.providers.AuthProvider
import com.alex.uberclonedriverapp.providers.BookingProvider
import com.alex.uberclonedriverapp.providers.GeoProvider
import com.alex.uberclonedriverapp.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ModalBottomSheetBooking: BottomSheetDialogFragment() {

    private lateinit var tvOrigin:TextView
    private lateinit var tvDestination:TextView
    private lateinit var tvTimeAndDistance: TextView
    private lateinit var btnAccept: Button
    private lateinit var btnCancel: Button

    private val bookingProvider = BookingProvider()
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private lateinit var mapActivity: MapActivity


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ):View?{
        val view = inflater.inflate(R.layout.modal_bottom_sheet_booking, container, false)



        tvOrigin = view.findViewById(R.id.tvOrigin)
        tvDestination = view.findViewById(R.id.tvDestination)
        tvTimeAndDistance = view.findViewById(R.id.tvTimeAndDistance)
        btnAccept = view.findViewById(R.id.btnAccept)
        btnCancel = view.findViewById(R.id.btnCancel)

        val data = arguments?.getString(Constants.BOOKING)
        val booking = Booking.fromJson(data!!)
        Log.d("ARGUMENTS BOOKING", "Booking: ${booking?.toJson()} ")

        tvOrigin.text = booking?.origin
        tvDestination.text = booking?.destination
        tvTimeAndDistance.text = "${booking?.time} Min - ${booking?.km} Km"

        btnAccept.setOnClickListener { acceptBooking(booking?.idClient!!) }
        btnCancel.setOnClickListener { cancelBooking(booking?.idClient!!) }

        return view
    }

    private fun cancelBooking(idClient: String){
        bookingProvider.updateStatus(idClient, "cancel").addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(context, "Viaje cancelado", Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(context, "No se pudo cancelar el viaje", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun acceptBooking(idClient: String){
        bookingProvider.updateStatus(idClient, "accept").addOnCompleteListener {
            if (it.isSuccessful){
                //Tenemos que finalizar el reconocimiento de la ubicación en tiempo real por lo que es necesario hacer uso
                //del metodo endUpdates ya que si no se usa solamente se borra una vez y es necesario parar ese servicio
                //la funcionalidad es para que lo quite de la base de datos locations una vez que se acepto el viaje y los demas
                //usuarios no lo podran ver
                (activity as? MapActivity)?.easyWayLocation?.endUpdates()
                geoProvider.removeLocation(authProvider.getId())
                Toast.makeText(context, "Viaje aceptado", Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(context, "No se pudo aceptar el viaje", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object{
        const val TAG = "ModalBottomSheet"
    }
}