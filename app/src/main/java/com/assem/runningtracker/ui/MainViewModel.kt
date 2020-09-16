package com.assem.runningtracker.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assem.runningtracker.data.db.Plot
import com.assem.runningtracker.data.repository.MainRepository
import kotlinx.coroutines.launch


/**
 * Created by Mohamed Assem on 15-Sep-20.
 * mohamed.assem.ali@gmail.com
 */

class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
) : ViewModel() {

    /*
    // wadi degla
    start lat1 + long1 = 29.971712, 30.957243
    start lat3 + long3 = 29.973405, 30.959912
    end lat2 + long2 = 29.964042, 30.963758
    end lat4 + long4 = 29.965796, 30.966464
     */
    private var plot1 =
        Plot(29.971712, 30.957243, 29.973405, 30.959912, 29.964042, 30.963758, 29.965796, 30.966464)

    init {
        insertPlot(plot1)
    }

    fun insertPlot(plot: Plot) = viewModelScope.launch {
        mainRepository.insertPlot(plot)
    }
}