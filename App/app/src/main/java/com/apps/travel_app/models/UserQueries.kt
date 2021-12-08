package com.apps.travel_app.models

import android.util.Log
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
        } else db.collection("users").add(user).addOnSuccessListener { documentReference ->
            Log.d("User adding", "DocumentSnapshot added with ID: ${documentReference.id}")
            addUserPeferences(db, documentReference.id)
        }
            .addOnFailureListener { e ->
                Log.w("User adding", "Error adding document", e)
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


    fun addUserPeferences(db: FirebaseFirestore, userId: String){
        val userPreferences = UserPreferences(true, 1, null, null)
        val userPref = db.collection("users").document(userId).collection("user_preferences").document(userId).set(userPreferences).
        addOnSuccessListener {
            Log.d(ATAG, "User preferences successfully added.")
        }.addOnFailureListener(){
                e-> Log.w(ATAG, "Error updating document.", e)
        }
    }


