package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.ActivityLog
import com.kushwahahardware.data.repository.RBACRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ActivityLogsViewModel @Inject constructor(
    private val repository: RBACRepository
) : ViewModel() {

    val logs: StateFlow<List<ActivityLog>> = repository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
