package com.example.backgroundcamera

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraRecordingService : Service() {

    companion object {
        private const val TAG = "CameraRecordingService"
        const val CHANNEL_ID = "camera_recording_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_RECORDING"
        const val ACTION_STOP = "ACTION_STOP_RECORDING"
        const val BROADCAST_STATUS = "com.example.backgroundcamera.RECORDING_STATUS"
        const val EXTRA_IS_RECORDING = "is_recording"
        const val E
