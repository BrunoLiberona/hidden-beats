package com.example.hiddenbeats

import android.content.Context
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote

class SpotifyController(
    private val context: Context,
    private val clientId: String,
    private val redirectUri: String
) {
    private var spotifyAppRemote: SpotifyAppRemote? = null

    fun connect(onConnected: () -> Unit, onFailure: (Throwable) -> Unit) {
        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                Log.d("SpotifyController", "Connected to Spotify")
                onConnected()
            }

            override fun onFailure(throwable: Throwable) {
                Log.e("SpotifyController", "Connection failed", throwable)
                onFailure(throwable)
            }
        })
    }

    fun playTrackById(
        id: String,
        onSuccess: (String) -> Unit,
        onFailure: () -> Unit
    ) {
        val uri = "spotify:track:$id"
        spotifyAppRemote?.playerApi?.play(uri)

        spotifyAppRemote?.playerApi
            ?.subscribeToPlayerState()
            ?.setEventCallback { playerState ->
                if (!playerState.isPaused) {
                    onSuccess(playerState.track.name ?: "Canción sin nombre")
                }
            }
    }

    fun pause() {
        spotifyAppRemote?.playerApi?.pause()
    }

    fun resume() {
        spotifyAppRemote?.playerApi?.resume()
    }

    fun seekToFraction(fraction: Float) {
        spotifyAppRemote?.playerApi?.getPlayerState()?.setResultCallback { state ->
            val duration = state.track?.duration ?: 0
            val target = (duration * fraction).toLong()
            spotifyAppRemote?.playerApi?.seekTo(target)
        }
    }

    fun getPlayerProgress(onResult: (Long, Long) -> Unit) {
        spotifyAppRemote?.playerApi?.getPlayerState()?.setResultCallback { state ->
            val position = state.playbackPosition
            val duration = state.track?.duration ?: 0
            onResult(position, duration)
        }
    }

    fun disconnect() {
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }
}
