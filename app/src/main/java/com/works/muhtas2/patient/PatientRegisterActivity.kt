package com.works.muhtas2.patient

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.works.muhtas2.R
import com.works.muhtas2.patient.models.PatientData
import java.text.SimpleDateFormat
import java.util.*

class PatientRegisterActivity : AppCompatActivity() {
    private lateinit var txtRPatientName: EditText
    private lateinit var txtRPatientSurname: EditText
    private lateinit var txtRPatientBirthdate: EditText
    private lateinit var txtRPatientEmail: EditText
    private lateinit var txtRPatientPassword: EditText
    private lateinit var btnRPatientConfirm: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_register)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        txtRPatientName = findViewById(R.id.txtRPatientName)
        txtRPatientSurname = findViewById(R.id.txtRPatientSurname)
        txtRPatientBirthdate = findViewById(R.id.txtRPatientDob)
        txtRPatientEmail = findViewById(R.id.txtRPatientEmail)
        txtRPatientPassword = findViewById(R.id.txtRPatientPassword)
        btnRPatientConfirm = findViewById(R.id.btnRPatientConfirm)

        txtRPatientBirthdate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var text = s.toString()
                if (text.length == 2 || text.length == 5) {
                    text += "/"
                    txtRPatientBirthdate.setText(text)
                    txtRPatientBirthdate.setSelection(text.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnRPatientConfirm.setOnClickListener {
            registerPatient()
        }
    }

    private fun registerPatient() {
        val patientName = txtRPatientName.text.toString().trim()
        val patientSurname = txtRPatientSurname.text.toString().trim()
        val birthdateStr = txtRPatientBirthdate.text.toString().trim()
        val patientEmail = txtRPatientEmail.text.toString().trim()
        val patientPassword = txtRPatientPassword.text.toString().trim()

        if (patientName.isEmpty() || patientSurname.isEmpty() || birthdateStr.isEmpty() ||
            patientEmail.isEmpty() || patientPassword.isEmpty()
        ) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidDate(birthdateStr)) {
            Toast.makeText(this, "Fecha de nacimiento inválida. Use el formato DD/MM/YYYY", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(patientEmail).matches()) {
            Toast.makeText(this, "Correo electrónico inválido", Toast.LENGTH_SHORT).show()
            return
        }

        if (patientPassword.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(patientEmail, patientPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val patientData = PatientData(
                        uid = user!!.uid,
                        firstName = patientName,
                        lastName = patientSurname,
                        dob = birthdateStr,
                        email = patientEmail,
                        password = patientPassword,
                        phoneN = null, // Se puede completar más tarde
                        dni = null
                    )
                    db.collection("patients").document(user.uid).set(patientData)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Paciente registrado correctamente")
                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, PatientLoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error al registrar paciente", e)
                            Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, task.exception?.localizedMessage ?: "Error desconocido", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun isValidDate(date: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            sdf.isLenient = false
            sdf.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }
}
