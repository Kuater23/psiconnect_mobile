package com.works.muhtas2.patient

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.works.muhtas2.R

class PatientLoginActivity : AppCompatActivity() {
    private lateinit var btnRegister: Button
    private lateinit var btnLogin: Button
    private lateinit var editTxtLEmail: EditText
    private lateinit var editTxtLPassword: EditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_login)

        btnLogin = findViewById(R.id.btnPatientLogin)
        btnRegister = findViewById(R.id.btnPatientRegister)
        editTxtLEmail = findViewById(R.id.editTxtPatientLEmail)
        editTxtLPassword = findViewById(R.id.editTxtPatientLPassword)

        auth = FirebaseAuth.getInstance()

        btnRegister.setOnClickListener {
            startActivity(Intent(this, PatientRegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val loginEmail = editTxtLEmail.text.toString().trim()
            val loginPassword = editTxtLPassword.text.toString().trim()

            if (loginEmail.isEmpty() || loginPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, complete toda la información", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (loginPassword.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(loginEmail, loginPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, PatientHomePageActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, task.exception?.localizedMessage ?: "Error desconocido", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
