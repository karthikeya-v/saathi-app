package com.her.app.data

data class UserProfile(
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",        // "Man", "Woman", "Other"
    val interestedIn: String = "",  // "Men", "Women", "Everyone"
    val mbtiType: String = ""       // e.g. "INFJ-T", "ENTP-A"
)
