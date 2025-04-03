package com.works.muhtas2.doctor

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.works.muhtas2.R
import com.works.muhtas2.doctor.models.DoctorData
import java.text.SimpleDateFormat
import java.util.*

class DoctorRegisterActivity : AppCompatActivity() {

    private lateinit var spinnerSpecialties: Spinner
    private lateinit var txtRDoctorName: EditText
    private lateinit var txtRDoctorSurname: EditText
    private lateinit var txtRDoctorBirthdate: EditText
    private lateinit var txtRDoctorEmail: EditText
    private lateinit var txtRDoctorPassword: EditText
    private lateinit var txtRDoctorLicense: EditText
    private lateinit var txtRDoctorID: EditText
    private lateinit var txtRDoctorPhone: EditText
    private lateinit var btnRDocConfirm: Button
    private lateinit var btnGoogleSignIn: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

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

        setupTextWatchers()

        btnRDocConfirm.setOnClickListener { registerDoctor() }
        btnGoogleSignIn.setOnClickListener {
            // Implementar lógica de registro con Google
        }
    }

    private fun setupTextWatchers() {
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

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) {
                    isUpdating = false
                    return
                }

                var input = s.toString().replace("[^\\d]".toRegex(), "")
                val length = input.length

                if (length > 2) input = input.substring(0, 2) + "/" + input.substring(2)
                if (length > 4) input = input.substring(0, 5) + "/" + input.substring(5)
                if (length > 10) input = input.substring(0, 10)

                isUpdating = true
                txtRDoctorBirthdate.setText(input)
                txtRDoctorBirthdate.setSelection(input.length)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun registerDoctor() {
        val doctorName = txtRDoctorName.text.toString().trim()
        val doctorSurname = txtRDoctorSurname.text.toString().trim()
        val birthdateStr = txtRDoctorBirthdate.text.toString().trim()
        val doctorEmail = txtRDoctorEmail.text.toString().trim()
        val doctorPassword = txtRDoctorPassword.text.toString().trim()
        val doctorLicense = txtRDoctorLicense.text.toString().trim()
        val doctorID = txtRDoctorID.text.toString().trim()
        val doctorPhone = txtRDoctorPhone.text.toString().trim()
        val doctorSpeciality = spinnerSpecialties.selectedItem.toString()

        if (doctorName.isEmpty() || doctorSurname.isEmpty() || birthdateStr.isEmpty() ||
            doctorEmail.isEmpty() || doctorPassword.isEmpty() || doctorLicense.isEmpty() ||
            doctorID.isEmpty() || doctorPhone.isEmpty()
        ) {
            showToast("Por favor, complete toda la información")
            return
        }

        auth.createUserWithEmailAndPassword(doctorEmail, doctorPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val doctorData = DoctorData(
                            dob = birthdateStr,
                            email = doctorEmail,
                            firstName = doctorName,
                            lastName = doctorSurname,
                            password = doctorPassword,
                            phoneN = doctorPhone,
                            dni = doctorID,
                            uid = it.uid,
                            license = doctorLicense,
                            speciality = doctorSpeciality,
                            startTime = null,
                            endTime = null,
                            workDays = null,
                            breakDuration = null
                        )
                        saveDoctorToFirestore(it.uid, doctorData)
                    }
                } else {
                    showToast(task.exception?.message ?: "Error al registrar usuario")
                }
            }
    }

    private fun saveDoctorToFirestore(uid: String, doctorData: DoctorData) {
        db.collection("doctors").document(uid).set(doctorData)
            .addOnSuccessListener {
                Log.d("Firestore", "Doctor registrado con éxito.")
                navigateToLogin()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al guardar el doctor en Firestore", e)
                showToast("Error al guardar los datos")
            }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, DoctorLoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
