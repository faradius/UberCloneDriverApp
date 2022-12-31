package com.alex.uberclonedriverapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.alex.uberclonedriverapp.R
import com.alex.uberclonedriverapp.models.Booking
import com.alex.uberclonedriverapp.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ModalBottomSheetBooking: BottomSheetDialogFragment() {

    private lateinit var tvOrigin:TextView
    private lateinit var tvDestination:TextView
    private lateinit var tvTimeAndDistance: TextView
    private lateinit var btnAccept: Button
    private lateinit var btnCancel: Button

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

        return view
    }

    companion object{
        const val TAG = "ModalBottomSheet"
    }
}