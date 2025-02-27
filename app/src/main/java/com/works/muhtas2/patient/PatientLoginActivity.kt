package com.works.muhtas2.patient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.works.muhtas2.R

class PatientLoginActivity : AppCompatActivity() {
    lateinit var btnRegister: Button
    lateinit var btnLogin: Button
    lateinit var editTxtLEmail: EditText
    lateinit var editTxtLPassword: EditText
    lateinit var user: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_login)

        btnLogin = findViewById(R.id.btnDoctorLogin)
        btnRegister = findViewById(R.id.btnDoctorRegister)
        editTxtLEmail = findViewById(R.id.editTxtDoctorLEmail)
        editTxtLPassword = findViewById(R.id.editTxtDoctorLPassword)

        user = FirebaseAuth.getInstance()

        // Establecer filtro para el campo de contraseña
        val hexPattern = "[0-9A-Za-z!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+"
        editTxtLPassword.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source.matches(Regex(hexPattern))) {
                source
            } else {
                ""
            }
        })

        btnRegister.setOnClickListener {
            val intent = Intent(this, PatientRegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            if (editTxtLEmail.text.toString().isEmpty() || editTxtLPassword.text.toString().isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor, complete toda la información",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val loginEmail = editTxtLEmail.text.toString()
                val loginPassword = editTxtLPassword.text.toString()
                if (loginPassword.length < 6) {
                    Toast.makeText(
                        this,
                        "La contraseña debe tener al menos 6 caracteres",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    user.signInWithEmailAndPassword(loginEmail, loginPassword)
                        .addOnCompleteListener(PatientLoginActivity()) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Usuario ha iniciado sesión con éxito",
                                    Toast.LENGTH_LONG
                                ).show()
                                val intent = Intent(this, PatientHomePageActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
        }
    }
}