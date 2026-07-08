package com.faiz.bumiloka.ui.login

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.faiz.bumiloka.AdminActivity
import com.faiz.bumiloka.ForgotPasswordActivity
import com.faiz.bumiloka.MainActivity
import com.faiz.bumiloka.ProgressSyncHelper
import com.faiz.bumiloka.R
import com.faiz.bumiloka.RegisterActivity
import com.faiz.bumiloka.databinding.LoginActivityBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: LoginActivityBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private val rcSignIn = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())[LoginViewModel::class.java]

        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val loginButton = binding.login
        val loadingProgressBar = binding.loading
        val registerText = binding.registerText
        val forgotPasswordText = binding.forgotPassword

        val text = "Belum Punya Akun? Daftar Di Sini"
        val spannable = SpannableString(text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        }

        spannable.setSpan(
            clickableSpan,
            17,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            ForegroundColorSpan("#5DAE4A".toColorInt()),
            17,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        registerText.text = spannable
        registerText.movementMethod = LinkMovementMethod.getInstance()
        registerText.highlightColor = Color.TRANSPARENT

        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        loginViewModel.loginFormState.observe(this, Observer { loginFormState ->
            if (loginFormState == null) {
                return@Observer
            }
            loginButton.isEnabled = loginFormState.isDataValid
            
            usernameEditText.error = loginFormState.usernameError?.let { getString(it) }
            passwordEditText.error = loginFormState.passwordError?.let { getString(it) }
        })

        loginViewModel.loginResult.observe(this, Observer { loginResult ->
            loginResult ?: return@Observer
            loadingProgressBar.visibility = View.GONE
            loginResult.error?.let {
                showLoginFailed(it)
            }
            loginResult.success?.let {
                val user = FirebaseAuth.getInstance().currentUser

                if (user != null && user.isEmailVerified) {
                    // ✅ EMAIL SUDAH VERIFIED
                    updateUiWithUser(it)

                } else {
                    // ❌ BELUM VERIFIED
                    FirebaseAuth.getInstance().signOut()

                    Toast.makeText(
                        this,
                        "Email belum diverifikasi, cek email kamu",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                loginViewModel.loginDataChanged(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
            false
        }

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        loginButton.setOnClickListener {

            val email =
                usernameEditText.text.toString()

            val password =
                passwordEditText.text.toString()

            // LOGIN ADMIN HARDCODE

            if (
                email == "admin@gmail.com" &&
                password == "admin123"
            ) {

                Toast.makeText(
                    this,
                    "Login Admin Berhasil",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(
                    Intent(
                        this,
                        AdminActivity::class.java
                    )
                )

                finish()

                return@setOnClickListener
            }

            // LOGIN USER BIASA FIREBASE

            loadingProgressBar.visibility = View.VISIBLE

            loginViewModel.login(
                email,
                password
            )
        }

        binding.btnGoogle.setOnClickListener {
            signInGoogle()
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, rcSignIn)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == rcSignIn) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken ?: "")
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        if (idToken.isEmpty()) return
        
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Google berhasil", Toast.LENGTH_SHORT).show()
                    val user = firebaseAuth.currentUser

                    if (user != null) {
                        // SINKRONISASI PROGRESS SEBELUM MASUK KE MAIN
                        ProgressSyncHelper.syncProgressFromFirebase(this) {
                            FirebaseDatabase.getInstance()
                                .reference
                                .child("users")
                                .child(user.uid)
                                .child("role")
                                .get()
                                .addOnSuccessListener { snapshot ->
                                    val role = snapshot.getValue(String::class.java)
                                    if (role == "admin") {
                                        startActivity(Intent(this, AdminActivity::class.java))
                                    } else {
                                        startActivity(Intent(this, MainActivity::class.java))
                                    }
                                    finish()
                                }
                        }
                    }
                } else {
                    Toast.makeText(this, "Authentication gagal", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome) + " " + model.displayName
        Toast.makeText(applicationContext, welcome, Toast.LENGTH_LONG).show()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        // SINKRONISASI PROGRESS UNTUK LOGIN EMAIL/PASSWORD
        ProgressSyncHelper.syncProgressFromFirebase(this) {
            FirebaseDatabase.getInstance().reference
                .child("users")
                .child(uid)
                .child("role")
                .get()
                .addOnSuccessListener { snapshot ->
                    val role = snapshot.getValue(String::class.java)
                    if (role == "admin") {
                        startActivity(Intent(this, AdminActivity::class.java))
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    finish()
                }
                .addOnFailureListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
        }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_LONG).show()
    }
}
