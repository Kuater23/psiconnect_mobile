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
import java.text.SimpleDateFormat
import java.util.*

class DoctorRegisterActivity : AppCompatActivity() {
    lateinit var spinnerSpecialties: Spinner
    lateinit var txtRDoctorName: EditText
    lateinit var txtRDoctorSurname: EditText
    lateinit var txtRDoctorBirthdate: EditText
    lateinit var txtRDoctorEmail: EditText
    lateinit var txtRDoctorPassword: EditText
    lateinit var txtRDoctorLicense: EditText
    lateinit var txtRDoctorID: EditText
    lateinit var txtRDoctorPhone: EditText
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
    lateinit var DoctorPhone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_register)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        txtRDoctorName = findViewById(R.id.txtRDoctorName)
        txtRDoctorSurname = findViewById(R.id.txtRDoctorSurname)
        txtRDoctorBirthdate = findViewById(R.id.txtRDoctorBirthdate)
        txtRDoctorEmail = findViewById(R.id.txtRDoctorEmail)
        txtRDoctorPassword = findViewById(R.id.txtRDoctorPassword)
        txtRDoctorLicense = findViewById(R.id.txtRDoctorLicense)
        txtRDoctorID = findViewById(R.id.txtRDoctorID)
        txtRDoctorPhone = findViewById(R.id.txtRDoctorPhone)
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

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().replace("MN-", "")
                if (input.length > 7) {
                    txtRDoctorLicense.setText("MN-" + input.substring(0, 7))
                    txtRDoctorLicense.setSelection(txtRDoctorLicense.text.length)
                }
            }
        })

        txtRDoctorBirthdate.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val dateFormat = "##/##/####"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) {
                    isUpdating = false
                    return
                }

                var input = s.toString().replace("[^\\d]".toRegex(), "")
                val length = input.length

                if (length > 2) {
                    input = input.substring(0, 2) + "/" + input.substring(2)
                }
                if (length > 4) {
                    input = input.substring(0, 5) + "/" + input.substring(5)
                }
                if (length > 8) {
                    input = input.substring(0, 10)
                }

                isUpdating = true
                txtRDoctorBirthdate.setText(input)
                txtRDoctorBirthdate.setSelection(input.length)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnRDocConfirm.setOnClickListener {
            DoctorName = txtRDoctorName.text.toString()
            DoctorSurname = txtRDoctorSurname.text.toString()
            val birthdateStr = txtRDoctorBirthdate.text.toString()
            DoctorAge = calculateAge(birthdateStr).toString()
            DoctorField = spinnerSpecialties.selectedItem.toString()
            DoctorEmail = txtRDoctorEmail.text.toString()
            DoctorPassword = txtRDoctorPassword.text.toString()
            DoctorLicense = txtRDoctorLicense.text.toString()
            DoctorID = txtRDoctorID.text.toString()
            DoctorPhone = txtRDoctorPhone.text.toString()

            if (DoctorName.isNotEmpty() && DoctorSurname.isNotEmpty() && birthdateStr.isNotEmpty() && DoctorField.isNotEmpty() && DoctorEmail.isNotEmpty() && DoctorPassword.isNotEmpty() && DoctorLicense.isNotEmpty() && DoctorID.isNotEmpty() && DoctorPhone.isNotEmpty()) {
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
                                DoctorID,
                                DoctorPhone
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