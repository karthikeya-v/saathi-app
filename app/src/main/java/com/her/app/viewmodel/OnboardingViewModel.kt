package com.her.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.her.app.data.FirestoreRepository
import com.her.app.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {

    private val repo = FirestoreRepository()

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _age = MutableStateFlow(25)
    val age: StateFlow<Int> = _age.asStateFlow()

    private val _gender = MutableStateFlow("")
    val gender: StateFlow<String> = _gender.asStateFlow()

    private val _interestedIn = MutableStateFlow("")
    val interestedIn: StateFlow<String> = _interestedIn.asStateFlow()

    // 5 quiz answers (1-7 scale), default 4 = neutral
    private val _quizAnswers = MutableStateFlow(listOf(4, 4, 4, 4, 4))
    val quizAnswers: StateFlow<List<Int>> = _quizAnswers.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isDone = MutableStateFlow(false)
    val isDone: StateFlow<Boolean> = _isDone.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun setName(v: String) { _name.value = v }
    fun setAge(v: Int) { _age.value = v.coerceIn(18, 65) }
    fun setGender(v: String) { _gender.value = v }
    fun setInterestedIn(v: String) { _interestedIn.value = v }
    fun setQuizAnswer(index: Int, value: Int) {
        val updated = _quizAnswers.value.toMutableList()
        updated[index] = value
        _quizAnswers.value = updated
    }

    fun next() {
        val step = _currentStep.value
        if (step < 4) {
            _currentStep.value = step + 1
        } else {
            save()
        }
    }

    fun back() {
        if (_currentStep.value > 0) _currentStep.value--
    }

    private fun computeMbti(answers: List<Int>): String {
        // Each answer: 1-3 = left dimension, 4 = neutral (pick left), 5-7 = right dimension
        // Q0: agree=E, disagree=I
        // Q1: agree=N, disagree=S
        // Q2: agree=T, disagree=F
        // Q3: agree=J, disagree=P
        // Q4: agree=A, disagree=T (identity)
        val e = if (answers[0] >= 5) "E" else "I"
        val n = if (answers[1] >= 5) "N" else "S"
        val t = if (answers[2] >= 5) "T" else "F"
        val j = if (answers[3] >= 5) "J" else "P"
        val a = if (answers[4] >= 5) "A" else "T"
        return "$e$n$t$j-$a"
    }

    private fun save() {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            try {
                val mbtiType = computeMbti(_quizAnswers.value)
                repo.saveProfile(
                    UserProfile(
                        name = _name.value.trim(),
                        age = _age.value,
                        gender = _gender.value,
                        interestedIn = _interestedIn.value,
                        mbtiType = mbtiType
                    )
                )
                _isDone.value = true
            } catch (e: Exception) {
                _error.value = "Couldn't save profile. Try again."
            }
            _isSaving.value = false
        }
    }
}
