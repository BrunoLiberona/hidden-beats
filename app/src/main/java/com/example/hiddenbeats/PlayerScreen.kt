package com.example.hiddenbeats

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

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
    var uriInput by remember { mutableStateOf(TextFieldValue("")) }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            result.contents?.let { scannedText ->
                uriInput = TextFieldValue(scannedText)
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("HiddenBeats 🎧", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        IconButton(onClick = {
            scanLauncher.launch(
                ScanOptions().apply {
                    setPrompt("Escanea un QR de canción")
                    setBeepEnabled(true)
                    setOrientationLocked(true)
                    setCaptureActivity(PortraitCaptureActivity::class.java)
                }
            )
        }) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = "Escanear QR")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!hasTrackLoaded) {
            OutlinedTextField(
                value = uriInput,
                onValueChange = { uriInput = it },
                label = { Text("URI de Spotify") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onPlayUri(uriInput.text) },
                enabled = uriInput.text.isNotBlank()
            ) {
                Text("▶ Reproducir")
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
