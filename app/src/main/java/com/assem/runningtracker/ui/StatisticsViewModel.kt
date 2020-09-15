package com.assem.runningtracker.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.assem.runningtracker.data.repository.MainRepository


/**
 * Created by Mohamed Assem on 15-Sep-20.
 * mohamed.assem.ali@gmail.com
 */

class StatisticsViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
) : ViewModel() {

}