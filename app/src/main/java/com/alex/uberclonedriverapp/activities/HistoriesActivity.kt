package com.alex.uberclonedriverapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alex.uberclonedriverapp.R
import com.alex.uberclonedriverapp.adapters.HistoriesAdapter
import com.alex.uberclonedriverapp.databinding.ActivityHistoriesBinding
import com.alex.uberclonedriverapp.models.History
import com.alex.uberclonedriverapp.providers.HistoryProvider

class HistoriesActivity : AppCompatActivity() {

    private  lateinit var binding: ActivityHistoriesBinding
    private var historyProvider = HistoryProvider()
    private var histories = ArrayList<History>()
    private lateinit var adapter: HistoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val linearLayoutManager = LinearLayoutManager(this)
        binding.rvHistories.layoutManager = linearLayoutManager

        getHistories()
    }

    private fun getHistories(){
        //es bueno limpiar la lista antes de mostrar los elementos
        histories.clear()
        historyProvider.getHistories().get().addOnSuccessListener { query->
            if (query != null){
                if (query.documents.size > 0){
                    val documents = query.documents

                    for (d in documents){
                        val history = d.toObject(History::class.java)
                        histories.add(history!!)
                    }

                    adapter = HistoriesAdapter(this@HistoriesActivity, histories)
                    binding.rvHistories.adapter = adapter
                }
            }

        }
    }
}