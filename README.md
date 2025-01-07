# MedMobile

## Table of Contents
1. [Overview](#overview)
2. [Technologies](#technologies)
3. [Features](#features)
4. [User Profiles](#user-profiles)
5. [Appointments](#appointments)
6. [Daily Health News](#daily-health-news)
7. [Permissions](#permissions)
8. [Dependencies](#dependencies)
9. [Demo](#demo)

## Overview

MedMobile is an Android application aimed at improving the interaction between doctors and patients. Through this app, patients can manage their medical appointments and doctors can keep track of their schedules. The application integrates Firebase for authentication, database storage, and media hosting.


## Technologies

- **Firebase Auth**
- **Firebase Firestore**
- **Firebase Storage**
- **Glide**
- **Jsoup**

## Features

### User Authentication

- Utilizes Firebase Authentication for user sign-up and login.


##  User Profiles

- There are two types of user profiles: Patient Profile and Doctor Profile.
- Allows users to create and update their profiles including:
    - First Name
    - Last Name
    - Age
    - Password
    - Profile Picture (for this, the app requests permission to access the phone's gallery)
    
- Profile pictures are stored in Firebase Storage, and their URLs are saved in the Firestore database.
- Uses Glide to display profile images efficiently.

### Patient Profile
Data Model:
```kotlin
data class PatientData(
    var UID: String? = null,
    var first: String? = null,
    var last: String? = null,
    var age: String? = null,
    var email: String? = null,
    var password: String? = null,
    var image: String? = null,
)
```
- **UID**: Unique Identifier for the doctor.
- **first**: First name of the doctor.
- **last**: Last name of the doctor.
- **age**: Age of the doctor.
- **field**: Medical field or specialization of the doctor.
- **email**: Email address of the doctor.
- **password**: Password for the doctor's account.
- **image**: URL of the doctor's profile image stored in Firebase Storage.

### Doctor Profile
Data Model:
```kotlin
data class DoctorData(
    var UID: String? = null,
    var first: String? = null,
    var last: String? = null,
    var age: String? = null,
    var field: String? = null,
    var email: String? = null,
    var password: String? = null,
    var image: String? = null,
)
```

- **UID**: Unique Identifier for the doctor.
- **first**: First name of the doctor.
- **last**: Last name of the doctor.
- **age**: Age of the doctor.
- **field**: Medical field or specialization of the doctor.
- **email**: Email address of the doctor.
- **password**: Password for the doctor's account.
- **image**: URL of the doctor's profile image stored in Firebase Storage.

## Appointments

### Appointments

- Users can schedule appointments using customized Date and Time pickers:
    - No appointment can be made for a date earlier than today.
    - Appointments can be scheduled for a maximum of 20 days ahead.
    - Time slots are available between 9:00 AM to 5:00 PM in 15-minute intervals.
  
- Appointments are stored in two different collections in Firestore for easy retrieval:
    - Under the patient's email for all their appointments.
    - Under the doctor's email for all the doctor’s appointments.
    
- Users can view their appointments within the app.
- Users can long-press an appointment to cancel it. When an appointment is canceled by the doctor, it is also removed from the patient's schedule. Same for the patient aswell. When patient canceled an appointment, it is also removed from the doctor's schedule. 



Appointment Data Model:
```kotlin
data class AppointmentData(
    val id : String? = null,
    val doctorEmail : String? = null,
    val patientEmail : String? = null,
    val patientName : String? = null,
    val patientImg : String? = null,
    val doctorName : String? = null,
    val doctorImg : String? = null,
    val note : String? = null,
    val date : String? = null,
    val hour : String? = null
)
```
- **id**: Unique identifier for the appointment.
- **doctorEmail**: Email of the doctor involved in the appointment.
- **patientEmail**: Email of the patient involved in the appointment.
- **patientName**: Name of the patient.
- **patientImg**: URL of the patient's profile image.
- **doctorName**: Name of the doctor.
- **doctorImg**: URL of the doctor's profile image.
- **note**: Additional notes or information for the appointment.
- **date**: Date of the appointment.
- **hour**: Time of the appointment.

Appointments are stored in Firestore under two different collections for easy retrieval:
- Under the patient's email (`patientEmail`) for all their appointments.
- Under the doctor's email (`doctorEmail`) for all the doctor’s appointments.

## Daily Health News
- Retrieves daily health news from [haberler.com/saglik](https://www.haberler.com/saglik/) by parsing HTML using Jsoup.
- News is displayed in a list and updated daily.
- Clicking on a news item opens the full article in a WebView.

## Permissions

The application requires the following permissions:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
## Dependencies
Make sure to include these dependencies in your build.gradle file before building the project.
```groovy
dependencies {

    implementation 'androidx.core:core-ktx:1.7.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.8.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.gridlayout:gridlayout:1.0.0'
    implementation 'com.google.firebase:firebase-auth:22.0.0'
    implementation 'com.google.firebase:firebase-firestore-ktx:24.6.1'
    implementation 'com.google.firebase:firebase-auth-ktx:22.0.0'
    implementation 'com.google.firebase:firebase-storage-ktx:20.2.0'
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'

    implementation 'org.jsoup:jsoup:1.16.1'
    implementation 'com.github.bumptech.glide:glide:4.15.1'
}
```
Also, include this in your project-level build.gradle file.
```groovy
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.3.15'
    }
}

plugins {
    id 'com.android.application' version '7.4.0' apply false
    id 'com.android.library' version '7.4.0' apply false
    id 'org.jetbrains.kotlin.android' version '1.7.21' apply false
}
```

## Demo

<h2 align="center">MainActivity</h2>

<p align="center">
    In this screen, users can choose to log in to the application as Doctor or Patient.
</p>

<p align="center">
    <img width="180" alt="mainactivity" src="https://github.com/akinemreyazici/MedMobile/assets/116732291/0250a54a-bcb5-4691-a26f-4d7664664e08">
</p>


<h2 align="center">Pictures</h2>

<p align="center">
    <img width="200" alt="doctorRegister" src="https://github.com/akinemreyazici/MedMobile/assets/116732291/39e2dbcb-e9ea-4fb7-b7f2-0b08787320b8">
    <img width="200" alt="doctorProfileEdit" src="https://github.com/akinemreyazici/MedMobile/assets/116732291/f0e4c4a7-75d8-4fa3-93f4-d02695791801">
    <img width="200" alt="doctorProfile" src="https://github.com/akinemreyazici/MedMobile/assets/116732291/527d1109-32cf-4be7-b32d-00ede18b8e99">
    <img width="200" alt="patientHomepage" src="https://github.com/akinemreyazici/MedMobile/assets/116732291/ccac0ad5-3359-4f11-abc8-14c0b5b031fb">
</p>

<h2 align="center">Project Realization</h2>


<p align="center">
    <img width="200" alt="login" src="https://github.com/akinemreyazici/MedMobile/assets/116732291/d2fe88d9-8636-4cbc-bb41-1a492b2eea13">
    <img width="200" alt="appDeletegif" src="https://github.com/akinemreyazici/MedMobile/assets/116732291/18bf0d72-0c86-4eaa-b5b8-69665c668c23">
    <img width="200" alt="newsShow" src="https://github.com/akinemreyazici/MedMobile/assets/116732291/d82204da-62cb-4b09-9365-8fbb2afb1d9e"> 
</p>

<p align="center">
    <img width="200" alt="editphoto" src="https://github.com/akinemreyazici/MedMobile/assets/116732291/ac1207a3-d9f9-4c42-8f9d-9c09d874d25d">
    <img width="200" alt="makeApp" src="https://github.com/akinemreyazici/MedMobile/assets/116732291/e775bf37-e3d3-4ede-8e3c-d27fe9dd0bd6">
    <img width="200" alt="deleteApp" src="https://github.com/akinemreyazici/MedMobile/assets/116732291/d1821e9a-05cd-4822-8dae-b1b359b0f338">   
</p>




