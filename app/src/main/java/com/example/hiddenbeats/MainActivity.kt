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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var spotifyController: SpotifyController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Make App Full-Screen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        spotifyController = SpotifyController(this)

        setContent {
            HiddenBeatsTheme {
                var isPlaying by remember { mutableStateOf(false) }
                var hasTrackLoaded by remember { mutableStateOf(false) }
                var playbackProgress by remember { mutableFloatStateOf(0f) }
                var suppressProgress by remember { mutableIntStateOf(20) }

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
                            Log.d("Spotify", "Called with trackId: $trackId")
                            spotifyController.playTrackById(
                                id = trackId,
                                onSuccess = { trackName ->
                                    isPlaying = true
                                    hasTrackLoaded = true
                                    Log.d("Spotify", "Playing: $trackName")
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
            onConnected = { Log.d("MainActivity", "Connected to Spotify") }
        )
    }

    override fun onStop() {
        super.onStop()
        spotifyController.disconnect()
    }
}
