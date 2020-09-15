package com.assem.runningtracker.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.assem.runningtracker.R
import com.assem.runningtracker.ui.MainViewModel
import com.assem.runningtracker.ui.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint


/**
 * Created by Mohamed Assem on 15-Sep-20.
 * mohamed.assem.ali@gmail.com
 */

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticsViewModel by viewModels()

}