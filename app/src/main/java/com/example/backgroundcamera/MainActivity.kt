package com.example.backgroundcamera

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvFilePath: TextView

    private val recordingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isRecording = intent?.getBooleanExtra(
                CameraRecordingService.EXTRA_IS_RECORDING, false
            ) ?: false
            val filePath = intent?.getStringExtra(CameraRecordingService.EXTRA_FILE_PATH)
            val error = intent?.getStringExtra(CameraRecordingService.EXTRA_ERROR)

            if (error != null) {
                updateUI(isRecording = false, error = error)
            } else {
                updateUI(isRecording = isRecording, filePath = filePath)
            }
        }
    }

    private val requiredPermissions = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    ).apply {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }.toTypedArray()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(this, "Permissões concedidas!", Toast.LENGTH_SHORT).show()
            btnStart.isEnabled = true
        } else {
            Toast.makeText(
                this,
                "Permissões necessárias não concedidas.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        tvStatus = findViewById(R.id.tvStatus)
        tvFilePath = findViewById(R.id.tvFilePath)

        btnStart.setOnClickListener { handleStartRecording() }
        btnStop.setOnClickListener { handleStopRecording() }

        btnStart.isEnabled = false
        btnStop.isEnabled = false

        checkAndRequestPermissions()
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(CameraRecordingService.BROADCAST_STATUS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(recordingReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(recordingReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(recordingReceiver)
    }

    private fun handleStartRecording() {
        if (!hasPermissions()) {
            checkAndRequestPermissions()
            return
        }
        val intent = Intent(this, CameraRecordingService::class.java).apply {
            action = CameraRecordingService.ACTION_START
        }
        ContextCompat.startForegroundService(this, intent)
        updateUI(isRecording = true)
        Toast.makeText(this, "Gravação iniciada! Pode minimizar o app.", Toast.LENGTH_LONG).show()
    }

    private fun handleStopRecording() {
        val intent = Intent(this, CameraRecordingService::class.java).apply {
            action = CameraRecordingService.ACTION_STOP
        }
        startService(intent)
        updateUI(isRecording = false)
    }

    private fun updateUI(isRecording: Boolean, filePath: String? = null, error: String? = null) {
        runOnUiThread {
            btnStart.isEnabled = !isRecording && hasPermissions()
            btnStop.isEnabled = isRecording

            when {
                error != null -> {
                    tvStatus.text = "❌ Erro: $error"
                    tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                }
                isRecording -> {
                    tvStatus.text = "🔴 Gravando em segundo plano..."
                    tvStatus.setTextColor(getColor(android.R.color.holo_red_light))
                }
                else -> {
                    tvStatus.text = "⚪ Pronto para gravar"
                    tvStatus.setTextColor(getColor(android.R.color.darker_gray))
                }
            }

            if (filePath != null) {
                tvFilePath.text = "💾 Salvo em:\n$filePath"
            }
        }
    }

    private fun hasPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkAndRequestPermissions() {
        if (hasPermissions()) {
            btnStart.isEnabled = true
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }
}
