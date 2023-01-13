package com.alex.uberclonedriverapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.alex.uberclonedriverapp.R
import com.alex.uberclonedriverapp.databinding.ActivityCalificationClientBinding
import com.alex.uberclonedriverapp.models.History
import com.alex.uberclonedriverapp.providers.HistoryProvider
import com.alex.uberclonedriverapp.utils.Constants

class CalificationClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalificationClientBinding
    private var extraPrice = 0.0
    private var historyProvider = HistoryProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificationClientBinding.inflate(layoutInflater)
        setContentView(binding.root)
        extraPrice = intent.getDoubleExtra(Constants.PRICE, 0.0)
        binding.tvPrice.text = "Precio: $extraPrice"

        getHistory()
    }

    private fun getHistory(){
        historyProvider.getLastHistory().get().addOnSuccessListener { query ->
            if (query != null){
                if(query.documents.size > 0){
                    val history =  query.documents[0].toObject(History::class.java)
                    Log.d("FIRESTORE", "HISTORIAL: ${history?.toJson()}")
                }else{
                    Toast.makeText(this, "No se encontro el historial", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}