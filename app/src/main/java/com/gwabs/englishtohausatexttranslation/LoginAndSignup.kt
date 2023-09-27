package com.gwabs.englishtohausatexttranslation

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.gwabs.englishtohausatexttranslation.databinding.ActivityLoginAndSignupBinding
import com.gwabs.englishtohausatexttranslation.databinding.ActivityMainBinding
import com.techiness.progressdialoglibrary.ProgressDialog
import java.util.regex.Pattern

class LoginAndSignup : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginAndSignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginAndSignupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = Firebase.auth




        // click listener for buttons
        binding.btnLogin.setOnClickListener {

            if (isDetailsValid(binding.emailEditText.text.toString()
                    ,binding.passwordEditText.text.toString())){
                loginUser(binding.emailEditText.text.toString(),binding.passwordEditText.text.toString())
            }
        }
        binding.btnSignup.setOnClickListener {
            if (isDetailsValid(binding.emailEditText.text.toString()
                    ,binding.passwordEditText.text.toString())){
                signUpUser(binding.emailEditText.text.toString(),binding.passwordEditText.text.toString())
            }
        }
        binding.forgetPassword.setOnClickListener {
            showForgetPasswordDialog(binding.emailEditText.text.toString())
        }
    }
    private fun showForgetPasswordDialog(email: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Forgot Password")
        builder.setMessage("Enter your email address to reset your password.")
        builder.setCancelable(false)

        val input = EditText(this)
        input.hint = "Email Address"
        builder.setView(input)

        builder.setPositiveButton("Submit") { dialog, which ->
            val email = input.text.toString()
            if (TextUtils.isEmpty(email)) {
                input.error = "Email can't be empty"
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                input.error = "Invalid Email"
            } else {
                sendForgetPassword(email)
            }
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        builder.show()
    }


    private fun loginUser(email:String , password:String){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait..")
        progressDialog.show()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithEmail:success")
                    Toast.makeText(this,"Login successfully",Toast.LENGTH_SHORT).show()
                    toMainActivity()
                } else {
                    // If sign in fails, display a message to the user.
                    progressDialog.dismiss()
                    Log.w("TAG", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.${task.exception?.message}",
                        Toast.LENGTH_SHORT,
                    ).show()

                }
            }
    }

    private fun signUpUser(email:String , password:String){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait..")
        progressDialog.show()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    progressDialog.show()
                    Log.d("TAG", "createUserWithEmail:success")
                    Toast.makeText(this,"Signup successfully",Toast.LENGTH_SHORT).show()
                   toMainActivity()


                } else {
                    // If sign in fails, display a message to the user.
                    //Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    progressDialog.dismiss()
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }

    }

    private fun toMainActivity() {
        val intent = Intent(this@LoginAndSignup,MainActivity::class.java)
        startActivity(intent)
        this.finish()
    }


    private fun sendForgetPassword(email: String){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Sending...")
        progressDialog.show()
        Firebase.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(this,"Send email",Toast.LENGTH_SHORT).show()
                }
            }



    }

    private fun isDetailsValid(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.emailEditText.error = "Email can't be empty"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show()
            binding.emailEditText.error = "Invalid email"
            return false
        }

        if (password.length < 6 || password.length > 8) {
            binding.passwordEditText.error = "Password should be 6 to 8 char length"
            return false
        }

        return true
    }


    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
           toMainActivity()
        }
    }
}