package com.works.muhtas2.doctor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.works.muhtas2.R
import com.works.muhtas2.doctor.models.DoctorData

class DoctorRegisterActivity : AppCompatActivity() {
    lateinit var spinnerSpecialties: Spinner
    lateinit var txtRDoctorName: EditText
    lateinit var txtRDoctorSurname: EditText
    lateinit var txtRDoctorAge: EditText
    lateinit var txtRDoctorEmail: EditText
    lateinit var txtRDoctorPassword: EditText
    lateinit var txtRDoctorLicense: EditText
    lateinit var txtRDoctorID: EditText
    lateinit var btnRDocConfirm: Button
    lateinit var btnGoogleSignIn: Button

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    lateinit var DoctorName: String
    lateinit var DoctorSurname: String
    lateinit var DoctorAge: String
    lateinit var DoctorField: String
    lateinit var DoctorEmail: String
    lateinit var DoctorPassword: String
    lateinit var DoctorLicense: String
    lateinit var DoctorID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_register)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        txtRDoctorName = findViewById(R.id.txtRDoctorName)
        txtRDoctorSurname = findViewById(R.id.txtRDoctorSurname)
        txtRDoctorAge = findViewById(R.id.txtRDoctorAge)
        txtRDoctorEmail = findViewById(R.id.txtRDoctorEmail)
        txtRDoctorPassword = findViewById(R.id.txtRDoctorPassword)
        txtRDoctorLicense = findViewById(R.id.txtRDoctorLicense)
        txtRDoctorID = findViewById(R.id.txtRDoctorID)
        btnRDocConfirm = findViewById(R.id.btnRDocConfirm)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        spinnerSpecialties = findViewById(R.id.spinnerField)

        val specialties = resources.getStringArray(R.array.especialidades)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSpecialties.adapter = adapter

        txtRDoctorLicense.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.toString().startsWith("MN-")) {
                    txtRDoctorLicense.setText("MN-")
                    txtRDoctorLicense.setSelection(txtRDoctorLicense.text.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnRDocConfirm.setOnClickListener {
            DoctorName = txtRDoctorName.text.toString()
            DoctorSurname = txtRDoctorSurname.text.toString()
            DoctorAge = txtRDoctorAge.text.toString()
            DoctorField = spinnerSpecialties.selectedItem.toString()
            DoctorEmail = txtRDoctorEmail.text.toString()
            DoctorPassword = txtRDoctorPassword.text.toString()
            DoctorLicense = txtRDoctorLicense.text.toString()
            DoctorID = txtRDoctorID.text.toString()

            if (DoctorName.isNotEmpty() && DoctorSurname.isNotEmpty() && DoctorAge.isNotEmpty() && DoctorField.isNotEmpty() && DoctorEmail.isNotEmpty() && DoctorPassword.isNotEmpty() && DoctorLicense.isNotEmpty() && DoctorID.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(DoctorEmail, DoctorPassword)
                    .addOnCompleteListener(DoctorRegisterActivity()) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "User added successfully", Toast.LENGTH_LONG).show()
                            val user = auth.currentUser
                            val doctorData = DoctorData(
                                user!!.uid,
                                DoctorName,
                                DoctorSurname,
                                DoctorAge,
                                DoctorField,
                                DoctorEmail,
                                DoctorPassword,
                                DoctorLicense,
                                DoctorID
                            )
                            db.collection("doctors").document(user.uid).set(doctorData)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Doctor DocumentSnapshot successfully written!")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error writing document", e)
                                }
                            Log.d("doctor", doctorData.toString())
                            val intent = Intent(this@DoctorRegisterActivity, DoctorLoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "No introduzca información incompleta", Toast.LENGTH_LONG).show()
            }
        }

        btnGoogleSignIn.setOnClickListener {
            // Implementar lógica de registro con Google
        }
    }
}