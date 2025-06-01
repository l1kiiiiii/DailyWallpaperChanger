package com.example.daily.worker

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daily.api.RetrofitInstance
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WallpaperViewModel : ViewModel() {
    var imageUrl = mutableStateOf("")
    var imageTitle = mutableStateOf("")
    var lastFetchDate = mutableStateOf("")

    private val apiKey = "YOUR_API_KEY" // Replace with your NASA API key

    fun fetchImageOfTheDay() {
        val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        if (lastFetchDate.value != currentDate) {
            viewModelScope.launch {
                try {
                    val response = RetrofitInstance.api.getImageOfTheDay(apiKey)
                    imageUrl.value = response.url
                    imageTitle.value = response.title
                    lastFetchDate.value = currentDate
                } catch (e: Exception) {
                    // Handle error (e.g., show toast)
                }
            }
        }
    }
}