package com.dimrnhhh.moneytopia.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimrnhhh.moneytopia.database.realm
import com.dimrnhhh.moneytopia.models.Expense
import com.dimrnhhh.moneytopia.models.ExpenseSource
import com.dimrnhhh.moneytopia.models.Recurrence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class AddExpenseState(
    val amount: String = "",
    var category: String = "",
    val date: LocalDate = LocalDate.now(),
    val recurrence: Recurrence = Recurrence.None,
    val note: String = ""
)

class AddExpenseViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AddExpenseState())
    val uiState: StateFlow<AddExpenseState> = _uiState.asStateFlow()

    fun setAmount(amount: String) {
        var parsed = amount.toDoubleOrNull()
        if (amount.isEmpty()) {
            parsed = 0.0
        }
        if (parsed != null) {
            _uiState.update {
                it.copy(
                    amount = amount.trim().ifEmpty { "" }
                )
            }
        }
    }

    fun setCategory(category: String) {
        _uiState.update {
            it.copy(
                category = category
            )
        }
    }

    fun setDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                date = date
            )
        }
    }

    fun setNote(note: String) {
        _uiState.update {
            it.copy(
                note = note
            )
        }
    }

    fun submitExpense() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = LocalDateTime.now()
            realm.write {
                this.copyToRealm(
                    Expense(
                        amount = _uiState.value.amount.toDouble(),
                        category = _uiState.value.category,
                        date = _uiState.value.date.atTime(now.hour, now.minute, now.second),
                        recurrence = _uiState.value.recurrence,
                        note = _uiState.value.note,
                        smsId = "EXP_${UUID.randomUUID()}",
                        source = ExpenseSource.MANUAL
                    )
                )
            }
            _uiState.update {
                it.copy(
                    amount = "",
                    note = ""
                )
            }
        }
    }
}