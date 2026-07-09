package com.atlas.agent.service

import android.content.Context
import com.atlas.agent.screen.ScreenCaptureManager
import com.atlas.agent.streaming.StreamSession
import fi.iki.elonen.NanoHTTPD
import java.io.ByteArrayInputStream
import java.util.Locale

object LocalDemoServer : NanoHTTPD(8080) {

    private const val STREAM_PATH = "/stream"
    private const val TOUCH_PATH = "/touch"
    private const val KEYBOARD_PATH = "/keyboard"
    private const val CLIPBOARD_PATH = "/clipboard"
    private const val FILES_PATH = "/files"

    var isRunning: Boolean = false
        private set

    private var appContext: Context? = null
    private var lastTouchEvent: String = "idle"
    private var lastKeyboardText: String = ""
    private var lastClipboardText: String = ""

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun startServer() {
        if (isRunning) return
        start(SOCKET_READ_TIMEOUT, false)
        isRunning = true
    }

    fun stopServer() {
        if (!isRunning) return
        stop()
        isRunning = false
    }

    fun currentUrl(): String {
        val host = discoverLocalIp() ?: "127.0.0.1"
        return "http://$host:8080$STREAM_PATH"
    }

    fun latestTouchEvent(): String = lastTouchEvent
    fun latestKeyboardText(): String = lastKeyboardText
    fun latestClipboardText(): String = lastClipboardText

    override fun serve(session: IHTTPSession): Response {
        return when (session.uri) {
            STREAM_PATH -> serveStream(session)
            TOUCH_PATH -> handleTouch(session)
            KEYBOARD_PATH -> handleKeyboard(session)
            CLIPBOARD_PATH -> handleClipboard(session)
            FILES_PATH -> handleFiles(session)
            else -> serveIndex(session)
        }
    }

    private fun serveStream(session: IHTTPSession): Response {
        val frame = ScreenCaptureManager.latestFrameBytes() ?: createFallbackJpeg()
        return newFixedLengthResponse(
            Response.Status.OK,
            "image/jpeg",
            ByteArrayInputStream(frame),
            frame.size.toLong()
        )
    }

    private fun serveIndex(session: IHTTPSession): Response {
        val payload = """
            {
              "service": "Atlas Demo",
              "stream": "${currentUrl()}",
              "touch": "POST /touch",
              "keyboard": "POST /keyboard",
              "clipboard": "GET/POST /clipboard",
              "files": "GET /files"
            }
        """.trimIndent()
        return newFixedLengthResponse(Response.Status.OK, "application/json", payload)
    }

    private fun handleTouch(session: IHTTPSession): Response {
        val params = HashMap<String, String>()
        session.parseBody(params)
        val x = params["x"] ?: "0"
        val y = params["y"] ?: "0"
        val action = params["action"] ?: "tap"
        lastTouchEvent = "x=$x,y=$y,action=$action"
        return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"ok\":true}")
    }

    private fun handleKeyboard(session: IHTTPSession): Response {
        val params = HashMap<String, String>()
        session.parseBody(params)
        lastKeyboardText = params["text"] ?: ""
        return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"ok\":true}")
    }

    private fun handleClipboard(session: IHTTPSession): Response {
        val method = session.method.name.uppercase(Locale.US)
        return if (method == "POST") {
            val params = HashMap<String, String>()
            session.parseBody(params)
            lastClipboardText = params["text"] ?: ""
            newFixedLengthResponse(Response.Status.OK, "application/json", "{\"ok\":true}")
        } else {
            newFixedLengthResponse(Response.Status.OK, "application/json", "{\"text\":\"$lastClipboardText\"}")
        }
    }

    private fun handleFiles(session: IHTTPSession): Response {
        val context = appContext ?: return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"ok\":false}")
        val method = session.method.name.uppercase(Locale.US)
        return if (method == "POST") {
            val params = HashMap<String, String>()
            session.parseBody(params)
            val text = params["text"] ?: "demo"
            val file = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                ?.resolve("atlas-upload-${System.currentTimeMillis()}.txt")
            if (file != null) {
                file.writeText(text)
            }
            newFixedLengthResponse(Response.Status.OK, "application/json", "{\"ok\":true}")
        } else {
            val directory = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            val files = directory?.listFiles()?.sortedBy { it.name } ?: emptyList()
            val payload = files.joinToString(prefix = "[", postfix = "]") { "\"${it.name}\"" }
            newFixedLengthResponse(Response.Status.OK, "application/json", payload)
        }
    }

    private fun discoverLocalIp(): String? {
        val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            if (!networkInterface.isUp || networkInterface.isLoopback) continue
            val addresses = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                    return address.hostAddress
                }
            }
        }
        return null
    }

    private fun createFallbackJpeg(): ByteArray {
        val width = 320
        val height = 180
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.BLACK)
        val paint = android.graphics.Paint().apply { color = android.graphics.Color.WHITE; textSize = 28f }
        canvas.drawText("Atlas Demo", 20f, 90f, paint)
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, stream)
        bitmap.recycle()
        return stream.toByteArray()
    }
}
