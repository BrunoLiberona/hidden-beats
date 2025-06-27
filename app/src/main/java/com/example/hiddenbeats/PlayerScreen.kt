package com.example.hiddenbeats

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("HiddenBeats 🎧", style = MaterialTheme.typography.headlineMedium)

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
//            Row {
//                Button(onClick = onTogglePlayPause, modifier = Modifier.padding(end = 8.dp)) {
//                    Text(if (isPlaying) "⏸ Pausar" else "▶ Reanudar")
//                }
//                Button(onClick = onStop) {
//                    Text("⏹ Detener")
//                }
//            }
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
