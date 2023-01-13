package com.alex.uberclonedriverapp.providers

import android.util.Log
import com.alex.uberclonedriverapp.models.Booking
import com.alex.uberclonedriverapp.models.History
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistoryProvider {

    val db = Firebase.firestore.collection("Histories")
    val authProvider = AuthProvider()

    fun create(history: History): Task<DocumentReference> {
        return db.add(history).addOnFailureListener{
            Log.d("FIRESTORE", "ERROR: ${it.message}")
        }
    }

    fun getBooking(): Query {
        return db.whereEqualTo("idDriver", authProvider.getId())
    }

    fun updateStatus(idClient:String, status: String): Task<Void> {
        return db.document(idClient).update("status", status).addOnFailureListener{ exception ->
            Log.d("FIRESTORE", "ERROR: ${exception.message}")
        }
    }
}