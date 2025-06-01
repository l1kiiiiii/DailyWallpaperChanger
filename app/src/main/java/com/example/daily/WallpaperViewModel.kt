package com.example.daily

import android.app.WallpaperManager
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.daily.api.RetrofitInstance

class WallpaperWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val apiKey = BuildConfig.API_KEY // Replace with your NASA API key
        return try {
            val response = RetrofitInstance.api.getImageOfTheDay(apiKey)

            val request = ImageRequest.Builder(applicationContext)
                .data(response.url)
                .build()

            val result = applicationContext.imageLoader.execute(request)
            if (result is SuccessResult) {
                val drawable = result.drawable
                val bitmap = (drawable as? BitmapDrawable)?.bitmap
                bitmap?.let {
                    WallpaperManager.getInstance(applicationContext).setBitmap(it)
                }
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
