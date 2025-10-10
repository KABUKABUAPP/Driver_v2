package com.kabukabu.driver.features.auth.presentation.sign_up.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    init {
        fetchCarBrands()
    }

    private val _carBrands = MutableStateFlow<List<String>>(emptyList())
    val carBrands: StateFlow<List<String>> = _carBrands

    fun fetchCarBrands() {
        val db = FirebaseFirestore.getInstance()
        val reference = db.collection("carbrands").document("t9MZDmH3FWTg3KoKuXs3")

        reference.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val brands = document.get("branditems") as? List<String>
                    brands?.let {
                        _carBrands.value = it
                        println("car brands are $carBrands")
                    }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}
