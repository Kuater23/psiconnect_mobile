package com.works.muhtas2.doctor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.works.muhtas2.R
import com.works.muhtas2.patient.PatientHomePageActivity
import com.works.muhtas2.patient.PatientLoginActivity
import com.works.muhtas2.patient.PatientRegisterActivity

class DoctorLoginActivity : AppCompatActivity() {
    lateinit var btnDoctorLogin: Button
    lateinit var btnDoctorRegister: Button
    lateinit var editTxtDoctorLEmail: EditText
    lateinit var editTxtDoctorLPassword: EditText
    lateinit var user: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_login)

        btnDoctorLogin = findViewById(R.id.btnDoctorLogin)
        btnDoctorRegister = findViewById(R.id.btnDoctorRegister)
        editTxtDoctorLEmail = findViewById(R.id.editTxtDoctorLEmail)
        editTxtDoctorLPassword = findViewById(R.id.editTxtDoctorLPassword)



        user = FirebaseAuth.getInstance()

        btnDoctorLogin.setOnClickListener {
            if (editTxtDoctorLEmail.text.toString() == "" || editTxtDoctorLPassword.text.toString() == "") {
                Toast.makeText(
                    this,
                    "Por favor, rellene la información en su totalidad",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val LoginEmail = editTxtDoctorLEmail.text.toString()
                val LoginPassword = editTxtDoctorLPassword.text.toString()
                user.signInWithEmailAndPassword(LoginEmail, LoginPassword)
                    .addOnCompleteListener(PatientLoginActivity()) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "El usuario ha iniciado sesión con éxito",
                                Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(this, DoctorHomepageActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        btnDoctorRegister.setOnClickListener {
            var intent = Intent(this, DoctorRegisterActivity::class.java)
            startActivity(intent)
        }
    }
}