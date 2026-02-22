package com.example.myapplicationxx

import android.content.Intent
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

/**
 * Flutter Activity for displaying the movies module.
 * This handles the method channel for the 'showTrailer' method that returns control
 * to the native Android app to display trailers.
 */
class FlutterMoviesActivity : FlutterActivity() {
    companion object {
        private const val METHOD_CHANNEL = "com.example.movies/channel"
    }

    private lateinit var methodChannel: MethodChannel

    override fun getInitialRoute(): String {
        return intent?.getStringExtra("route") ?: "/movies"
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        methodChannel = MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            METHOD_CHANNEL
        )

        methodChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "showTrailer" -> {
                    val videoKey = call.argument<String>("videoKey")
                    val movieId = call.argument<Int>("movieId")
                    val movieTitle = call.argument<String>("movieTitle") ?: "Movie"

                    if (videoKey != null && movieId != null) {
                        launchTrailer(videoKey, movieId, movieTitle)
                        result.success(null)
                    } else {
                        result.error(
                            "INVALID_ARGS",
                            "videoKey and movieId are required",
                            null
                        )
                    }
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun launchTrailer(videoKey: String, movieId: Int, movieTitle: String) {
        try {
            val intent = Intent(this, TrailerActivity::class.java).apply {
                putExtra("videoKey", videoKey)
                putExtra("movieId", movieId)
                putExtra("movieTitle", movieTitle)
            }
            startActivity(intent)
            Log.d("FlutterMoviesActivity", "Trailer activity launched with videoKey: $videoKey, title: $movieTitle")
        } catch (e: Exception) {
            Log.e("FlutterMoviesActivity", "Error launching trailer: ${e.message}", e)
            methodChannel.invokeMethod("trailerError", mapOf(
                "error" to e.message
            ))
        }
    }
}
