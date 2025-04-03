package com.works.muhtas2.doctor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.works.muhtas2.R

class DoctorLoginActivity : AppCompatActivity() {
    private lateinit var btnDoctorLogin: Button
    private lateinit var btnDoctorRegister: Button
    private lateinit var editTxtDoctorEmail: EditText
    private lateinit var editTxtDoctorPassword: EditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_login)

        btnDoctorLogin = findViewById(R.id.btnDoctorLogin)
        btnDoctorRegister = findViewById(R.id.btnDoctorRegister)
        editTxtDoctorEmail = findViewById(R.id.editTxtDoctorLEmail)
        editTxtDoctorPassword = findViewById(R.id.editTxtDoctorLPassword)

        auth = FirebaseAuth.getInstance()

        btnDoctorLogin.setOnClickListener {
            loginDoctor()
        }

        btnDoctorRegister.setOnClickListener {
            val intent = Intent(this, DoctorRegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginDoctor() {
        val email = editTxtDoctorEmail.text.toString().trim()
        val password = editTxtDoctorPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Por favor, complete todos los campos.")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showToast("Inicio de sesión exitoso.")
                    navigateToHome()
                } else {
                    showToast(task.exception?.message ?: "Error al iniciar sesión.")
                }
            }
    }

    private fun navigateToHome() {
        val intent = Intent(this, DoctorHomepageActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
