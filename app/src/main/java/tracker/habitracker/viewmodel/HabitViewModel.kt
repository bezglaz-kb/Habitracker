package tracker.habitracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tracker.habitracker.db.AppDb
import tracker.habitracker.entity.Habit

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val habitDao = AppDb.getDatabase(application).habitDao()

    val habitsState = habitDao.getAllHabits().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun createNewHabit(name: String) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            val newHabit = Habit(
                name = name,
                progressRow = 0,
                history = List(7) { false }
            )
            habitDao.insertOrUpdate(newHabit)
        }
    }

    fun toggleDay(habitName: String, clickedIndex: Int) {
        viewModelScope.launch {
            val currentHabit = habitsState.value.find { it.name == habitName } ?: return@launch
            val updatedHistory = currentHabit.history.toMutableList().apply {
                this[clickedIndex] = !this[clickedIndex]
            }
            val newProgress = updatedHistory.count { it }
            val updatedHabit =
                currentHabit.copy(history = updatedHistory, progressRow = newProgress)
            habitDao.insertOrUpdate(updatedHabit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitDao.deleteHabit(habit)
        }
    }
}