package com.works.muhtas2.patient

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.works.muhtas2.R
import com.works.muhtas2.patient.models.PatientData

class PatientProfileEditActivity : AppCompatActivity() {
    private lateinit var edtPName: EditText
    private lateinit var edtPSurname: EditText
    private lateinit var edtPDob: EditText
    private lateinit var edtOldPassword: EditText
    private lateinit var edtNewPassword: EditText
    private lateinit var btnSaveChanges: Button
    private lateinit var imgPatientProfile: ImageView

    private var downloadUri: Uri? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile_edit)

        edtPName = findViewById(R.id.editPName)
        edtPSurname = findViewById(R.id.editPSurname)
        edtPDob = findViewById(R.id.editPDob)
        edtOldPassword = findViewById(R.id.editOldPassword)
        edtNewPassword = findViewById(R.id.editNewPassword)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        imgPatientProfile = findViewById(R.id.imgPatientProfilePicture)

        loadUserProfile()

        btnSaveChanges.setOnClickListener {
            saveChanges()
        }

        imgPatientProfile.setOnClickListener {
            openGallery()
        }
    }

    private fun loadUserProfile() {
        val userUid = auth.currentUser?.uid ?: return

        db.collection("patients").document(userUid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val patient = document.toObject(PatientData::class.java)
                    runOnUiThread {
                        edtPName.setText(patient?.firstName ?: "")
                        edtPSurname.setText(patient?.lastName ?: "")
                        edtPDob.setText(patient?.dob ?: "")

                        // Cargar imagen de perfil
                        Glide.with(this)
                            .load(patient?.phoneN)
                            .placeholder(android.R.drawable.ic_menu_camera) // Ícono genérico de Android
                            .error(android.R.drawable.ic_menu_camera) // Se muestra si no hay imagen
                            .into(imgPatientProfile)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener datos del paciente: ${e.message}", e)
            }
    }

    private fun saveChanges() {
        val userUid = auth.currentUser?.uid ?: return
        val name = edtPName.text.toString().trim()
        val surname = edtPSurname.text.toString().trim()
        val dob = edtPDob.text.toString().trim()
        val newPassword = edtNewPassword.text.toString().trim()

        if (name.isEmpty() || surname.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this).apply {
            setTitle("Guardar cambios")
            setMessage("¿Quieres actualizar tu perfil?")
            setPositiveButton("Sí") { _, _ ->
                updateClientInFirestore(userUid, name, surname, dob, newPassword, downloadUri?.toString())
            }
            setNegativeButton("No", null)
        }.create().show()
    }

    private fun updateClientInFirestore(
        userId: String,
        firstName: String,
        lastName: String,
        dob: String,
        newPassword: String,
        phoneN: String?
    ) {
        val updateData = mutableMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName,
            "dob" to dob
        )

        phoneN?.let { updateData["phoneN"] = it }

        db.collection("patients").document(userId)
            .set(updateData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Firestore", "Perfil actualizado con éxito")
                if (newPassword.isNotEmpty()) {
                    updatePassword(newPassword)
                } else {
                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al actualizar perfil: ${e.message}", e)
                Toast.makeText(this, "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePassword(newPassword: String) {
        val user = auth.currentUser ?: return

        val credential = EmailAuthProvider.getCredential(user.email!!, edtOldPassword.text.toString())
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirebaseAuth", "Error al actualizar contraseña: ${e.message}", e)
                        Toast.makeText(this, "Error al actualizar contraseña", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data ?: return

            Glide.with(this).load(selectedImageUri).into(imgPatientProfile)
            uploadImageToFirebase(selectedImageUri)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val userUid = auth.currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("users/$userUid/profile.jpg")

        storageRef.putFile(imageUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception!!
                storageRef.downloadUrl
            }
            .addOnSuccessListener { uri ->
                downloadUri = uri
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 123
    }
}
