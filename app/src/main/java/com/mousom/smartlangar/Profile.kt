package com.mousom.smartlangar

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.util.*

class Profile : AppCompatActivity() {
    var profileName: TextInputEditText? = null
    var profilePhone: TextInputEditText? = null
    var profileAddress: TextInputEditText? = null
    var profilePic: ImageView? = null
    var saveProfile: Button? = null
    var fstore: FirebaseFirestore? = null
    var userId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        profilePic = findViewById(R.id.profile_pic)
        profileName = findViewById(R.id.profile_name)
        profilePhone = findViewById(R.id.profile_num)
        profileAddress = findViewById(R.id.user_address)
        saveProfile = findViewById(R.id.save_profile)
        fstore = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        val img = Objects.requireNonNull(FirebaseAuth.getInstance().currentUser)
            ?.photoUrl
        val signInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (signInAccount != null) {
            profileName?.setText(" " + signInAccount.displayName)
            Picasso.get().load(img).into(profilePic)
            val docf = fstore!!.collection("users").document(
                userId!!
            )
            docf.addSnapshotListener(this) { value, error ->
                profilePhone?.setText(value!!.getString("PhoneNum"))
                if (value != null) {
                    profileAddress?.setText(value.getString("Address"))
                }
            }
        }
        saveProfile?.setOnClickListener(View.OnClickListener { v: View? -> saveProfile() })
    }

    fun saveProfile() {
        val userName = FirebaseAuth.getInstance().currentUser!!.displayName
        val userPhone = profilePhone!!.text.toString()
        val userAddress = profileAddress!!.text.toString()
        val documentReference = fstore!!.collection("users").document(
            userId!!
        )
        val user: MutableMap<String, Any?> = HashMap()
        user["FullName"] = userName
        user["PhoneNum"] = userPhone
        user["Address"] = userAddress
        documentReference.set(user).addOnSuccessListener { unused: Void? ->
            Toast.makeText(
                this@Profile,
                "Data Saved",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        private const val TAG = "tag"
    }
}