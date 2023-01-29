package com.jasmeet.cryptoapp.fragment.models


data class UserInfo (
    val userName : String?= null,
    val userEmail : String? =null,
)
{
    constructor() : this("","")
}