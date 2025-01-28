package com.works.muhtas2.patient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.works.muhtas2.R
import com.works.muhtas2.patient.models.PatientData

class PatientRegisterActivity : AppCompatActivity() {
    lateinit var txtClientName: EditText
    lateinit var txtClientSurname: EditText
    lateinit var txtClientAge: EditText
    lateinit var txtClientEmail: EditText
    lateinit var txtClientPassword: EditText
    lateinit var btnConfirm: ImageButton

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    lateinit var ClientName: String
    lateinit var ClientSurname: String
    lateinit var ClientAge: String
    lateinit var ClientEmail: String
    lateinit var ClientPassword: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_register)

        txtClientName = findViewById(R.id.txtClientName)
        txtClientSurname = findViewById(R.id.txtClientSurname)
        txtClientAge = findViewById(R.id.txtClientAge)
        txtClientEmail = findViewById(R.id.txtClientEmail)
        txtClientPassword = findViewById(R.id.txtClientPassword)
        btnConfirm = findViewById(R.id.btnRDocConfirm)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnConfirm.setOnClickListener {
            ClientName = txtClientName.text.toString()
            ClientSurname = txtClientSurname.text.toString()
            ClientAge = txtClientAge.text.toString()
            ClientEmail = txtClientEmail.text.toString()
            ClientPassword = txtClientPassword.text.toString()
            Log.d("name", ClientName)
            Log.d("surname", ClientSurname)
            Log.d("age", ClientAge)
            Log.d("email", ClientEmail)
            Log.d("password", ClientPassword)

            if (ClientName.isNotEmpty() && ClientSurname.isNotEmpty() && ClientAge.isNotEmpty() && ClientEmail.isNotEmpty() && ClientPassword.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(ClientEmail, ClientPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Usuario agregado exitosamente", Toast.LENGTH_LONG).show()
                            val user = auth.currentUser
                            val client = PatientData(
                                user!!.uid,
                                ClientName,
                                ClientSurname,
                                ClientAge,
                                ClientEmail,
                                ClientPassword,
                                ""
                            )
                            db.collection("patients").document(user.email!!).set(client)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "¡Documento del paciente escrito exitosamente!")
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Firestore", "Error al escribir el documento del paciente", e)
                                }
                            Log.d("patient", client.toString())
                            val intent = Intent(this@PatientRegisterActivity, PatientLoginActivity::class.java)
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
    }
}