package com.works.muhtas2.patient.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.works.muhtas2.R
import com.works.muhtas2.doctor.models.DoctorData
import java.text.SimpleDateFormat
import java.util.*

class DoctorCustomAdapter(
    private val context: Activity,
    private val list: List<DoctorData>
) : ArrayAdapter<DoctorData>(context, R.layout.custom_doctor_list, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rootView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.custom_doctor_list, parent, false)

        val rName = rootView.findViewById<TextView>(R.id.r_name)
        val rAge = rootView.findViewById<TextView>(R.id.r_age)
        val rField = rootView.findViewById<TextView>(R.id.r_field)

        val user = list[position]

        // Concatenamos nombre y apellido de manera segura
        rName.text = "${user.firstName ?: ""} ${user.lastName ?: ""}".trim()

        // Guardamos la fecha de nacimiento en una variable local inmutable
        val dob = user.dob

        // Calculamos la edad si la fecha de nacimiento está disponible
        rAge.text = if (!dob.isNullOrEmpty()) {
            "Edad: ${calculateAge(dob)} años"
        } else {
            "Edad: N/A"
        }

        // Mostramos la especialidad o "Sin especialidad" si es null
        rField.text = user.speciality ?: "Sin especialidad"

        return rootView
    }

    // Función segura para calcular la edad
    private fun calculateAge(dob: String): Int {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val birthDate = sdf.parse(dob) ?: return -1
            val today = Calendar.getInstance()
            val birthDay = Calendar.getInstance().apply { time = birthDate }

            var age = today.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birthDay.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (e: Exception) {
            -1 // En caso de error, devolver un valor inválido
        }
    }
}
