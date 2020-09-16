package com.assem.runningtracker.data.repository

import com.assem.runningtracker.data.db.Plot
import com.assem.runningtracker.data.db.Run
import com.assem.runningtracker.data.db.RunDAO
import javax.inject.Inject


/**
 * Created by Mohamed Assem on 15-Sep-20.
 * mohamed.assem.ali@gmail.com
 */

class MainRepository @Inject constructor(
    val runDao: RunDAO
) {

    suspend fun insertPlot(plot: Plot) = runDao.insertPlot(plot)

    fun getAllPlots() = runDao.getAllPlots()

    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByTimeInMillis() = runDao.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByAvgSpeed() = runDao.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()
}