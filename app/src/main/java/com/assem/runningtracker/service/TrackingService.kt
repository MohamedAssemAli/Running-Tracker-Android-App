package com.assem.runningtracker.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.assem.runningtracker.R
import com.assem.runningtracker.util.Constants.ACTION_PAUSE_SERVICE
import com.assem.runningtracker.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.assem.runningtracker.util.Constants.ACTION_STOP_SERVICE
import com.assem.runningtracker.util.Constants.FASTEST_LOCATION_INTERVAL
import com.assem.runningtracker.util.Constants.LOCATION_UPDATE_INTERVAL
import com.assem.runningtracker.util.Constants.NOTIFICATION_CHANNEL_ID
import com.assem.runningtracker.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.assem.runningtracker.util.Constants.NOTIFICATION_ID
import com.assem.runningtracker.util.Constants.TIMER_UPDATE_INTERVAL
import com.assem.runningtracker.util.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


/**
 * Created by Mohamed Assem on 15-Sep-20.
 * mohamed.assem.ali@gmail.com
 */

typealias PolyLine = MutableList<LatLng>
typealias PolyLines = MutableList<PolyLine>

@AndroidEntryPoint
class TrackingService : LifecycleService() {
    private var isFirstRun = true
    private var isServiceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    private lateinit var currentNotificationBuilder: NotificationCompat.Builder


    // timer
    private var timeRunInSeconds = MutableLiveData<Long>()
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        isTracking.observe(this, {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
        currentNotificationBuilder = baseNotificationBuilder
    }

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<PolyLines>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service...")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseTrackingService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killTrackingService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun pauseTrackingService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun killTrackingService() {
        isServiceKilled = true
        isFirstRun = true
        pauseTrackingService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if (!isServiceKilled) {
            currentNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoints(location)
                        Timber.d("New Location: ${location.latitude} + ${location.longitude}")
                    }
                }
            }
        }
    }


    private fun addPathPoints(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun startTimer() {
        addEmptyPolyLine()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                //  time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted
                // post the new lap time
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun addEmptyPolyLine() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, {
            if (!isServiceKilled) {
                val notification = currentNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}

/*

    /*
    // wadi degla ocotober
    start lat2 + long2 = 29.964042, 30.963758
    start lat4 + long4 = 29.965796, 30.966464

    end lat1 + long1 = 29.971712, 30.957243
    end lat3 + long3 = 29.973405, 30.959912

    // place before
    29.962015, 30.968805

    // place inside
    29.968242, 30.962053

    // place after
    29.977700, 30.952824
     */

    private var plot1 =
        Plot(29.965796, 30.966464, 29.964042, 30.963758, 29.973405, 30.959912, 29.971712, 30.957243)

    private var startLocation1 = Location("start1")
    private var startLocation2 = Location("start2")
    private var endLocation1 = Location("end2")
    private var endLocation2 = Location("end2")


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)

            startLocation1.latitude = plot1.lat1
            startLocation1.longitude = plot1.long1

            startLocation2.latitude = plot1.lat3
            startLocation2.longitude = plot1.long3

            endLocation1.latitude = plot1.lat2
            endLocation1.longitude = plot1.long2

            endLocation2.latitude = plot1.lat4
            endLocation2.longitude = plot1.long4

            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        if (
                            location.latitude > startLocation1.latitude && location.latitude > startLocation2.latitude
                            && location.latitude < endLocation1.latitude && location.latitude < endLocation2.latitude
                            && location.longitude < startLocation1.longitude && location.longitude < startLocation2.longitude
                            && location.longitude > endLocation1.longitude && location.longitude > endLocation2.longitude
                        ) {
                            Timber.d("Assem is inside the plot")
                        } else{
                            Timber.d("Assem is outside the plot")
                        }
                            /*
                            if (
                            location.latitude > endLocation1.latitude && location.latitude > endLocation2.latitude
                        ) {
                            Timber.d("Assem is after the plot")
                        } else if (
                            location.latitude < startLocation1.latitude && location.latitude < startLocation2.latitude
                        ) {
                            Timber.d("Assem is before the plot")
                        }*/
                        /*
 */
                        addPathPoints(location)
//                        Timber.d("Assem New Location => ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }


 */
