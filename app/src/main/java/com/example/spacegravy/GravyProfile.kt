package com.example.spacegravy

data class GravyProfile (
    val uid: String,
    val email: String,
    val firstname: String,
    val lastname: String,
    val profileImage: String,
    val bio: String
){
    constructor():this("","","","","","")
}