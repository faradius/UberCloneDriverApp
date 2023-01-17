package com.alex.uberclonedriverapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.alex.uberclonedriverapp.R
import com.alex.uberclonedriverapp.databinding.ActivityCalificationClientBinding
import com.alex.uberclonedriverapp.models.History
import com.alex.uberclonedriverapp.providers.HistoryProvider
import com.alex.uberclonedriverapp.utils.Config
import com.alex.uberclonedriverapp.utils.Constants

class CalificationClientActivity : AppCompatActivity() {

    private var history: History? = null
    private lateinit var binding: ActivityCalificationClientBinding
    private var extraPrice = 0.0
    private var historyProvider = HistoryProvider()
    private var calification = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificationClientBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Config.setVersionCompatibilityStatusBar(window)
        extraPrice = intent.getDoubleExtra(Constants.PRICE, 0.0)
        binding.tvPrice.text = "${String.format("%.1f", extraPrice)}"

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, value, b ->
            calification = value
        }

        binding.btnCalification.setOnClickListener{
            if (history?.id != null){
                updateCalification(history?.id!!)
            }else{
                Toast.makeText(this, "El id del historial es nulo", Toast.LENGTH_LONG).show()
            }
        }

        getHistory()
    }

    private fun updateCalification(idDocument: String){
        historyProvider.updateCalificationToClient(idDocument, calification).addOnCompleteListener {
            if (it.isSuccessful){
                goToMap()
            }else{
                Toast.makeText(this@CalificationClientActivity, "Error al actualizar la calificación", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun goToMap(){
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun getHistory(){
        historyProvider.getLastHistory().get().addOnSuccessListener { query ->
            if (query != null){
                if(query.documents.size > 0){
                    history =  query.documents[0].toObject(History::class.java)
                    //Aqui le ponemos el valor del id del documento
                    history?.id = query.documents[0].id
                    binding.tvOrigin.text = history?.origin
                    binding.tvDestination.text = history?.destination
                    binding.tvTimeAndDistance.text = "${history?.time} Min - ${String.format("%.1f", history?.km)} Km"
                    Log.d("FIRESTORE", "HISTORIAL: ${history?.toJson()}")
                }else{
                    Toast.makeText(this, "No se encontro el historial", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}