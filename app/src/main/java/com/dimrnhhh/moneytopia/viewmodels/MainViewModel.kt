package com.dimrnhhh.moneytopia.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimrnhhh.moneytopia.api.Repository
import com.dimrnhhh.moneytopia.api.Resource
import com.dimrnhhh.moneytopia.models.AppUpdateInfoResponseModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private val _isReady = MutableStateFlow(true)
    val isReady = _isReady.asStateFlow()

    private val _appUpdateInfo = MutableStateFlow(AppUpdateInfoResponseModel())
    val appUpdateInfo: StateFlow<AppUpdateInfoResponseModel> = _appUpdateInfo.asStateFlow()

    private val repository = Repository()

    init {
        viewModelScope.launch {
            delay(1500L)
            _isReady.value = false
        }
    }

    fun getAppUpdateInfo() {
        viewModelScope.launch {
            repository.getAppUpdateInfo().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {

                    }

                    is Resource.Success -> {
                        _appUpdateInfo.value = resource.data ?: AppUpdateInfoResponseModel()
                    }

                    is Resource.Error -> {

                    }
                }
            }
        }
    }
}