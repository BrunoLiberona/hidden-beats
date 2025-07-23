package com.example.hiddenbeats

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    onPlayUri: (String) -> Unit,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onSeekTo: (Float) -> Unit,
    playbackPositionFraction: Float,
    isPlaying: Boolean,
    hasTrackLoaded: Boolean
) {
    var scannedText by remember { mutableStateOf<String?>(null) }
    var shouldAutoPlay by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            result.contents?.let { scanned ->
                scannedText = scanned
                shouldAutoPlay = true
            }
        }
    )

    LaunchedEffect(scannedText) {
        if (shouldAutoPlay && scannedText != null) {
            isLoading = true
            Log.d("PlayerScreen", "Triggered with scannedText: $scannedText")

            delay(1000)

            onPlayUri(scannedText!!)
            shouldAutoPlay = false
        }
    }

    LaunchedEffect(hasTrackLoaded) {
        if (isLoading && hasTrackLoaded) {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("HiddenBeats 🎧", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(64.dp))

        if (!hasTrackLoaded) {
            Button(
                onClick = {
                    scanLauncher.launch(
                        ScanOptions().apply {
                            setPrompt("Escanea el QR de la canción")
                            setOrientationLocked(true)
                            setCaptureActivity(PortraitCaptureActivity::class.java)
                        }
                    )
                },
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                enabled = !isLoading
            ) {
                if (!isLoading) {
                    Text(
                        "Escanear QR",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                    )
                }
            }
        } else {
            CircularSeekBar(
                progress = playbackPositionFraction,
                onProgressChange = { newProgress ->
                    onSeekTo(newProgress)
                },
                isPlaying = isPlaying,
                onPlayPause = onTogglePlayPause,
                onStop = onStop
            )
        }
    }
}
