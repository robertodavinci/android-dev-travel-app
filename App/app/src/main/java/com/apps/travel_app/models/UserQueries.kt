package com.apps.travel_app.models

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import com.apps.travel_app.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

//class UserQueries {


private val UTAG = "Updating document "
private val ATAG = "Adding document "
private val DTAG = "Deleting document "

fun addUser(db: FirebaseFirestore, displayName: String, userId: String?){
    val user = hashMapOf(
        "display_name" to displayName,
        "user_id" to userId,
    )
    if (userId != null) {
        Log.d("User adding", "DocumentSnapshot added with ID: ${userId}")
        db.collection("users").document(userId).set(user)
            .addOnSuccessListener { documentReference ->
                Log.d("User adding", "DocumentSnapshot added with ID: ${userId}")
                addUserPeferences(db, userId)
            }
            .addOnFailureListener { e ->
                Log.w("User adding", "Error adding document", e)
            }
    }
    else db.collection("users").add(user)
        .addOnSuccessListener { documentReference ->
        Log.d("User adding", "DocumentSnapshot added with ID: ${documentReference.id}")
        addUserPeferences(db, documentReference.id)
        }
        .addOnFailureListener { e ->
            Log.w("User adding", "Error adding document", e)
        }
}


fun addUserPeferences(db: FirebaseFirestore, userId: String){
    val userPreferences = UserPreferences(true, 1, null, null)
    val userPref = db.collection("users").document(userId).collection("user_preferences").document(userId).set(userPreferences).
    addOnSuccessListener {
        Log.d(ATAG, "User preferences successfully added.")
    }.addOnFailureListener(){
            e-> Log.w(ATAG, "Error adding document.", e)
    }
}

fun addDestination(db: FirebaseFirestore, destination: Destination){
    val destinationDocument = db.collection("destinations")
    destinationDocument.add(destination)
        .addOnSuccessListener {   documentReference ->
          destinationDocument.document(documentReference.id).update("destination_id", documentReference.id).addOnSuccessListener {
              Log.d("Destination adding", "DocumentSnapshot added with ID: ${documentReference.id}")
          }
            }
        .addOnFailureListener{ e ->
                Log.w("Destination adding", "Error adding document", e)
            }
}

fun addCountry(db: FirebaseFirestore, country: Country){
    val countrySet = hashMapOf(
        "acronym" to country.acronym,
        "name" to country.name,
        "currency" to country.currency
    )
    val countryDocument = db.collection("countries").document(country.name).set(countrySet).
    addOnSuccessListener {
            Log.d(ATAG, "Country successfully added.")
        }.addOnFailureListener(){
                e-> Log.w(ATAG, "Error adding document.", e)
        }
    }

fun updateUserInfo(db: FirebaseFirestore, userId: String, newDisplayName: String){
    val userRef = db.collection("users").document(userId)
    userRef.update("display_name", newDisplayName).addOnSuccessListener {
        // also add the feature for auth.currentUser display name update
        Log.d(UTAG, "Document successfully updated.")
    }.addOnFailureListener(){
            e-> Log.w(UTAG, "Error updating document.", e)
    }
}

fun updateDestination(db: FirebaseFirestore, destination: Destination){
    val destinationDocument = db.collection("destinations").document(destination.id)
}

fun updateUserPreferences(db: FirebaseFirestore, userId: String, colour: Boolean?, economy: Int?, name: String?, surname: String?){
    val userPref = db.collection("users").document(userId).collection("user_preferences").
    document(userId)
    val userPreferences = UserPreferences(colour, economy, name, surname)
    val data = mutableMapOf<String,Any>()
    if(colour != null) data["colour"] = colour
    if(economy != null) data["economy"] = economy
    if(name != null) data["colour"] = name
    if(surname != null) data["colour"] = surname
    userPref.let { if(data.isNotEmpty()) it.update("colour",true) }
}

//


fun uploadImage(){

}

fun selectImage(){

}


