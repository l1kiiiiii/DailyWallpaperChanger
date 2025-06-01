package com.example.daily

import androidx.compose.ui.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.io.IOException
import coil.imageLoader
import coil.request.SuccessResult
import androidx.work.*
import com.example.daily.worker.WallpaperViewModel
import java.util.concurrent.TimeUnit
import android.Manifest // Required for Manifest.permission.SET_WALLPAPER
import android.content.pm.PackageManager // Required for PackageManager.PERMISSION_GRANTED
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope // For launching coroutine on button click
import androidx.core.content.ContextCompat // Required for ContextCompat.checkSelfPermission
import com.example.daily.ui.theme.DailyTheme
import kotlinx.coroutines.launch // For scope.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleDailyWallpaperUpdate(applicationContext)
        setContent {
            DailyTheme {
                WallpaperApp()
            }
        }
    }
}
fun scheduleDailyWallpaperUpdate(context: Context) { // Changed to Context for broader usability
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val workRequest = PeriodicWorkRequestBuilder<WallpaperWorker>(1, TimeUnit.DAYS)
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(
            "daily_wallpaper_update",
            ExistingPeriodicWorkPolicy.KEEP, // KEEPS the existing work if it's already scheduled
            workRequest
        )
}
@Composable
fun WallpaperApp(viewModel: WallpaperViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current
    val imageUrl by viewModel.imageUrl
    val imageTitle by viewModel.imageTitle

    LaunchedEffect(Unit) {
        viewModel.fetchImageOfTheDay()
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black // Set background to pure black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = imageTitle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = imageTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White // Ensure text is readable on black background
                )
                Spacer(modifier = Modifier.height(16.dp))
                SetWallpaperButton(imageUrl)
            } else {
                Text(
                    text = "Loading image...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White // Ensure text is readable
                )
            }
        }
    }
}
@Composable
fun SetWallpaperButton(imageUrl: String) {
    val context = LocalContext.current
    val wallpaperManager = WallpaperManager.getInstance(context)
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope() // For launching coroutines
    var hasSetWallpaperPermission by remember {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            mutableStateOf(true) // Permission not needed or implicitly granted before Android 13
        } else {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SET_WALLPAPER
                ) == PackageManager.PERMISSION_GRANTED
            )
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            hasSetWallpaperPermission = true
        } else {
            hasSetWallpaperPermission = false

        }
    }
    Button(onClick = {
        if (hasSetWallpaperPermission) { // Check our tracked permission state
            scope.launch { // Use the coroutine scope
                try {
                    val request = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .allowHardware(false) // Good practice for bitmaps intended for WallpaperManager
                        .build()
                    val result = context.imageLoader.execute(request)
                    if (result is SuccessResult) {
                        val drawable = result.drawable
                        val bitmapToSet: Bitmap? = if (drawable is android.graphics.drawable.BitmapDrawable) {
                            drawable.bitmap.copy(Bitmap.Config.ARGB_8888, true)
                        } else {
                            // Fallback for other drawable types (draw to a new bitmap)
                            // This part might need to be more robust depending on image sources
                            try {
                                val bmp = Bitmap.createBitmap(
                                    drawable.intrinsicWidth.coerceAtLeast(1),
                                    drawable.intrinsicHeight.coerceAtLeast(1),
                                    Bitmap.Config.ARGB_8888
                                )
                                val canvas = android.graphics.Canvas(bmp)
                                drawable.setBounds(0, 0, canvas.width, canvas.height)
                                drawable.draw(canvas)
                                bmp
                            } catch (e: Exception) {
                                null // Could not convert
                            }
                        }
                        bitmapToSet?.let {
                            wallpaperManager.setBitmap(it)
                            showDialog = true
                        } ?: run {
                            // Log error or show toast: "Failed to prepare image for wallpaper"
                        }
                    } else {
                        // Log error or show toast: "Failed to load image"
                    }
                } catch (e: IOException) {
                    // Log error or show toast: "Error setting wallpaper"
                }
            }
        } else {
            // Request permission if not granted (and on Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.SET_WALLPAPER)
            }
        }
    }) {
        Text("Set as Wallpaper")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Success") },
            text = { Text("Wallpaper set successfully!") },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}