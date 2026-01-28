package com.sbuddy.app.data.model

import androidx.annotation.Keep

@Keep
data class User(
    val uid: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val mobile: String? = null
)
