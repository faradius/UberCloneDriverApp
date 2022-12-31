package com.alex.uberclonedriverapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alex.uberclonedriverapp.R
import com.alex.uberclonedriverapp.models.Booking
import com.alex.uberclonedriverapp.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ModalBottomSheetBooking: BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ):View?{
        val view = inflater.inflate(R.layout.modal_bottom_sheet_booking, container, false)

        val data = arguments?.getString(Constants.BOOKING)
        val booking = Booking.fromJson(data!!)
        Log.d("ARGUMENTS BOOKING", "Booking: ${booking?.toJson()} ")

        return view
    }

    companion object{
        const val TAG = "ModalBottomSheet"
    }
}