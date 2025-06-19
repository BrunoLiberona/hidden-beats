package com.example.hiddenbeats

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import com.example.hiddenbeats.ui.theme.HiddenBeatsTheme
import android.widget.Toast

class MainActivity : ComponentActivity() {
    private val clientId = BuildConfig.SPOTIFY_CLIENT_ID
    private val redirectUri = "http://com.example.hiddenbeats/callback"
    private lateinit var spotifyController: SpotifyController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        spotifyController = SpotifyController(this, clientId, redirectUri)

        setContent {
            HiddenBeatsTheme {
                var isPlaying by remember { mutableStateOf(false) }
                var hasTrackLoaded by remember { mutableStateOf(false) }

                Scaffold { _ ->
                    PlayerScreen(
                        onPlayUri = { trackId ->
                            spotifyController.playTrackById(
                                id = trackId,
                                onSuccess = { trackName ->
                                    isPlaying = true
                                    hasTrackLoaded = true
                                    Log.d("Success", "Reproduciendo: $trackName")
                                },
                                onFailure = {
                                    Toast.makeText(this, "El ID no es válido o no se pudo cargar.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onTogglePlayPause = {
                            if (isPlaying) {
                                spotifyController.pause()
                            } else {
                                spotifyController.resume()
                            }
                            isPlaying = !isPlaying
                        },
                        onStop = {
                            spotifyController.pause()
                            isPlaying = false
                            hasTrackLoaded = false
                        },
                        isPlaying = isPlaying,
                        hasTrackLoaded = hasTrackLoaded
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        spotifyController.connect(
            onConnected = { Log.d("MainActivity", "Connected to Spotify") },
            onFailure = { Log.e("MainActivity", "Spotify connection failed", it) }
        )
    }

    override fun onStop() {
        super.onStop()
        spotifyController.disconnect()
    }
}
