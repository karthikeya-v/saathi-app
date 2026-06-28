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

    private val _vibe = MutableStateFlow("")
    val vibe: StateFlow<String> = _vibe.asStateFlow()

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
    fun setVibe(v: String) { _vibe.value = v }

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

    private fun save() {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            try {
                repo.saveProfile(
                    UserProfile(
                        name = _name.value.trim(),
                        age = _age.value,
                        gender = _gender.value,
                        interestedIn = _interestedIn.value,
                        vibe = _vibe.value
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
