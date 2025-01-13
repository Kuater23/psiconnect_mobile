package com.works.muhtas2.doctor

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.works.muhtas2.R
import com.works.muhtas2.doctor.models.DoctorData

class DoctorProfileEditActivity : AppCompatActivity() {
    lateinit var edtDName: EditText
    lateinit var edtDSurname: EditText
    lateinit var edtDAge: EditText
    lateinit var edtOldPassword: EditText
    lateinit var edtNewPassword: EditText
    lateinit var spinnerField: Spinner
    lateinit var btnSaveChanges: Button
    lateinit var imgDoctorProfile: ImageView

    var downloadUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_profile_edit)

        edtDName = findViewById(R.id.editDName)
        edtDSurname = findViewById(R.id.editDSurname)
        edtDAge = findViewById(R.id.editDAge)
        edtOldPassword = findViewById(R.id.editOldPassword)
        edtNewPassword = findViewById(R.id.editNewPassword)
        spinnerField = findViewById(R.id.spinnerField)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        imgDoctorProfile = findViewById(R.id.imgDoctorProfilePicture)

        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        // Extraer datos de Firestore y asignarlos a EditTexts
        db.collection("doctors")
            .document(user?.email!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val doctorData = document.toObject(DoctorData::class.java)
                    // Asignar datos a EditTexts
                    edtDName.setText(doctorData?.first)
                    edtDSurname.setText(doctorData?.last)
                    edtDAge.setText(doctorData?.age)
                    // Configurar el spinner para la especialidad
                    val specialties = resources.getStringArray(R.array.doctor_specialties)
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialties)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerField.adapter = adapter
                    val selectedIndex = specialties.indexOf(doctorData?.field)
                    spinnerField.setSelection(selectedIndex)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting doctor data: ${e.message}", e)
            }

        btnSaveChanges.setOnClickListener {
            if (edtDName.text.isNotEmpty() &&
                edtDSurname.text.isNotEmpty() &&
                edtDAge.text.isNotEmpty()
            ) {
                // Vamos a obtener el consentimiento del usuario con AlertDialog
                AlertDialog.Builder(this).apply {
                    setTitle("Guardar cambios")
                    setMessage("¿Quieres actualizar?")
                    setPositiveButton("Yes") { _, _ ->
                        val name = edtDName.text.toString()
                        val surname = edtDSurname.text.toString()
                        val age = edtDAge.text.toString()
                        val field = spinnerField.selectedItem.toString()
                        val newPassword = edtNewPassword.text.toString()

                        // Actualizar la información en Firestore
                        updateDoctorInFirestore(
                            user?.uid!!,
                            name,
                            surname,
                            age,
                            field,
                            user.email!!,
                            newPassword,
                            downloadUri?.toString()
                        )
                        // Inicie el intent con un tiempo de retraso específico
                        Handler().postDelayed({
                            val intent = Intent(
                                this@DoctorProfileEditActivity,
                                DoctorProfileActivity::class.java
                            )
                            startActivity(intent)
                            finish()
                        }, 2000) // 2 segundos de retraso
                    }
                    setNegativeButton("No", null)
                }.create().show()
            } else {
                Toast.makeText(this, "Por favor, rellene la información por completo", Toast.LENGTH_LONG)
                    .show()
            }
        }

        // Solicitar acceso a la galería cuando se hace clic en la imagen
        imgDoctorProfile.setOnClickListener {
            openGallery()
        }
    }

    // Código de permiso para solicitar al usuario el acceso a la galería
    private val READ_EXTERNAL_STORAGE_PERMISSION = 123
    private val PICK_IMAGE_REQUEST = 123
    // Compruebe el método onRequestPermissionsResult para ver los resultados de los permisos solicitados
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, se puede proporcionar acceso a la galería
                openGallery()
            } else {
                // Permiso denegado, no se puede acceder a la galería
                Toast.makeText(this, "Galeriye erişim izni reddedildi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Se invoca cuando se concede acceso a la galería
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Compruebe el método onActivityResult para ver el resultado de la selección de la galería
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data

            // Carga de la imagen seleccionada en ImageView con Glide
            Glide.with(this).load(selectedImageUri).into(imgDoctorProfile)

            // Guarde la imagen seleccionada en Firebase Storage
            val user = FirebaseAuth.getInstance().currentUser
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("users/${user?.email}/profile.jpg")

            val uploadTask = imageRef.putFile(selectedImageUri!!)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                // Obtener la URL descargable de una imagen
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    downloadUri = task.result
                    // Qué hacer cuando obtienes la URL descargable de la imagen
                    // Por ejemplo, guardar en Firestore
                    // Puedes obtener la URL usando downloadUri.toString()
                } else {
                    // Manejo de errores cuando la imagen no se carga
                }
            }
        }
    }

    private fun updateDoctorInFirestore(
        userId: String,
        first: String,
        last: String,
        age: String,
        field: String,
        email: String,
        newPassword: String,
        image: String?
    ) {
        val db = FirebaseFirestore.getInstance()

        // Obtener la contraseña actual del usuario
        db.collection("doctors").document(email).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val currentPassword = document.getString("password") ?: ""

                // Usar la nueva contraseña solo si se ha proporcionado
                val passwordToUpdate = if (newPassword.isNotEmpty()) newPassword else currentPassword

                val doctorDataInfo = DoctorData(
                    UID = userId,
                    first = first,
                    last = last,
                    age = age,
                    field = field,
                    email = email,
                    password = passwordToUpdate,
                    image = image
                )

                db.collection("doctors")
                    .document(email)
                    .set(doctorDataInfo)
                    .addOnSuccessListener {
                        Toast.makeText(this, "La información se ha actualizado con éxito", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al actualizar la información: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }
}