package com.works.muhtas2.patient

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.works.muhtas2.R
import com.works.muhtas2.doctor.models.AppointmentData
import java.util.Calendar

class AppointmentActivity : AppCompatActivity() {
    private lateinit var txtAppName: TextView
    private lateinit var txtAppSurname: TextView
    private lateinit var txtAppDob: TextView
    private lateinit var txtAppSpeciality: TextView
    private lateinit var txtAppHour: TextView
    private lateinit var btnSelectHour: ImageButton
    private lateinit var btnSelectDate: ImageButton
    private lateinit var btnMakeApp: Button
    private lateinit var editTxtAppNote: EditText

    private var selectedDate = ""
    private var selectedHour = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment)

        txtAppName = findViewById(R.id.txtAppName)
        txtAppSurname = findViewById(R.id.txtAppSurname)
        txtAppDob = findViewById(R.id.txtAppDob)
        txtAppSpeciality = findViewById(R.id.txtAppSpeciality)
        txtAppHour = findViewById(R.id.txtAppHour)
        btnSelectHour = findViewById(R.id.btnSelectHour)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        btnMakeApp = findViewById(R.id.btnMakeApp)
        editTxtAppNote = findViewById(R.id.editTxtAppNote)

        val doctorFirstName = intent.getStringExtra("firstName")
        val doctorLastName = intent.getStringExtra("lastName")
        val doctorDob = intent.getStringExtra("dob")
        val doctorSpeciality = intent.getStringExtra("speciality")
        val doctorImage = intent.getStringExtra("doctorImage")
        val doctorUid = intent.getStringExtra("doctorUid")

        txtAppName.text = "Nombre: $doctorFirstName"
        txtAppSurname.text = "Apellido: $doctorLastName"
        txtAppDob.text = "Fecha de Nacimiento: $doctorDob"
        txtAppSpeciality.text = "Especialidad: $doctorSpeciality"

        Glide.with(this).load(doctorImage).into(findViewById(R.id.ImgApp))

        setupDatePicker()
        setupTimePicker()

        btnMakeApp.setOnClickListener {
            createAppointment(doctorUid)
        }
    }

    private fun setupDatePicker() {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val selectedDateCalendar = Calendar.getInstance()
                selectedDateCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth)
                val dayOfWeek = selectedDateCalendar.get(Calendar.DAY_OF_WEEK)

                if (dayOfWeek == Calendar.SUNDAY) {
                    Toast.makeText(this, "No hay horario de trabajo los domingos", Toast.LENGTH_LONG).show()
                } else {
                    selectedDate = String.format("%02d/%02d/%d", selectedDayOfMonth, selectedMonth + 1, selectedYear)
                }
            },
            year,
            month,
            dayOfMonth
        )

        val minDate = Calendar.getInstance()
        datePickerDialog.datePicker.minDate = minDate.timeInMillis

        val maxDate = Calendar.getInstance()
        maxDate.add(Calendar.DAY_OF_MONTH, 20)
        datePickerDialog.datePicker.maxDate = maxDate.timeInMillis

        btnSelectDate.setOnClickListener {
            datePickerDialog.show()
        }
    }

    private fun setupTimePicker() {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHourOfDay, selectedMinute ->
                val roundedMinute = (selectedMinute / 15) * 15
                if (selectedHourOfDay < 9 || selectedHourOfDay >= 17) {
                    Toast.makeText(this, "Seleccione una hora entre 9:00 y 17:00", Toast.LENGTH_LONG).show()
                } else {
                    selectedHour = String.format("%02d:%02d", selectedHourOfDay, roundedMinute)
                    txtAppHour.text = "Fecha: $selectedDate\nHora: $selectedHour"
                }
            },
            hour,
            minute,
            true
        )

        btnSelectHour.setOnClickListener {
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Seleccione una fecha primero", Toast.LENGTH_LONG).show()
            } else {
                timePickerDialog.show()
            }
        }
    }

    private fun createAppointment(doctorUid: String?) {
        val patientUid = FirebaseAuth.getInstance().currentUser?.uid
        val appointmentNote = editTxtAppNote.text.toString()

        if (patientUid != null && doctorUid != null && selectedDate.isNotEmpty() && selectedHour.isNotEmpty()) {
            val appointmentInfo = AppointmentData(
                id = null,
                doctorUid = doctorUid,
                patientUid = patientUid,
                note = appointmentNote,
                date = selectedDate,
                hour = selectedHour
            )
            addAppointmentToFirestore(patientUid, doctorUid, appointmentInfo)
            Toast.makeText(this, "Cita creada con éxito", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, PatientHomePageActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Complete toda la información requerida", Toast.LENGTH_LONG).show()
        }
    }

    private fun addAppointmentToFirestore(patientUid: String, doctorUid: String, appointment: AppointmentData) {
        val db = FirebaseFirestore.getInstance()

        val patientRef = db.collection("appointments").document(patientUid)
        val newAppointmentRef = patientRef.collection("patientAppointments").document()
        val doctorRef = db.collection("doctorAppointments").document(doctorUid)
        val newDoctorAppointmentRef = doctorRef.collection("appointments").document(newAppointmentRef.id)

        db.runBatch { batch ->
            batch.set(newAppointmentRef, appointment)
            batch.set(newDoctorAppointmentRef, appointment)
        }.addOnSuccessListener {
            Log.d("AppointmentActivity", "Cita añadida con éxito.")
        }.addOnFailureListener { e ->
            Log.w("AppointmentActivity", "Error al añadir la cita", e)
        }
    }
}
