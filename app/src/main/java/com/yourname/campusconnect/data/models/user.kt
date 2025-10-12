package com.yourname.campusconnect.data.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val department: String = "",
    val year: Int = 1,
    val bio: String = "",
    val phone: String = "",
    val profilePhotoUrl: String = "",
    val skillsCanTeach: List<String> = emptyList(),
    val skillsWantToLearn: List<String> = emptyList(),
    val interests: List<String> = emptyList()
)


