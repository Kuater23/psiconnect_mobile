package com.works.muhtas2.patient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.works.muhtas2.R
import com.works.muhtas2.patient.models.PatientData
import java.text.SimpleDateFormat
import java.util.*

class PatientRegisterActivity : AppCompatActivity() {
    lateinit var txtRPatientName: EditText
    lateinit var txtRPatientSurname: EditText
    lateinit var txtRPatientBirthdate: EditText
    lateinit var txtRPatientEmail: EditText
    lateinit var txtRPatientPassword: EditText
    lateinit var btnRPatientConfirm: Button

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    lateinit var PatientName: String
    lateinit var PatientSurname: String
    lateinit var PatientAge: String
    lateinit var PatientEmail: String
    lateinit var PatientPassword: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_register)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        txtRPatientName = findViewById(R.id.txtRPatientName)
        txtRPatientSurname = findViewById(R.id.txtRPatientSurname)
        txtRPatientBirthdate = findViewById(R.id.txtRPatientBirthdate)
        txtRPatientEmail = findViewById(R.id.txtRPatientEmail)
        txtRPatientPassword = findViewById(R.id.txtRPatientPassword)
        btnRPatientConfirm = findViewById(R.id.btnRPatientConfirm)

        // Establecer filtro para el campo de contraseña
        val hexPattern = "[0-9A-Fa-f!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+"
        txtRPatientPassword.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source.matches(Regex(hexPattern))) {
                source
            } else {
                ""
            }
        })

        txtRPatientBirthdate.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private val ddmmyyyy = "DDMMYYYY"
            private val cal = Calendar.getInstance()

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString() != current) {
                    var clean = s.toString().replace("[^\\d.]|\\.".toRegex(), "")
                    val cleanC = current.replace("[^\\d.]|\\.".toRegex(), "")

                    val cl = clean.length
                    var sel = cl
                    for (i in 2..cl step 2) {
                        sel++
                    }
                    if (clean == cleanC) sel--

                    if (clean.length < 8) {
                        clean += ddmmyyyy.substring(clean.length)
                    } else {
                        val day = Integer.parseInt(clean.substring(0, 2))
                        val mon = Integer.parseInt(clean.substring(2, 4))
                        val year = Integer.parseInt(clean.substring(4, 8))

                        cal.set(Calendar.DAY_OF_MONTH, day)
                        cal.set(Calendar.MONTH, mon - 1)
                        cal.set(Calendar.YEAR, year)

                        clean = String.format("%02d%02d%02d", day, mon, year)
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                        clean.substring(2, 4),
                        clean.substring(4, 8))

                    sel = if (sel < 0) 0 else sel
                    current = clean
                    txtRPatientBirthdate.setText(current)
                    txtRPatientBirthdate.setSelection(if (sel < current.length) sel else current.length)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        btnRPatientConfirm.setOnClickListener {
            PatientName = txtRPatientName.text.toString()
            PatientSurname = txtRPatientSurname.text.toString()
            val birthdateStr = txtRPatientBirthdate.text.toString()
            PatientAge = calculateAge(birthdateStr).toString()
            PatientEmail = txtRPatientEmail.text.toString()
            PatientPassword = txtRPatientPassword.text.toString()

            if (PatientName.isNotEmpty() && PatientSurname.isNotEmpty() && birthdateStr.isNotEmpty() && PatientEmail.isNotEmpty() && PatientPassword.isNotEmpty() && PatientPassword.length >= 6) {
                auth.createUserWithEmailAndPassword(PatientEmail, PatientPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val patientData = PatientData(
                                user!!.uid,
                                PatientName,
                                PatientSurname,
                                PatientAge,
                                PatientEmail
                            )
                            db.collection("patients").document(user.uid).set(patientData)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Patient DocumentSnapshot successfully written!")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error writing document", e)
                                }
                            val intent = Intent(this@PatientRegisterActivity, PatientLoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "No introduzca información incompleta o contraseña menor a 6 caracteres", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun calculateAge(birthdate: String): Int {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val birthDate = sdf.parse(birthdate)
        val today = Calendar.getInstance()
        val birthDay = Calendar.getInstance()
        birthDay.time = birthDate

        var age = today.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthDay.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }
}