package com.example.hiddenbeats

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.hiddenbeats.ui.theme.HiddenBeatsTheme

class MainActivity : ComponentActivity() {
    private val clientId = BuildConfig.SPOTIFY_CLIENT_ID
    private val redirectUri = "http://com.example.hiddenbeats/callback"
    private lateinit var spotifyController: SpotifyController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d("xD", clientId)
        spotifyController = SpotifyController(this, clientId, redirectUri)

        setContent {
            HiddenBeatsTheme {
                Scaffold { innerPadding ->
                    PlayerScreen(
                        onPlay = { spotifyController.play("spotify:track:4PTG3Z6ehGkBFwjybzWkR8") },
                        onPause = { spotifyController.pause() }
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
