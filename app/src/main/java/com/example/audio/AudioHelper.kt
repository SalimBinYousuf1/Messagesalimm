package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var amplitudeJob: Job? = null
    private var startTime: Long = 0L

    private val _amplitudes = MutableStateFlow<List<Int>>(emptyList())
    val amplitudes: StateFlow<List<Int>> = _amplitudes.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationMs = MutableStateFlow(0)
    val recordingDurationMs: StateFlow<Int> = _recordingDurationMs.asStateFlow()

    fun startRecording(coroutineScope: CoroutineScope): Boolean {
        return try {
            val audioDir = File(context.cacheDir, "voice_notes").apply { mkdirs() }
            val file = File(audioDir, "voice_${System.currentTimeMillis()}.m4a")
            outputFile = file

            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mr.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = mr
            startTime = System.currentTimeMillis()
            _isRecording.value = true
            _amplitudes.value = emptyList()

            amplitudeJob = coroutineScope.launch(Dispatchers.Default) {
                while (isActive && _isRecording.value) {
                    val maxAmp = try {
                        recorder?.maxAmplitude ?: 0
                    } catch (e: Exception) {
                        0
                    }
                    // Normalize amplitude 0..32767 to 5..100
                    val normalized = ((maxAmp.toFloat() / 32767f) * 95f + 5f).toInt().coerceIn(5, 100)
                    _amplitudes.value = (_amplitudes.value + normalized).takeLast(40)
                    _recordingDurationMs.value = (System.currentTimeMillis() - startTime).toInt()
                    delay(100)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "Error starting recording: ${e.message}", e)
            stopRecording()
            false
        }
    }

    data class RecordResult(
        val file: File,
        val durationMs: Int,
        val amplitudes: List<Int>
    )

    fun stopRecording(): RecordResult? {
        amplitudeJob?.cancel()
        amplitudeJob = null
        _isRecording.value = false

        val mr = recorder
        recorder = null

        return try {
            mr?.stop()
            mr?.release()
            val file = outputFile
            if (file != null && file.exists() && file.length() > 0) {
                val dur = (System.currentTimeMillis() - startTime).toInt()
                val amps = if (_amplitudes.value.isNotEmpty()) _amplitudes.value else listOf(20, 45, 75, 40, 20)
                RecordResult(file, dur, amps)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "Error stopping recording: ${e.message}")
            try { mr?.release() } catch (_: Exception) {}
            null
        }
    }

    fun cancelRecording() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        _isRecording.value = false
        try {
            recorder?.stop()
            recorder?.release()
        } catch (_: Exception) {}
        recorder = null
        outputFile?.delete()
        outputFile = null
    }
}

class AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    private val _currentPlayingUri = MutableStateFlow<String?>(null)
    val currentPlayingUri: StateFlow<String?> = _currentPlayingUri.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun play(uri: String, coroutineScope: CoroutineScope) {
        if (_currentPlayingUri.value == uri && _isPlaying.value) {
            pause()
            return
        }

        stop()

        try {
            val mp = MediaPlayer()
            mp.setDataSource(uri)
            mp.prepare()
            mp.setOnCompletionListener {
                _isPlaying.value = false
                _playbackProgress.value = 0f
                _currentPlayingUri.value = null
                progressJob?.cancel()
            }
            mp.start()
            mediaPlayer = mp
            _currentPlayingUri.value = uri
            _isPlaying.value = true

            progressJob = coroutineScope.launch(Dispatchers.Default) {
                while (isActive && mp.isPlaying) {
                    val pos = mp.currentPosition
                    val total = mp.duration
                    if (total > 0) {
                        _playbackProgress.value = pos.toFloat() / total.toFloat()
                    }
                    delay(50)
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Playback error for $uri: ${e.message}")
            stop()
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        _isPlaying.value = false
        progressJob?.cancel()
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _isPlaying.value = false
        _currentPlayingUri.value = null
        _playbackProgress.value = 0f
    }
}
