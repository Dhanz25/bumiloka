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
//                    runSeederOnce()
//                    val user = firebaseAuth.currentUser
//                    user?.let {
//                        FirebaseDatabase.getInstance().reference
//                            .child("users")
//                            .child(it.uid)
//                            .setValue(mapOf(
//                                "nama" to it.displayName,
//                                "email" to it.email,
//                                "role" to "admin",
//                                "totalPoin" to 0,
//                                "kuisSelesai" to 0,
//                                "edukasiDibaca" to 0,
//                                "tantanganSelesai" to 0
//                            ))
//                            .addOnSuccessListener {
//                                Toast.makeText(this, "Role admin berhasil diset!", Toast.LENGTH_SHORT).show()
//                            }
//                    }

//                    val intent = Intent(this, MainActivity::class.java)
//                    startActivity(intent)
//                    finish()

                    val user = firebaseAuth.currentUser

                    if (user != null) {

                        FirebaseDatabase.getInstance()
                            .reference
                            .child("users")
                            .child(user.uid)
                            .child("role")
                            .get()

                            .addOnSuccessListener { snapshot ->

                                val role =
                                    snapshot.getValue(String::class.java)

                                if (role == "admin") {

                                    startActivity(
                                        Intent(
                                            this,
                                            AdminActivity::class.java
                                        )
                                    )

                                } else {

                                    startActivity(
                                        Intent(
                                            this,
                                            MainActivity::class.java
                                        )
                                    )
                                }

                                finish()
                            }
                    }
                } else {
                    Toast.makeText(this, "Authentication gagal", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun runSeederOnce() {
        val sharedPref = getSharedPreferences("BumilokaPrefs", Context.MODE_PRIVATE)
        // 🔥 UBAH NAMA KEY JADI _v2 AGAR DIANGGAP BELUM PERNAH JALAN
        val isSeeded = sharedPref.getBoolean("isDataSekolahSeeded_v2", false)

        if (!isSeeded) {
            Log.d("Seeder", "Mulai mengunggah data sekolah ke Firebase...")
            Toast.makeText(this, "Mulai mengunggah data sekolah...", Toast.LENGTH_LONG).show()

            val jsonData = """
            {
              "sekolah": {
                "LUMBIR": [
                  { "nama": "SMP Negeri 1 Lumbir", "jenjang": "SMP", "alamat": "Jalan Raya Lumbir" },
                  { "nama": "SMP Negeri 2 Lumbir", "jenjang": "SMP", "alamat": "Jl. Raya Parungkamal-Lumbir Banyumas" },
                  { "nama": "SMP Negeri 3 Lumbir", "jenjang": "SMP", "alamat": "Citunggul RT 04 RW 05" },
                  { "nama": "SMP IT SRIWIJAYA", "jenjang": "SMP", "alamat": "Jl. Raya Lumbir RT 01 RW 04" },
                  { "nama": "SMP PGRI LUMBIR", "jenjang": "SMP", "alamat": "Jl Raya Cingebul" },
                  { "nama": "SMP VIP NU TAHFIZ AL-QURAN BANYUMAS", "jenjang": "SMP", "alamat": "RT 004 RW 007" },
                  { "nama": "MTsS MA`ARIF NU 1 LUMBIR", "jenjang": "SMP", "alamat": "JL.PARUNGKAMAL-BESUKI KM.01 LUMBIR" },
                  { "nama": "SMK NEGERI 1 LUMBIR", "jenjang": "SMK", "alamat": "RT. 09 RW. 04", "kode_pos": "53166" },
                  { "nama": "SMK SRIWIJAYA 3 LUMBIR", "jenjang": "SMK", "alamat": "RT 001 RW 004 Desa Lumbir", "kode_pos": "53166" }
                ],
                "WANGON": [
                  { "nama": "SMP Negeri 1 Wangon", "jenjang": "SMP", "alamat": "Jln. Raya Utara No. 106 Wangon" },
                  { "nama": "SMP Negeri 2 Wangon", "jenjang": "SMP", "alamat": "Jl.Raya Barat No.1112 Wangon" },
                  { "nama": "SMP Negeri 3 Wangon", "jenjang": "SMP", "alamat": "Jl Planjan Wangon" },
                  { "nama": "SMP DIPONEGORO 5 WANGON", "jenjang": "SMP", "alamat": "Jl. Lapangan Klapagading Kulon Wangon" },
                  { "nama": "SMP ISLAM EL CHALIM", "jenjang": "SMP", "alamat": "RT 02 RW 05" },
                  { "nama": "SMP MUHAMMADIYAH I WANGON", "jenjang": "SMP", "alamat": "Jalan Raya Utara Wangon" },
                  { "nama": "SMP PGRI WANGON", "jenjang": "SMP", "alamat": "Jl. Raya Selatan No. 50 Wangon" },
                  { "nama": "MTsS Daarus Sunnah Wangon", "jenjang": "SMP", "alamat": "JL. Raya Wangon - Ajibarang Km 3" },
                  { "nama": "MTsS MA`ARIF NU 1 WANGON", "jenjang": "SMP", "alamat": "JL. RAYA TIMUR WANGON RT.01/06" },
                  { "nama": "MTsS MUHAMMADIYAH WANGON", "jenjang": "SMP", "alamat": "JL. ASTANA NO. 915 RT.02/06 WANGON" },
                  { "nama": "SMA NEGERI 1 WANGON", "jenjang": "SMA", "alamat": "JL. PEJARAKAN KELAPA GADING", "kode_pos": "53176" },
                  { "nama": "SMKS BUNDA SATRIA WANGON", "jenjang": "SMK", "alamat": "JL. RAYA UTARA WANGON", "kode_pos": "53176" },
                  { "nama": "SMKS MAARIF NU 1 WANGON", "jenjang": "SMK", "alamat": "JL. KARANGJENGKOL - WANGON - BANYUMAS", "kode_pos": "53176" },
                  { "nama": "SMKS SRIWIJAYA 2 WANGON", "jenjang": "SMK", "alamat": "JL. RAYA WANGON - CILACAP KM.01", "kode_pos": "53176" }
                ],
                "JATILAWANG": [
                  { "nama": "SMP Negeri 1 Jatilawang", "jenjang": "SMP", "alamat": "Jatilawang" },
                  { "nama": "SMP Negeri 2 Jatilawang", "jenjang": "SMP", "alamat": "Desa Gentawangi" },
                  { "nama": "SMP BOARDING SCHOOL QUEEN BUMI AL FALAH JATILAWANG", "jenjang": "SMP", "alamat": "Jl. Bantar No.70 (Komplek Ponpes Al Falah) Tinggarjaya, Kec. Jatilawang" },
                  { "nama": "SMP KARYA BAKTI JATILAWANG", "jenjang": "SMP", "alamat": "Jl. Pramuka No. 2 Jatilawang" },
                  { "nama": "SMP MUHAMMADIYAH JATILAWANG", "jenjang": "SMP", "alamat": "Jl. Raya Tinggarjaya No.1912" },
                  { "nama": "SMP PANCASILA JATILAWANG", "jenjang": "SMP", "alamat": "Jl. Raya Jatilawang No. 17" },
                  { "nama": "MTs Plus Al Falah Jatilawang", "jenjang": "SMP", "alamat": "Jl. Pesantren Mangunsari RT 003/ RW 007" },
                  { "nama": "MTsS MA`ARIF NU 1 JATILAWANG", "jenjang": "SMP", "alamat": "JL. RAYA TINGGARJAYA NO. 1051 RT 01/06" }
                ],
                "RAWALO": [
                  { "nama": "SMP Negeri 1 Rawalo", "jenjang": "SMP", "alamat": "Jl. Jenderal Sudirman No. 2 Menganti" },
                  { "nama": "SMP Negeri 2 Rawalo", "jenjang": "SMP", "alamat": "Sidamulih Rawalo" },
                  { "nama": "SMP DIPONEGORO 8 RAWALO", "jenjang": "SMP", "alamat": "Jl. Raya Tambaknegara-rawalo-banyumas 53173" },
                  { "nama": "SMP Islam Al Falah Rawalo", "jenjang": "SMP", "alamat": "Jl. HM. Bachroen No. 35" },
                  { "nama": "SMP MUHAMMADIYAH RAWALO", "jenjang": "SMP", "alamat": "Jl. Brigjend H.M. Bachrun" },
                  { "nama": "MTs PESANTREN EL MADANI", "jenjang": "SMP", "alamat": "Jalan Kedungwangkal RT 01 RW 01" },
                  { "nama": "MTsS MA`ARIF NU 1 RAWALO", "jenjang": "SMP", "alamat": "Desa Tipar, RT 001 RW 006, Kecamatan Rawalo Kabupaten Banyumas" },
                  { "nama": "MTsS MA`ARIF NU 2 RAWALO", "jenjang": "SMP", "alamat": "JL. HM. Bachroen No.01 Banjarparakan" },
                  { "nama": "MTsS MIFTAHUL HUDA RAWALO", "jenjang": "SMP", "alamat": "PESAWAHAN,RT.02/ IV RAWALO" },
                  { "nama": "MTsS NU AL MUJAHIDIN RAWALO", "jenjang": "SMP", "alamat": "Jl. Rawalo Cilacap" },
                  { "nama": "SMAN 1 RAWALO", "jenjang": "SMA", "alamat": "Jl. Pawiyatan No. 1", "kode_pos": "53174" },
                  { "nama": "SMAS EL-MADANI RAWALO", "jenjang": "SMA", "alamat": "KEDUNGWANGKAL, BANJARPARAKAN RT 01 RW 01 RAWALO", "kode_pos": "53174" },
                  { "nama": "SMKS DIPONEGORO 2 RAWALO", "jenjang": "SMK", "alamat": "Jl. Raya Tambaknegara", "kode_pos": "53174" },
                  { "nama": "SMKS MAARIF NU 1 RAWALO", "jenjang": "SMK", "alamat": "JL. SERAYU II PEGADUNGAN RT 2/6 RAWALO 53173 BANYUMAS", "kode_pos": "53174" },
                  { "nama": "SMKS MIFTAHUL HUDA", "jenjang": "SMK", "alamat": "JL. RAYA PESAWAHAN RAWALO", "kode_pos": "53174" },
                  { "nama": "SMKS TEKNIK KOMPUTER MBM RAWALO", "jenjang": "SMK", "alamat": "JL. PESANTREN NO. 3 PESAWAHAN RAWALO", "kode_pos": "53174" }
                ],
                "KEBASEN": [
                  { "nama": "SMP Negeri 1 Kebasen", "jenjang": "SMP", "alamat": "Jl. Ngalisiswi RT 01 RW 04 Kalisalak" },
                  { "nama": "SMP Negeri 2 Kebasen", "jenjang": "SMP", "alamat": "Jl. Desa Adisana Kebasen" },
                  { "nama": "SMP Negeri 3 Kebasen", "jenjang": "SMP", "alamat": "Mandirancan - Kebasen - Banyumas" },
                  { "nama": "SMP ISLAM ANDALUSIA 2 KEBASEN", "jenjang": "SMP", "alamat": "Dusun Leler, Desa Randegan RT 01/ RW 02" },
                  { "nama": "SMP ISLAM ANDALUSIA KEBASEN", "jenjang": "SMP", "alamat": "Dusun Leler Desa Randegan RT 04 RW 01" },
                  { "nama": "SMP MUHAMMADIYAH KEBASEN", "jenjang": "SMP", "alamat": "Kebasen Banyumas" },
                  { "nama": "SMP PGRI KEBASEN", "jenjang": "SMP", "alamat": "JL. PUTERAN" },
                  { "nama": "MTsS MA`ARIF NU 1 KEBASEN", "jenjang": "SMP", "alamat": "JL. RAYA KALISALAK NO 7 KALISALAK KEC. KEBASEN KAB. BANYUMAS" },
                  { "nama": "MTSS Sains Karimiyya", "jenjang": "SMP", "alamat": "Jalan Balai Desa Nomor 3 Leler RT 01 RW 02" },
                  { "nama": "SMA ISLAM ANDALUSIA KEBASEN", "jenjang": "SMA", "alamat": "Desa Randegan RT 02 RW 01", "kode_pos": "53172" },
                  { "nama": "SMK PLUS TUNAS BANGSA KEBASEN", "jenjang": "SMK", "alamat": "JL. RAYA BUNTU SAMPANG DESA ADISANA", "kode_pos": "53172" },
                  { "nama": "SMKN KEBASEN", "jenjang": "SMK", "alamat": "RAYA BENTUL - KEBASEN", "kode_pos": "53172" },
                  { "nama": "SMKS TERPADU WIDYATAMA KEBASEN", "jenjang": "SMK", "alamat": "RAYA KALISALAK NO.35", "kode_pos": "53172" }
                ],
                "KEMRANJEN": [
                  { "nama": "SMP Negeri 1 Kemranjen", "jenjang": "SMP", "alamat": "Jl. Pramuka Karangjati Kemranjen" },
                  { "nama": "SMP Negeri 2 Kemranjen", "jenjang": "SMP", "alamat": "RT 03 RW 05" },
                  { "nama": "SMP MAARIF NU 1 KEMRANJEN", "jenjang": "SMP", "alamat": "Jalan Balai Desa Sirau" },
                  { "nama": "SMP MAARIF NU 2 KEMRANJEN", "jenjang": "SMP", "alamat": "Sirau RT.02 RW.02 Kemranjen" },
                  { "nama": "SMP MUHAMMADIYAH KEMRANJEN", "jenjang": "SMP", "alamat": "Kemranjen" },
                  { "nama": "SMP SALAFIYAH KEMRANJEN", "jenjang": "SMP", "alamat": "Jln. Raya Buntu-Gombong Km 02 Kebarongan Kemranjen Banyumas" },
                  { "nama": "SMP TAMTAMA KEMRANJEN", "jenjang": "SMP", "alamat": "Jl. Raya Buntu - Banyumas" },
                  { "nama": "MTs MA`ARIF HIDAYATUL MUBTADI`IEN", "jenjang": "SMP", "alamat": "Komplek Pondok Pesantren Hidayatul Mubtadi`ien Sumur Amba" },
                  { "nama": "MTs Ma`arif NU Assalam Kemranjen", "jenjang": "SMP", "alamat": "Jl. Masjid Baabussalaam Rt 006 Rw 001" },
                  { "nama": "MTsS MA`ARIF NU 1 KEMRANJEN", "jenjang": "SMP", "alamat": "JL. K.H. MOH. MUQRI, RT. 02 RW. 02, SIRAU, KEMRANJEN, BANYUMAS, JAWA TENGAH." },
                  { "nama": "MTsS MA`ARIF NU 2 KEMRANJEN", "jenjang": "SMP", "alamat": "RT 02/05 SIBRAMA KEMRANJEN BANYUMAS" },
                  { "nama": "MTsS MA`ARIF NU 3 KEMRANJEN", "jenjang": "SMP", "alamat": "Jl. Kecila-Petarangan Km.3 Petarangan Kemranjen Banyumas" },
                  { "nama": "MTsS MA`ARIF NU 4 KEMRANJEN", "jenjang": "SMP", "alamat": "JL. PASAR MURIA ALASMALANG KEMRANJEN, KABUPATEN BANYUMAS, JAWA TENGAH KODE POS" },
                  { "nama": "MTsS MUHAMMADIYAH SIRAU", "jenjang": "SMP", "alamat": "JL. BALAI DESA RT 2/7 SIRAU" },
                  { "nama": "MTsS Wathoniyah Islamiyah Kebarongan", "jenjang": "SMP", "alamat": "JL. RAYA BUNTU - SUMPIUH KM. 02 KEBARONGAN KEMRANJEN BANYUMAS" },
                  { "nama": "SMAS MA ARIF NU SIRAU KEMRANJEN", "jenjang": "SMA", "alamat": "Jl. Al Huda Sirau Kemranjen Banyumas 53194", "kode_pos": "53194" },
                  { "nama": "SMKS MAARIF NU 1 KEMRANJEN", "jenjang": "SMK", "alamat": "JL. RAYA SIBRAMA KM 0,6 - KEMRANJEN - BANYUMAS 53194", "kode_pos": "53194" },
                  { "nama": "SMKS MPU TANTULAR KEMRANJEN", "jenjang": "SMK", "alamat": "Jl. Raya Perempatan Buntu-Banyumas No. 222", "kode_pos": "53194" },
                  { "nama": "SMKS PPRQ KEMRANJEN", "jenjang": "SMK", "alamat": "Jalan Balai Desa Sirau RT.02 RW.02 Sirau Kecamatan Kemranjen", "kode_pos": "53194" }
                ],
                "SUMPIUH": [
                  { "nama": "SMP Negeri 1 Sumpiuh", "jenjang": "SMP", "alamat": "Jl.Raya Timur Sumpiuh  53195 Banyumas" },
                  { "nama": "SMP Negeri 2 Sumpiuh", "jenjang": "SMP", "alamat": "Jalan Giritomo" },
                  { "nama": "SMP GIRIPURO SUMPIUH", "jenjang": "SMP", "alamat": "Jl.Giritomo 15 Sumpiuh" },
                  { "nama": "SMP MUHAMMADIYAH SUMPIUH", "jenjang": "SMP", "alamat": "Jl. Raya Barat No. 14 Sumpiuh" },
                  { "nama": "SMP NURUL IMAN SUMPIUH", "jenjang": "SMP", "alamat": "Kedung Sampang RT 01 RW 01" },
                  { "nama": "SMP PURNAMA SUMPIUH", "jenjang": "SMP", "alamat": "Sumpiuh" },
                  { "nama": "MTsS MA`ARIF NU 1 SUMPIUH", "jenjang": "SMP", "alamat": "JL. RAYA SUMPIUH TIMUR NO IV/12A" },
                  { "nama": "MTsS MA`ARIF NU 2 SUMPIUH", "jenjang": "SMP", "alamat": "JL. DESA KETANDA RT 7/1 KETANDA" },
                  { "nama": "SMA NEGERI 1 SUMPIUH", "jenjang": "SMA", "alamat": "JL. RAYA BARAT NO. 95 SUMPIUH", "kode_pos": "53195" },
                  { "nama": "SMAS DIPONEGORO SUMPIUH", "jenjang": "SMA", "alamat": "JL. Bong Cina Kradenan", "kode_pos": "53195" },
                  { "nama": "SMKS GIRIPURO SUMPIUH", "jenjang": "SMK", "alamat": "JALAN GIRITOMO NO. 15 SUMPIUH", "kode_pos": "53195" },
                  { "nama": "SMKS KESEHATAN BHAKTI HUSADA", "jenjang": "SMK", "alamat": "JL. BONG CINA KRADENAN", "kode_pos": "53195" },
                  { "nama": "SMKS MAARIF NU 1 SUMPIUH", "jenjang": "SMK", "alamat": "JALAN RAYA SUMPIUH TIMUR NOMOR IV/12 A", "kode_pos": "53195" },
                  { "nama": "SMKS MUHAMMADIYAH SUMPIUH", "jenjang": "SMK", "alamat": "Jalan Somagede KM. 0,3", "kode_pos": "53195" },
                  { "nama": "SMKS TAMANSISWA SUMPIUH", "jenjang": "SMK", "alamat": "JL. NUSAWUNGU NO.4 SUMPIUH KABUPATEN BANYUMAS", "kode_pos": "53195" },
                  { "nama": "SMKS YPE SUMPIUH", "jenjang": "SMK", "alamat": "JALAN RAYA TIMUR SUMPIUH", "kode_pos": "53195" }
                ],
                "TAMBAK": [
                  { "nama": "SMP Negeri 1 Tambak", "jenjang": "SMP", "alamat": "Jl. Watuagung, Kamulyan, Tambak, Banyumas 53196" },
                  { "nama": "SMP Negeri 2 Tambak", "jenjang": "SMP", "alamat": "Jln. Karangpucung" },
                  { "nama": "SMP DIPONEGORO 9 TAMBAK", "jenjang": "SMP", "alamat": "Pondok Pesantren Sikeris Tambak Banyumas" },
                  { "nama": "SMP ISLAM AL FALAH TAMBAK", "jenjang": "SMP", "alamat": "Jl. KH. Yusuf Tsanawi" },
                  { "nama": "SMP MUHAMMADIYAH TAMBAK", "jenjang": "SMP", "alamat": "Jl. Karangpucung Tambak" },
                  { "nama": "SMP PGRI TAMBAK", "jenjang": "SMP", "alamat": "Tambak" },
                  { "nama": "SMP TAHFIDH AL HIDAYAH TAMBAK", "jenjang": "SMP", "alamat": "Jl. Raya Kauman RT 002 RW 001" },
                  { "nama": "MTSN 2 BANYUMAS", "jenjang": "SMP", "alamat": "Jl. Diponegoro No. 5 Kamulyan Rt 08 Rw 01" },
                  { "nama": "MTsS MA`ARIF NU 1 TAMBAK", "jenjang": "SMP", "alamat": "JL. MASJID AT TAQWA PLANGKAPAN TAMBAK" },
                  { "nama": "MTsS NAHDLOTUT TALAMIDZ JOMBOR", "jenjang": "SMP", "alamat": "JL. MASJID NO. 1 GUMELAR LOR TAMBAK BANYUMAS 53196" },
                  { "nama": "SMAS MUHAMMADIYAH TAMBAK", "jenjang": "SMA", "alamat": "JL. KARANGPUCUNG TAMBAK", "kode_pos": "53196" },
                  { "nama": "SMAS PGRI TAMBAK", "jenjang": "SMA", "alamat": "JL. KARANG PUCUNG TAMBAK", "kode_pos": "53196" },
                  { "nama": "SMK AL FALAH TAMBAK", "jenjang": "SMK", "alamat": "Jl. KH. Yusuf Tsanawi", "kode_pos": "53196" },
                  { "nama": "SMK WIDYA MANDALA TAMBAK", "jenjang": "SMK", "alamat": "Komplek Ponpes Sikeris", "kode_pos": "53196" }
                ],
                "SOMAGEDE": [
                  { "nama": "SMP ISLAM TERPADU IKADI BANYUMAS", "jenjang": "SMP", "alamat": "RT 01 RW 01" },
                  { "nama": "SMP Negeri 1 Somagede", "jenjang": "SMP", "alamat": "Jalan Raya Somagede" },
                  { "nama": "SMP Negeri 2 Somagede", "jenjang": "SMP", "alamat": "Jl. Tanggeran" },
                  { "nama": "SMP PGRI 1 SOMAGEDE", "jenjang": "SMP", "alamat": "Jl. Raya Somagede" },
                  { "nama": "SMP PGRI 2 SOMAGEDE", "jenjang": "SMP", "alamat": "JL. PRAMUKA No.2" },
                  { "nama": "MTs MA`ARIF NU 1 SOMAGEDE", "jenjang": "SMP", "alamat": "Jalan Sawangan RT 06 RW 04" },
                  { "nama": "SMKS MUHAMMADIYAH SOMAGEDE", "jenjang": "SMK", "alamat": "JL. RAYA SOMAGEDE KM.5 SOMAGEDE BANYUMAS", "kode_pos": "53193" }
                ],
                "KALIBAGOR": [
                  { "nama": "SMP Negeri 1 Kalibagor", "jenjang": "SMP", "alamat": "Jalan Suwarjono No. 162" },
                  { "nama": "SMP Negeri 2 Kalibagor", "jenjang": "SMP", "alamat": "Jln. Raya Kaliori - Kalibagor Banyumas" },
                  { "nama": "SMP Negeri 3 Kalibagor", "jenjang": "SMP", "alamat": "Jalan Kalianja" },
                  { "nama": "SMP Negeri 4 Kalibagor", "jenjang": "SMP", "alamat": "Jl. Bangin Srowot" },
                  { "nama": "SMP PGRI KALIBAGOR", "jenjang": "SMP", "alamat": "Jl. Suwardjono 164 Kalibagor" },
                  { "nama": "MTsS BAITUL MUSLIM KALIBAGOR", "jenjang": "SMP", "alamat": "JL.RAYA KALIBAGOR NO.10 KALIBAGOR" },
                  { "nama": "SMKS POLITEKNIK YP3I BANYUMAS", "jenjang": "SMK", "alamat": "JL. RAYA PURWOKERTO-BANYUMAS KM 12 KEC. KALIBAGOR KAB. BANYUMAS", "kode_pos": "53191" }
                ],
                "BANYUMAS": [
                  { "nama": "SMP Negeri 1 Banyumas", "jenjang": "SMP", "alamat": "Jl.Alun-alun No. 1  Banyumas" },
                  { "nama": "SMP Negeri 2 Banyumas", "jenjang": "SMP", "alamat": "Jalan Bhayangkara Nomor 6" },
                  { "nama": "SMP Negeri 3 Banyumas", "jenjang": "SMP", "alamat": "Jl Raya Kejawar Km 1" },
                  { "nama": "SMP Negeri 4 Banyumas", "jenjang": "SMP", "alamat": "Jl. Dayakan Desa Pasinggangan Rt. 02 Rw. 03" },
                  { "nama": "SMP ISLAM AL FATTAH BANYUMAS", "jenjang": "SMP", "alamat": "Desa Karangrau RT 04 RW 03" },
                  { "nama": "SMP MUHAMMADIYAH BANYUMAS", "jenjang": "SMP", "alamat": "Jl.Sudirman No.52" },
                  { "nama": "MTS MA`ARIF NU UNGGULAN BANYUMAS", "jenjang": "SMP", "alamat": "RT 004 RW 001" },
                  { "nama": "MTsS PPPI MIFTAHUSSALAM BANYUMAS", "jenjang": "SMP", "alamat": "JL. RAYA KEJAWAR NO. 72 RT 03 RW 01 BANYUMAS" }
                ],
                "PATIKRAJA": [
                  { "nama": "SMP Negeri 1 Patikraja", "jenjang": "SMP", "alamat": "Jl. Banyumas No. 09 Patikraja" },
                  { "nama": "SMP Negeri 2 Patikraja", "jenjang": "SMP", "alamat": "Jl. Balai Desa Kedungwuluh Lor" },
                  { "nama": "SMP MAARIF NU 1 PATIKRAJA", "jenjang": "SMP", "alamat": "Jl. Bahagia Rt 6/6" },
                  { "nama": "SMP YPE PATIKRAJA", "jenjang": "SMP", "alamat": "Jl. Raya Patikraja No:18" },
                  { "nama": "MTsS MA`ARIF NU 1 PATIKRAJA", "jenjang": "SMP", "alamat": "JL. RAYA KEDUNGRANDU RT 03/03 KEDUNGRANDU PATIKRAJA" },
                  { "nama": "MTsS MUHAMMADIYAH PATIKRAJA", "jenjang": "SMP", "alamat": "JL. RAYA BANYUMAS NO.09 PATIKRAJA RT 03 /III" },
                  { "nama": "SMA NEGERI 1 PATIKRAJA", "jenjang": "SMA", "alamat": "JL. ADIPURA NO. 3 PATIKRAJA", "kode_pos": "53171" },
                  { "nama": "SMK ARYASATYA TEKNOLOGI", "jenjang": "SMK", "alamat": "Jl. Raya Rawalo - Purwokerto", "kode_pos": "53171" }
                ],
                "PURWOJATI": [
                  { "nama": "SMP Negeri 1 Purwojati", "jenjang": "SMP", "alamat": "Jalan Karangtalun Kidul" },
                  { "nama": "SMP Negeri 2 Purwojati", "jenjang": "SMP", "alamat": "Jl. Inpres Desa Karangmangu" },
                  { "nama": "SMP MUHAMMADIYAH PURWOJATI", "jenjang": "SMP", "alamat": "Jln.Karangtalun Kidul" },
                  { "nama": "MTS MANBAUL FALAH GERDUREN", "jenjang": "SMP", "alamat": "Jalan Lingkar Masjid RT 001 RW 003" },
                  { "nama": "MTsS MA`ARIF NU 1 PURWOJATI", "jenjang": "SMP", "alamat": "JL. RAYA INPRES NO. 245 RT 01 RW 03" },
                  { "nama": "MTsS SA HIDAYATUL MUBTADI`IN PURWOJATI", "jenjang": "SMP", "alamat": "JL. GUNUNG PUTRI NO. 9" },
                  { "nama": "SMKN 1 PURWOJATI", "jenjang": "SMK", "alamat": "KLAPA SAWIT KECAMATAN PURWOJATI", "kode_pos": "53173" }
                ],
                "AJIBARANG": [
                  { "nama": "SMP Negeri 1 Ajibarang", "jenjang": "SMP", "alamat": "Jl. Raya No. 2 Ajibarang" },
                  { "nama": "SMP Negeri 2 Ajibarang", "jenjang": "SMP", "alamat": "Jl. Pandansari No. 1044 Ajibarang - Banyumas" },
                  { "nama": "SMP Negeri 3 Ajibarang", "jenjang": "SMP", "alamat": "Jalan Raya Ajibarang Timur No.63" },
                  { "nama": "SMP MAARIF NU 1 AJIBARANG", "jenjang": "SMP", "alamat": "Jl. Pandansari No 876 Ajibarang" },
                  { "nama": "SMP MAARIF NU 2 AJIBARANG", "jenjang": "SMP", "alamat": "Jalan Raya Ajibarang-Purwojati Km.7" },
                  { "nama": "SMP MUHAMMADIYAH AJIBARANG", "jenjang": "SMP", "alamat": "Jl. Prajurit Ambyah No.15" },
                  { "nama": "SMP PGRI 1 Ajibarang", "jenjang": "SMP", "alamat": "Jalan Raya Tiparkidul - Ajibarang" },
                  { "nama": "SMP PGRI 2 AJIBARANG", "jenjang": "SMP", "alamat": "Jl. Babakan Rt 02/03" },
                  { "nama": "MTsS MA`ARIF NU 1 AJIBARANG", "jenjang": "SMP", "alamat": "JL.PANDANSARI RT 02 RW 12 AJIBARANG WETAN" },
                  { "nama": "MTsS MODERN AL AZHARY AJIBARANG", "jenjang": "SMP", "alamat": "LESMANA" },
                  { "nama": "MTSS TAHFIZ AL-QUR`AN AR RAUDLAH AJIBARANG", "jenjang": "SMP", "alamat": "JL. RAYA AJIBARANG-WANGON TIPAR KIDUL RT 03/RW 04 AJIBARANG BANYUMAS 53163" }
                ],
                "GUMELAR": [
                  { "nama": "SMP Negeri 1 Gumelar", "jenjang": "SMP", "alamat": "JL. Raya Gumelar No. 31 Kec. Gumelar Kab. Banyumas" },
                  { "nama": "SMP Negeri 2 Gumelar", "jenjang": "SMP", "alamat": "Jalan Raya Paningkaban" },
                  { "nama": "SMP Negeri 3 Gumelar", "jenjang": "SMP", "alamat": "Desa Samudra, Kecamatan Gumelar" },
                  { "nama": "SMP DIPONEGORO 7 GUMELAR", "jenjang": "SMP", "alamat": "Barat Kua Gumelar" },
                  { "nama": "SMP PGRI GUMELAR", "jenjang": "SMP", "alamat": "Jl. Pramuka No 4 Gumelar" },
                  { "nama": "MTsS MA`ARIF NU 1 GUMELAR", "jenjang": "SMP", "alamat": "JL. CIBANGKONG-CIHONJE,( KEDUNGURANG RT 03/01 ) GUMELAR 53165" }
                ],
                "PEKUNCEN": [
                  { "nama": "SMP Negeri 1 Pekuncen", "jenjang": "SMP", "alamat": "Karangklesem" },
                  { "nama": "SMP Negeri 2 Pekuncen", "jenjang": "SMP", "alamat": "Jl. Raya Cikawung - Pekuncen  No. 6" },
                  { "nama": "SMP Negeri 3 Pekuncen", "jenjang": "SMP", "alamat": "Pekuncen" },
                  { "nama": "SMP DIPONEGORO 10 PEKUNCEN", "jenjang": "SMP", "alamat": "Jalan Stasiun Legok Pekuncen" },
                  { "nama": "SMP MAARIF NU 1 PEKUNCEN", "jenjang": "SMP", "alamat": "Jl. Raya Ajibarang-tegal Km 8" },
                  { "nama": "SMP TAKHASSUS AL QURAN PEKUNCEN", "jenjang": "SMP", "alamat": "Jl. Curug Cipendok No. 1 Tumiyang" },
                  { "nama": "MTS AR-RIDLO PEKUNCEN", "jenjang": "SMP", "alamat": "JL. SANTRI NO. 2 KOMPLEKS PONDOK PESANTREN RAUDLOTUL 'ILMI" },
                  { "nama": "MTS NURUL IMAN CIKEMBULAN PEKUNCEN", "jenjang": "SMP", "alamat": "Grumbul Rancah RT 02 RW 03" },
                  { "nama": "MTsS MA`ARIF NU 1 PEKUNCEN", "jenjang": "SMP", "alamat": "Jln. Banjaranyar - Pasiraman KM. 0,5 Danasri RT. 02 RW. 05" },
                  { "nama": "MTsS MUHAMMADIYAH PEKUNCEN", "jenjang": "SMP", "alamat": "RT 01/4 KRAJAN" },
                  { "nama": "SMK PONDOK PESANTREN HILYATUL QUR AN (PPHQ) PEKUNCEN", "jenjang": "SMK", "alamat": "Jl. Curug Cipendok No.1 Tumiyang", "kode_pos": "53164" },
                  { "nama": "SMKS MAARIF NU 1 PEKUNCEN", "jenjang": "SMK", "alamat": "JL. BANJARANYAR PASIRAMAN KM 0,5 PEKUNCEN BANYUMAS", "kode_pos": "53164" }
                ],
                "CILONGOK": [
                  { "nama": "SMP Negeri 1 Cilongok", "jenjang": "SMP", "alamat": "Jl. Raya Pernasidi - Cilongok - Banyumas" },
                  { "nama": "SMP Negeri 2 Cilongok", "jenjang": "SMP", "alamat": "Jalan Singadipa No 1 Panembangan Cilongok Banyumas" },
                  { "nama": "SMP Negeri 3 Cilongok", "jenjang": "SMP", "alamat": "Jl. Raya Kasegeran Km. 5" },
                  { "nama": "SMP AL HAMRA CILONGOK", "jenjang": "SMP", "alamat": "RT 01 RW 01" },
                  { "nama": "SMP ALAM AL AQWIYA CILONGOK", "jenjang": "SMP", "alamat": "Desa Langgongsari Rt 06 Rw 05 Kec. Cilongok" },
                  { "nama": "SMP MAARIF NU 1 CILONGOK", "jenjang": "SMP", "alamat": "Jl. Masjid Kauman Cilongok" },
                  { "nama": "SMP MAHAD DARUSSAADAH", "jenjang": "SMP", "alamat": "Desa Gununglurah, Kec. Cilongok Kab. Banyumas" },
                  { "nama": "SMP MUHAMMADIYAH BOARDING SCHOOL ZAM-ZAM CILONGOK", "jenjang": "SMP", "alamat": "Pernasidi Rt 01 Rw 05" },
                  { "nama": "SMP MUHAMMADIYAH CILONGOK", "jenjang": "SMP", "alamat": "Jl. Masjid Pernasidi" },
                  { "nama": "SMP PGRI 1 CILONGOK", "jenjang": "SMP", "alamat": "Jl. Kelurahan Cikidang" },
                  { "nama": "SMP PGRI 2 CILONGOK", "jenjang": "SMP", "alamat": "Jalan Raya Panusupan" },
                  { "nama": "SMP ZAMZAM INTEGRATED ISLAMIC SCHOOL", "jenjang": "SMP", "alamat": "JL.RAYA CIKIDANG NO. 13" },
                  { "nama": "An Najah Cilongok", "jenjang": "SMP", "alamat": "Jl. Kalipancur Rt 03 Rw 03" },
                  { "nama": "MTs DARUSSALAMAH", "jenjang": "SMP", "alamat": "Jl. Puteran No. 07 RT 03 RW 02" },
                  { "nama": "MTs Ma`arif NU 3 Cilongok", "jenjang": "SMP", "alamat": "Komplek Masjid Baiturrohmah Rt 6 Rw 5" },
                  { "nama": "MTsS BIROYATUL HUDA", "jenjang": "SMP", "alamat": "JL. K.H. ISMAIL RT 01 RW 01" },
                  { "nama": "MTsS DARUSSALAM CILONGOK", "jenjang": "SMP", "alamat": "PANUSUPAN RT 04 RW 02" },
                  { "nama": "MTsS MA`ARIF NU 1 CILONGOK", "jenjang": "SMP", "alamat": "JL. MASJID NO. 1 CILONGOK" },
                  { "nama": "MTsS MA`ARIF NU 2 CILONGOK", "jenjang": "SMP", "alamat": "JL. CILEWENG PANEMBANGAN" }
                ],
                "KARANGLEWAS": [
                  { "nama": "SMP Negeri 1 Karanglewas", "jenjang": "SMP", "alamat": "Jl. Raya Tamansari" },
                  { "nama": "SMP Negeri 2 Karanglewas", "jenjang": "SMP", "alamat": "Pangebatan" },
                  { "nama": "SMP Negeri 3 Karanglewas", "jenjang": "SMP", "alamat": "Jalan Raya Kejubug" },
                  { "nama": "SMP MUHAMMADIYAH 1 KARANGLEWAS", "jenjang": "SMP", "alamat": "Jln. Damaraja" },
                  { "nama": "SMP MUHAMMADIYAH 2 KARANGLEWAS", "jenjang": "SMP", "alamat": "Jalan Jaya Diwangsa No. 43 Karanglewas" },
                  { "nama": "MTsS MA`ARIF NU 1 KARANGLEWAS", "jenjang": "SMP", "alamat": "JL. DESA BABAKAN KARANGLEWAS" },
                  { "nama": "SMKS IT MAARIF NU KARANGLEWAS", "jenjang": "SMK", "alamat": "DESA BABAKAN", "kode_pos": "53161" },
                  { "nama": "SMKS MAARIF NU 2 KARANGLEWAS", "jenjang": "SMK", "alamat": "JALAN SYEKH MAQDUM WALI - KARANGLEWAS", "kode_pos": "53161" }
                ],
                "KEDUNG BANTENG": [
                  { "nama": "SMP Negeri 1 Kedungbanteng", "jenjang": "SMP", "alamat": "Jl. Raya Kedungbanteng" },
                  { "nama": "SMP Negeri 2 Kedungbanteng", "jenjang": "SMP", "alamat": "Jalan Raya Keniten" },
                  { "nama": "SMP Negeri 3 Kedungbanteng", "jenjang": "SMP", "alamat": "Kedung Banteng" },
                  { "nama": "SMP Negeri 4 Kedungbanteng", "jenjang": "SMP", "alamat": "Jl.Raya Rabuk-Baseh" },
                  { "nama": "SMP DIPONEGORO 3 KEDUNGBANTENG", "jenjang": "SMP", "alamat": "Jl. Raya Kedungbanteng" },
                  { "nama": "SMP ISLAM AL AZHAR 63 PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. Raya Kedungbanteng RT 02 RW 01" },
                  { "nama": "SMP MUHAMMADIYAH BEJI KEDUNGBANTENG", "jenjang": "SMP", "alamat": "Jl.R.Soepeno No.03 Beji" },
                  { "nama": "SMP QARYAH THAYYIBAH PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. Raya Beji Gang Kampus" },
                  { "nama": "MTsS AL IKHSAN BEJI KEDUNGBANTENG", "jenjang": "SMP", "alamat": "KOMP. PON. PES. AL-IKHSAN BEJI RT 04 RW 02" },
                  { "nama": "MTSS ANWAARUL HIDAYAH KEDUNGBANTENG", "jenjang": "SMP", "alamat": "Jl. Lingkar Utara Desa Karangnangka RT 03 RW 01" },
                  { "nama": "MTsS MA`ARIF NU 1 KEDUNGBANTENG", "jenjang": "SMP", "alamat": "JL.RAYA KEDUNGBANTENG 33  Rt. 01 Rw. 03" },
                  { "nama": "SMAN 3 PURWOKERTO", "jenjang": "SMA", "alamat": "JL. KAMANDAKA BARAT NO. 3", "kode_pos": "53152" },
                  { "nama": "SMKS DIPONEGORO 3 KEDUNGBANTENG", "jenjang": "SMK", "alamat": "JALAN RAYA KEDUNGBANTENG", "kode_pos": "53152" }
                ],
                "BATURADEN": [
                  { "nama": "SMP DARUL QURAN AL KARIM BATURRADEN", "jenjang": "SMP", "alamat": "Jl. Raya Baturraden Barat RT 003 RW 004" },
                  { "nama": "SMP Negeri 1 Baturraden", "jenjang": "SMP", "alamat": "Jl. Raya Rempoah Barat" },
                  { "nama": "SMP Negeri 2 Baturraden", "jenjang": "SMP", "alamat": "Jl. Kemutug Kidul" },
                  { "nama": "Sekolah Rakyat Menengah Pertama 13 Banyumas", "jenjang": "SMP", "alamat": "Jl. Raya Barat Baturraden No. 35, Desa Ketenger, Kec. Baturraden,Banyumas" },
                  { "nama": "SMP BOARDING SCHOOL AL IRSYAD AL ISLAMIYYAH PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. Raya Kebumen-Baturaden, RT 08/ RW 04" },
                  { "nama": "SMP PGRI BATURRADEN", "jenjang": "SMP", "alamat": "Jl. Raya Baturraden Timur" },
                  { "nama": "SMP SAINS AN NAJAH PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. Raya Kutasari Baturraden" },
                  { "nama": "MTsS AL MASRURIYAH BATURADEN", "jenjang": "SMP", "alamat": "JL. Ponpes Al Masruriyah RT 01 RW 02" }
                ],
                "SUMBANG": [
                  { "nama": "SMP Negeri 1 Sumbang", "jenjang": "SMP", "alamat": "Jl. Raya Sumbang" },
                  { "nama": "SMP Negeri 2 Sumbang", "jenjang": "SMP", "alamat": "Jl. Raya Banteran Sumbang" },
                  { "nama": "SMP Negeri 3 Sumbang", "jenjang": "SMP", "alamat": "Jln. Raya Baturaden Timur RT. 3 RW. 2" },
                  { "nama": "SMP Negeri 4 Sumbang", "jenjang": "SMP", "alamat": "Desa Susukan" },
                  { "nama": "SMP MUHAMMADIYAH SUMBANG", "jenjang": "SMP", "alamat": "Jl. Raya Karangcegak" },
                  { "nama": "SMP PGRI SUMBANG", "jenjang": "SMP", "alamat": "Jalan Raya Sumbang" },
                  { "nama": "MTSN 3 BANYUMAS", "jenjang": "SMP", "alamat": "JL. RAYA SILADO" },
                  { "nama": "MTsS MA`ARIF NU 1 SUMBANG", "jenjang": "SMP", "alamat": "JL. Raya Banteran RT 2/2" },
                  { "nama": "SMKS DEWANTARA SUMBANG", "jenjang": "SMK", "alamat": "DESA BANTERAN KEC. SUMBANG", "kode_pos": "53183" },
                  { "nama": "SMKS MULYA HUSADA", "jenjang": "SMK", "alamat": "JL. BATURRADEN TIMUR NO.57 KARANGCEGAK SUMBANG 53183", "kode_pos": "53183" },
                  { "nama": "SMKS TAMANSISWA PURWOKERTO", "jenjang": "SMK", "alamat": "Jl.Sunan Ampel RT 03 RW 01 Kedungmalang", "kode_pos": "53183" }
                ],
                "KEMBARAN": [
                  { "nama": "SMP Negeri 1 Kembaran", "jenjang": "SMP", "alamat": "Kembaran" },
                  { "nama": "SMP Negeri 2 Kembaran", "jenjang": "SMP", "alamat": "Jl. Raya Purwodadi" },
                  { "nama": "SMP GUNUNGJATI KEMBARAN", "jenjang": "SMP", "alamat": "Jl. R. Patah 911 A" },
                  { "nama": "SMP ISLAM DARUSSALAM PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. Sunan Bonang" },
                  { "nama": "SMP ISLAM TERPADU NUSANTARA KEMBARAN", "jenjang": "SMP", "alamat": "RT 005 RW 002" },
                  { "nama": "SMP ISLAM WALISONGO KEMBARAN", "jenjang": "SMP", "alamat": "Jl. Ponpes Mambaul Ushulil Hikmah" },
                  { "nama": "SMP MUHAMMADIYAH KEMBARAN", "jenjang": "SMP", "alamat": "Kembaran Banyumas" },
                  { "nama": "SMP UMP", "jenjang": "SMP", "alamat": "Jl. Senopati No.17" },
                  { "nama": "MTsS MA`ARIF NU 1 KEMBARAN", "jenjang": "SMP", "alamat": "KARANGSARI RT 3/III" },
                  { "nama": "MTsS SA RAUDHOTUT THOLIBIN KEMBARAN", "jenjang": "SMP", "alamat": "JL. PESANTREN RT 02 RW 07DUKUHWALUH" },
                  { "nama": "SMK KESEHATAN KESATRIAN 2 PURWOKERTO", "jenjang": "SMK", "alamat": "Jl. Sunan Bonang Dukuhwaluh Kembaran", "kode_pos": "53182" },
                  { "nama": "SMK Mulia Bakti Purwokerto", "jenjang": "SMK", "alamat": "Jl. Sunan Bonang No.121 Dukuhwaluh", "kode_pos": "53182" },
                  { "nama": "SMKS MAARIF NU 1 KEMBARAN", "jenjang": "SMK", "alamat": "PONPES MAMBAUL USHULIL HIKMAH", "kode_pos": "53182" }
                ],
                "SOKARAJA": [
                  { "nama": "SMP PERSADA INSAN NUSANTARA", "jenjang": "SMP", "alamat": "Jl. Desa Kusuma II" },
                  { "nama": "SMP Negeri 1 Sokaraja", "jenjang": "SMP", "alamat": "Jl. Jend. Sudirman No.  82" },
                  { "nama": "SMP Negeri 2 Sokaraja", "jenjang": "SMP", "alamat": "Jl. Letjend. Soepardjo Roestam No. 168 Sokaraja" },
                  { "nama": "SMP Negeri 3 Sokaraja", "jenjang": "SMP", "alamat": "Jalan Brawijaya" },
                  { "nama": "SMP IT ANNIDA SOKARAJA", "jenjang": "SMP", "alamat": "Jalan Suparjo Rustam Perumahan Ketapang Indah D4 Nomor 3" },
                  { "nama": "SMP IT MUTIARA ILMU SOKARAJA", "jenjang": "SMP", "alamat": "Jalan Dipa Setra No. 3 RT 04 RW 01" },
                  { "nama": "SMP MUHAMMADIYAH SOKARAJA", "jenjang": "SMP", "alamat": "Jl. Karangbangkang No.27 Sokaraja" },
                  { "nama": "SMP TOP KIDS ISLAMIC SCHOOL", "jenjang": "SMP", "alamat": "RT 002 RW 005" },
                  { "nama": "MTsS MA`ARIF NU 1 SOKARAJA", "jenjang": "SMP", "alamat": "JL. K. MURSYID SOKARAJA LOR" },
                  { "nama": "SMAN 1 SOKARAJA", "jenjang": "SMA", "alamat": "JL. RAYA SOKARAJA  TIMUR", "kode_pos": "53181" },
                  { "nama": "SMAS BUDI UTOMO SOKARAJA", "jenjang": "SMA", "alamat": "JL. MENTERI SUPENO NO.07", "kode_pos": "53181" },
                  { "nama": "SMAS MA ARIF NU SOKARAJA", "jenjang": "SMA", "alamat": "JL. KY. ACH. MURSYID SOKARAJA", "kode_pos": "53181" },
                  { "nama": "SMAS MUHAMMADIYAH SOKARAJA", "jenjang": "SMA", "alamat": "JL. PRAMUKA N0.24 SOKARAJA", "kode_pos": "53181" },
                  { "nama": "SMKS BUDI UTOMO SOKARAJA", "jenjang": "SMK", "alamat": "JL. MENTERI SOEPENO NO. 07 SOKARAJA TELP. (0281) 694153", "kode_pos": "53181" },
                  { "nama": "SMKS YOS SUDARSO SOKARAJA", "jenjang": "SMK", "alamat": "JL. SUPARJO RUSTAM - Tromol POS 1 Telp. (0281) 639369 Kec. Sokaraja, Banyumas", "kode_pos": "53181" }
                ],
                "PURWOKERTO SELATAN": [
                  { "nama": "SMP Negeri 5 Purwokerto", "jenjang": "SMP", "alamat": "Jl. Prof. Mr. Moch. Yamin No. 867 Purwokerto" },
                  { "nama": "SMP Negeri 7 Purwokerto", "jenjang": "SMP", "alamat": "Jl. HOS Notosuwiryo No.1" },
                  { "nama": "SMP DIPONEGORO 1 PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. Karangbenda" },
                  { "nama": "SMP ISLAM TERPADU HARAPAN BUNDA", "jenjang": "SMP", "alamat": "JL.Hos Notosuwiryo No.5 Teluk Purwokerto Selatan" },
                  { "nama": "SMP MAARIF NU 03 PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. Arsadimeja Teluk Rt 01 / Rw 12 Purwokerto" },
                  { "nama": "SMP MAARIF NU 2 PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. Pancurawis" },
                  { "nama": "SMP MUHAMMADIYAH 1 PURWOKERTO", "jenjang": "SMP", "alamat": "Jalan Perintis Kemerdekaan No 6 Purwokerto" },
                  { "nama": "SMP MUHAMMADIYAH 2 PURWOKERTO", "jenjang": "SMP", "alamat": "JL GERILYA BARAT GANG II, TANJUNG, PWT SELATAN" },
                  { "nama": "SMP TELKOM PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. DI Panjaitan 128 Purwokerto" },
                  { "nama": "SMA NASIONAL 3 BAHASA PUTERA HARAPAN", "jenjang": "SMA", "alamat": "Jl. S. Parman Kompleks Stadion Mini", "kode_pos": "53143" },
                  { "nama": "SMAS JENDERAL SUDIRMAN PWT", "jenjang": "SMA", "alamat": "JL. GERILYA TIMUR", "kode_pos": "53143" },
                  { "nama": "SMKS BINA TEKNOLOGI PURWOKERTO", "jenjang": "SMK", "alamat": "JL.PAHLAWAN VI/18 TANJUNG PURWOKERTO TELP/FAX (0281) 638328", "kode_pos": "53143" },
                  { "nama": "SMKS CITRA BANGSA MANDIRI PURWOKERTO", "jenjang": "SMK", "alamat": "Jl. Gerilya Barat Gg.1A- Tanjung, Kampoeng PendidikanCBM - Purwokerto", "kode_pos": "53143" },
                  { "nama": "SMKS DIPONEGORO 1 PURWOKERTO", "jenjang": "SMK", "alamat": "JL. KARANGBENDA RAYA, BERKOH, PURWOKERTO SELATAN TELP. (0281) 623407", "kode_pos": "53143" },
                  { "nama": "SMKS MAARIF NU 1 PURWOKERTO", "jenjang": "SMK", "alamat": "JALAN PERUM GRIYA TELUK BARU NO.1 PAMUJAN", "kode_pos": "53143" },
                  { "nama": "SMKS MUHAMMADIYAH 3 PURWOKERTO", "jenjang": "SMK", "alamat": "JL. KH. WACHID HASJIM NO. 271 PURWOKERTO", "kode_pos": "53143" },
                  { "nama": "SMKS NASIONAL PURWOKERTO", "jenjang": "SMK", "alamat": "JL.KH.WAHID HASYIM NO.93 PURWOKERTO", "kode_pos": "53143" },
                  { "nama": "SMKS SWAGAYA 2 PURWOKERTO", "jenjang": "SMK", "alamat": "JL. PROF. MR. MUCH YAMIN XI/4 PURWOKERTO", "kode_pos": "53143" },
                  { "nama": "SMKS TEKNOLOGI NASIONAL PURWOKERTO", "jenjang": "SMK", "alamat": "JL. KH. AGUS SALIM NOMOR 98 PURWOKERTO SELATAN", "kode_pos": "53143" },
                  { "nama": "SMKS TELEKOMUNIKASI SANDHY PUTRA PURWOKERTO", "jenjang": "SMK", "alamat": "JL. DI PANJAITAN NO.128 PURWOKERTO", "kode_pos": "53143" },
                  { "nama": "SMKS TUJUH LIMA 1 PURWOKERTO", "jenjang": "SMK", "alamat": "JL. MARGANTARA TANJUNG", "kode_pos": "53143" },
                  { "nama": "SMKS TUJUH LIMA 2 PURWOKERTO", "jenjang": "SMK", "alamat": "JL. MARGANTARA TANJUNG PURWOKERTO", "kode_pos": "53143" }
                ],
                "PURWOKERTO BARAT": [
                  { "nama": "SMP Negeri 10 Purwokerto", "jenjang": "SMP", "alamat": "Jl. H. Mashuri No. 39 Kelurahan Rejasari Kecamatan Purwokerto Barat" },
                  { "nama": "SMP Negeri 4 Purwokerto", "jenjang": "SMP", "alamat": "Jalan Kertawibawa No. 575" },
                  { "nama": "SMP BOARDING SCHOOL PUTRA HARAPAN PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. Ks Tubun No. 4 Rejasari, Purwokerto Barat" },
                  { "nama": "SMP GUNUNGJATI 1 PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. Tentara Pelajar No. 17 Purwokerto" },
                  { "nama": "SMP GUNUNGJATI 2 PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. Jend. Sutoyo Gg.2 Purwokerto" },
                  { "nama": "MTsS AL HIDAYAH PURWOKERTO BARAT", "jenjang": "SMP", "alamat": "Jl. Ks. Tubun Gg. Madrasah RT 001 RW 007" },
                  { "nama": "MTsS MA`ARIF NU 1 PURWOKERTO BARAT", "jenjang": "SMP", "alamat": "JL. ACH. ZEIN NO. 185 RT 3/II PASIR KIDUL" },
                  { "nama": "SMAS BOARDING SCHOOL PUTRA HARAPAN PURWOKERTO", "jenjang": "SMA", "alamat": "JL. KS TUBUN GANG SLOBOR NO.3 KOBER PURWOKERTO BARAT", "kode_pos": "53132" },
                  { "nama": "SMKS MUHAMMADIYAH 1 PURWOKERTO", "jenjang": "SMK", "alamat": "JL.LAKSDA. YOS SUDARSO 9", "kode_pos": "53132" },
                  { "nama": "SMKS WIWOROTOMO PURWOKERTO", "jenjang": "SMK", "alamat": "JL.LAKSDA YOS SUDARSO NO. 3", "kode_pos": "53132" }
                ],
                "PURWOKERTO TIMUR": [
                  { "nama": "SMP Negeri 1 Purwokerto", "jenjang": "SMP", "alamat": "Jalan Jendral Sudirman No. 181, Purwokerto" },
                  { "nama": "SMP Negeri 2 Purwokerto", "jenjang": "SMP", "alamat": "Jalan Gereja 20 Purwokerto" },
                  { "nama": "SMP Negeri 3 Purwokerto", "jenjang": "SMP", "alamat": "Jl. Gereja No.20 Purwokerto" },
                  { "nama": "SMP Negeri 6 Purwokerto", "jenjang": "SMP", "alamat": "Jl. Ksatrian No.83 Purwokerto" },
                  { "nama": "SMP Negeri 8 Purwokerto", "jenjang": "SMP", "alamat": "Jl. Kapten Piere Tendean No. 36" },
                  { "nama": "SMP AL IRSYAD AL ISLAMIYYAH PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. Prof. Dr. Soeharso Purwokerto" },
                  { "nama": "SMP BRUDERAN PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. Jend. Gatot Subroto No 63 Purwokerto" },
                  { "nama": "SMP KRISTEN 1 PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. Jend. Gatot Subroto 91 Purwokerto" },
                  { "nama": "SMP MAARIF NU 1 PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. Dr. Soeparno 19 Purwokerto" },
                  { "nama": "SMP PERMATA HATI", "jenjang": "SMP", "alamat": "RT 01, RW 07, Kelurahan Mersi, Kecamatan Purwokerto Timur" },
                  { "nama": "SMP SUSTERAN PURWOKERTO", "jenjang": "SMP", "alamat": "Jln. Jendral Gatot Subroto 44 Purwokerto" },
                  { "nama": "MTSN 1 BANYUMAS", "jenjang": "SMP", "alamat": "Jl.Jend.Soedirman No.791" },
                  { "nama": "MTsS MUHAMMADIYAH PURWOKERTO", "jenjang": "SMP", "alamat": "JL. OVERSTE ISDIMAN III/20 PURWOKERTO" },
                  { "nama": "SMAN 1 PURWOKERTO", "jenjang": "SMA", "alamat": "JL. JEND. GATOT SUBROTO NO. 73 PURWOKERTO", "kode_pos": "53114" },
                  { "nama": "SMAN 2 PURWOKERTO", "jenjang": "SMA", "alamat": "JL. GATOT SUBROTO NO. 69 PWT", "kode_pos": "53114" },
                  { "nama": "SMAN 4 PURWOKERTO", "jenjang": "SMA", "alamat": "JL. LETKOL ISDIMAN NO.9 Purwokerto", "kode_pos": "53114" },
                  { "nama": "SMAN 5 PURWOKERTO", "jenjang": "SMA", "alamat": "JL. GEREJA NO. 20 PURWOKERTO", "kode_pos": "53114" },
                  { "nama": "SMAS AL IRSYAD AL ISLAMIYYAH", "jenjang": "SMA", "alamat": "Jalan Prof. Dr. Suharso", "kode_pos": "53114" },
                  { "nama": "SMAS BRUDERAN PURWOKERTO", "jenjang": "SMA", "alamat": "JL. JEND. GATOT SUBROTO NO. 63", "kode_pos": "53114" },
                  { "nama": "SMAS KRISTEN PURWOKERTO", "jenjang": "SMA", "alamat": "JL. JEND. GATOT SUBROTO NO. 89 PURWOKERTO", "kode_pos": "53114" },
                  { "nama": "SMAS MUHAMMADIYAH 1 PURWOKERTO", "jenjang": "SMA", "alamat": "JL. DOKTER ANGKA NO. 1 PURWOKERTO", "kode_pos": "53114" },
                  { "nama": "SMAS VETERAN PURWOKERTO", "jenjang": "SMA", "alamat": "JL. DR. ANGKA NO. 56 PURWOKERTO", "kode_pos": "53114" },
                  { "nama": "SMKN 1 PURWOKERTO", "jenjang": "SMK", "alamat": "JL. DR. SOEPARNO NO. 29", "kode_pos": "53114" },
                  { "nama": "SMKN 2 PURWOKERTO", "jenjang": "SMK", "alamat": "JL. JENDERAL GATOT SUBROTO NO.81 PURWOKERTO KABUPATEN BANYUMAS JAWA TENGAH", "kode_pos": "53114" },
                  { "nama": "SMKN 3 PURWOKERTO", "jenjang": "SMK", "alamat": "JL. A.YANI NO.70 TELP.(0281) 637847 PURWOKERTO", "kode_pos": "53114" },
                  { "nama": "SMKS BAKTI PURWOKERTO", "jenjang": "SMK", "alamat": "JALAN DR SUPARNO 13 PURWOKERTO", "kode_pos": "53114" },
                  { "nama": "SMKS BINA TARUNA PURWOKERTO", "jenjang": "SMK", "alamat": "JL. HM. BACHROEN NO.15 B", "kode_pos": "53114" },
                  { "nama": "SMKS KESATRIAN PURWOKERTO", "jenjang": "SMK", "alamat": "JL. KESATRIAN NO 62 PURWOKERTO", "kode_pos": "53114" },
                  { "nama": "SMKS MARDIKENYA PURWOKERTO", "jenjang": "SMK", "alamat": "JL. MARDIKENYA 4-6 PURWOKERTO", "kode_pos": "53114" },
                  { "nama": "SMKS SWAGAYA 1 PURWOKERTO", "jenjang": "SMK", "alamat": "JL. OVERSTE ISDIMAN 54/IX PURWOKERTO", "kode_pos": "53114" },
                  { "nama": "SMKS WIDYA KARYA PURWOKERTO", "jenjang": "SMK", "alamat": "JLN. MARTADIREJA II MERSI PURWOKERTO JAWA TENGAH", "kode_pos": "53114" }
                ],
                "PURWOKERTO UTARA": [
                  { "nama": "SMP Negeri 9 Purwokerto", "jenjang": "SMP", "alamat": "Jl. Jatisari No. 25" },
                  { "nama": "SMP MUHAMMADIYAH 3 PURWOKERTO", "jenjang": "SMP", "alamat": "Jl. Dr. Angka No. 79 Purwokerto" },
                  { "nama": "MTsS AL - HIDAYAH PURWOKERTO", "jenjang": "SMP", "alamat": "Jl.Let.Jend.Pol.Soemarto VI/63 Karangsuci" },
                  { "nama": "SMAS DIPONEGORO 1 PWT", "jenjang": "SMA", "alamat": "JL. LETJEND POL SUMARTO GG.VI NO. 63", "kode_pos": "53124" },
                  { "nama": "SMK AL KAUTSAR PURWOKERTO", "jenjang": "SMK", "alamat": "Jl. Letjend. Pol. Soemarto RT 01 RW 04", "kode_pos": "53124" },
                  { "nama": "SMKS TEKNIK INFORMASI BINA CITRA INFORMATIKA PURWOKERTO", "jenjang": "SMK", "alamat": "JL. JATISARI 24 F SUMAMPIR", "kode_pos": "53124" }
                ]
              }
            }
            """

            val mapKecamatanToId = mapOf(
                "LUMBIR" to "330201", "WANGON" to "330202", "JATILAWANG" to "330203",
                "RAWALO" to "330204", "KEBASEN" to "330205", "KEMRANJEN" to "330206",
                "SUMPIUH" to "330207", "TAMBAK" to "330208", "SOMAGEDE" to "330209",
                "KALIBAGOR" to "330210", "BANYUMAS" to "330211", "PATIKRAJA" to "330212",
                "PURWOJATI" to "330213", "AJIBARANG" to "330214", "GUMELAR" to "330215",
                "PEKUNCEN" to "330216", "CILONGOK" to "330217", "KARANGLEWAS" to "330218",
                "KEDUNG BANTENG" to "330219", "BATURADEN" to "330220", "SUMBANG" to "330221",
                "KEMBARAN" to "330222", "SOKARAJA" to "330223", "PURWOKERTO SELATAN" to "330224",
                "PURWOKERTO BARAT" to "330225", "PURWOKERTO TIMUR" to "330226", "PURWOKERTO UTARA" to "330227"
            )

            try {
                val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference.child("sekolah")
                val jsonObject = org.json.JSONObject(jsonData)
                val sekolahObj = jsonObject.getJSONObject("sekolah")
                val iteratorKecamatan = sekolahObj.keys()

                while (iteratorKecamatan.hasNext()) {
                    val namaKecamatan = iteratorKecamatan.next()
                    val schoolsArray = sekolahObj.getJSONArray(namaKecamatan)
                    val idKecamatan = mapKecamatanToId[namaKecamatan] ?: namaKecamatan
                    val listNamaSekolah = mutableListOf<String>()

                    for (i in 0 until schoolsArray.length()) {
                        val school = schoolsArray.getJSONObject(i)
                        listNamaSekolah.add(school.getString("nama"))
                    }

                    db.child(idKecamatan).setValue(listNamaSekolah)
                        .addOnSuccessListener {
                            Log.d("Seeder", "Berhasil upload ID: $idKecamatan")
                        }
                }

                // 🔥 UBAH JUGA DI SINI JADI _v2
                sharedPref.edit().putBoolean("isDataSekolahSeeded_v2", true).apply()
                Toast.makeText(this, "Data Sekolah Berhasil Masuk Firebase!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Log.e("Seeder", "Error parsing JSON", e)
            }
        } else {
            Log.d("Seeder", "Data sekolah sudah pernah di-upload, melewati proses seeding.")
        }
    }
    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome) + " " + model.displayName
        Toast.makeText(applicationContext, welcome, Toast.LENGTH_LONG).show()

        // Cek role user di database
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(uid)
            .child("role")
            .get()
            .addOnSuccessListener { snapshot ->
                val role = snapshot.getValue(String::class.java)
                if (role == "admin") {
                    // Arahkan ke Admin Dashboard
                    startActivity(Intent(this, AdminActivity::class.java))
                } else {
                    // Arahkan ke halaman user biasa
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                // Kalau gagal ambil role, arahkan ke MainActivity saja
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_LONG).show()
    }
}
