package com.mousom.smartlangar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {
    private var googleSignInClient: GoogleSignInClient? = null
    private var firebaseAuth: FirebaseAuth? = null
    var logo: ImageView? = null
    var sign_in_with_google: TextView? = null
    var google_login: SignInButton? = null
    public override fun onStart() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        setContentView(R.layout.activity_main)
        firebaseAuth = FirebaseAuth.getInstance()
        logo = findViewById(R.id.logo)
        sign_in_with_google = findViewById(R.id.sign_in_txt)
        google_login = findViewById(R.id.google_signIn)
        google_login?.setOnClickListener(View.OnClickListener { v: View? -> signIn() })
        createRequest()


        //Animation logic
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(logo, "translationY", -1000f, 0f),
            ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(sign_in_with_google, "translationX", -200f, 0f)
        )
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.duration = 2000
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                val animatorSet2 = AnimatorSet()
                animatorSet2.playTogether(
                    ObjectAnimator.ofFloat(logo, "scaleX", 1f, 0.5f, 1f),
                    ObjectAnimator.ofFloat(logo, "scaleY", 1f, 0.5f, 1f)
                )
                animatorSet2.interpolator = AccelerateInterpolator()
                animatorSet2.duration = 1000
                animatorSet2.start()
            }
        })
        animatorSet.start()
    }

    private fun createRequest() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, C_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == C_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(g_account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(g_account.idToken, null)
        firebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val user = firebaseAuth!!.currentUser
                    val intent = Intent(applicationContext, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@MainActivity, "Something Went Wrong!!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    companion object {
        private const val C_SIGN_IN = 123
    }
}