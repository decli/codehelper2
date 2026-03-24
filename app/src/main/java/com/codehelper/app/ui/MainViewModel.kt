package com.codehelper.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codehelper.app.data.db.AppDatabase
import com.codehelper.app.data.db.DeletedCodeEntity
import com.codehelper.app.data.model.ExtractionRule
import com.codehelper.app.data.model.PickupCode
import com.codehelper.app.data.repository.RuleRepository
import com.codehelper.app.data.repository.SmsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class TimeFilter(val hours: Int, val label: String) {
    HOURS_12(12, "12小时"),
    DAY_1(24, "1天"),
    DAY_2(48, "2天"),
    DAY_5(120, "5天"),
}

data class MainUiState(
    val codes: List<PickupCode> = emptyList(),
    val timeFilter: TimeFilter = TimeFilter.HOURS_12,
    val isLoading: Boolean = false,
    val showDeleted: Boolean = false,
    val rules: List<ExtractionRule> = emptyList(),
    val hasPermission: Boolean = false,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val smsRepository = SmsRepository(application)
    private val ruleRepository = RuleRepository(application)
    private val deletedCodeDao = AppDatabase.getInstance(application).deletedCodeDao()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ruleRepository.rules.collect { rules ->
                _uiState.value = _uiState.value.copy(rules = rules)
                if (_uiState.value.hasPermission) {
                    loadCodes()
                }
            }
        }
    }

    fun onPermissionGranted() {
        _uiState.value = _uiState.value.copy(hasPermission = true)
        loadCodes()
    }

    fun onPermissionDenied() {
        _uiState.value = _uiState.value.copy(hasPermission = false)
    }

    fun setTimeFilter(filter: TimeFilter) {
        _uiState.value = _uiState.value.copy(timeFilter = filter)
        loadCodes()
    }

    fun loadCodes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val rules = _uiState.value.rules.ifEmpty {
                    ruleRepository.rules.first()
                }
                val messages = smsRepository.readSms(_uiState.value.timeFilter.hours)
                val allCodes = smsRepository.extractCodes(messages, rules)
                val deletedIds = deletedCodeDao.getAllDeletedIdsOnce().toSet()

                val displayCodes = if (_uiState.value.showDeleted) {
                    allCodes.map { code ->
                        if (code.id in deletedIds) code.copy(isDeleted = true) else code
                    }
                } else {
                    allCodes.filter { it.id !in deletedIds }
                }

                _uiState.value = _uiState.value.copy(
                    codes = displayCodes,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun deleteCode(codeId: String) {
        viewModelScope.launch {
            deletedCodeDao.markDeleted(DeletedCodeEntity(codeId))
            _uiState.value = _uiState.value.copy(
                codes = _uiState.value.codes.filter { it.id != codeId }
            )
        }
    }

    fun toggleShowDeleted() {
        _uiState.value = _uiState.value.copy(showDeleted = !_uiState.value.showDeleted)
        loadCodes()
    }

    fun saveRules(rules: List<ExtractionRule>) {
        viewModelScope.launch {
            ruleRepository.saveRules(rules)
        }
    }
}
