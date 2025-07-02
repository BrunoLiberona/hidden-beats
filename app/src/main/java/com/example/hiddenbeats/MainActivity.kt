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
import kotlinx.coroutines.delay

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
                var playbackProgress by remember { mutableFloatStateOf(0f) }
                var suppressProgress by remember { mutableStateOf(20) }

                LaunchedEffect(Unit) {
                    while (true) {
                        delay(100)

                        if (suppressProgress < 20) {
                            suppressProgress += 1
                            continue
                        }

                        spotifyController.getPlayerProgress { position, duration ->
                            playbackProgress = if (duration > 0) position / duration.toFloat() else 0f
                        }
                    }
                }

                Scaffold { _ ->
                    PlayerScreen(
                        onPlayUri = { trackId ->
                            spotifyController.playTrackById(
                                id = trackId,
                                onSuccess = { trackName ->
                                    isPlaying = true
                                    hasTrackLoaded = true
                                    Log.d("Spotify", "Reproduciendo: $trackName")
                                    Toast.makeText(this, "Reproduciendo: $trackName", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = {
                                    Log.e("Spotify", "Song not found!")
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
                        hasTrackLoaded = hasTrackLoaded,
                        onSeekTo = { fraction ->
                            suppressProgress = 0
                            playbackProgress = fraction
                            spotifyController.seekToFraction(fraction)
                        },
                        playbackPositionFraction = playbackProgress,
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
