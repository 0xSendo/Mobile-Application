package com.example.myacademate

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val _profileImageUri = mutableStateOf<Uri?>(null)
    val profileImageUri: Uri? by _profileImageUri

    fun setProfileImageUri(context: Context, username: String, uri: Uri?) {
        viewModelScope.launch {
            _profileImageUri.value = uri
            // Save to SharedPreferences for persistence
            context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("profile_image_uri_$username", uri?.toString())
                .apply()
            Log.d("ProfileViewModel", "Profile image set: $uri")
        }
    }

    fun loadProfileImageUri(context: Context, username: String) {
        viewModelScope.launch {
            val uriString = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                .getString("profile_image_uri_$username", null)
            _profileImageUri.value = uriString?.let { Uri.parse(it) }
            Log.d("ProfileViewModel", "Profile image loaded: ${_profileImageUri.value}")
        }
    }
}
