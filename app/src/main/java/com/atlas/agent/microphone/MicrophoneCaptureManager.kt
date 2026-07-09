package com.atlas.agent.microphone

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.util.concurrent.atomic.AtomicBoolean

object MicrophoneCaptureManager {
    private const val SAMPLE_RATE = 16000
    private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

    private var recorder: AudioRecord? = null
    private val isRecordingFlag = AtomicBoolean(false)
    private var latestPcmBytes: ByteArray = ByteArray(0)

    fun startCapture(): Boolean {
        if (isRecordingFlag.get()) return true
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize.coerceAtLeast(4096)
        )
        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord.release()
            return false
        }

        recorder = audioRecord
        isRecordingFlag.set(true)
        audioRecord.startRecording()

        Thread {
            val readBuffer = ByteArray(4096)
            while (isRecordingFlag.get()) {
                val read = audioRecord.read(readBuffer, 0, readBuffer.size)
                if (read > 0) {
                    latestPcmBytes = readBuffer.copyOf(read)
                }
            }
            audioRecord.stop()
            audioRecord.release()
            recorder = null
        }.start()

        return true
    }

    fun stopCapture() {
        isRecordingFlag.set(false)
        recorder?.stop()
        recorder?.release()
        recorder = null
    }

    fun isRecording(): Boolean = isRecordingFlag.get()

    fun latestPcmBytes(): ByteArray = latestPcmBytes.copyOf()
}
