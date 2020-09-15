package com.assem.runningtracker.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber


/**
 * Created by Mohamed Assem on 14-Sep-20.
 * mohamed.assem.ali@gmail.com
 */

// we need to annotate this app class to mark our app as injectable
@HiltAndroidApp
class BaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

}