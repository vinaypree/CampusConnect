package com.yourname.campusconnect.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.yourname.campusconnect.data.models.User
import com.yourname.campusconnect.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    sealed class ProfileState {
        object Idle : ProfileState(); object Loading : ProfileState(); object Success : ProfileState(); data class Error(val message: String) : ProfileState()
    }

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                userRepository.getUserProfile(uid).onSuccess {
                    _userProfile.value = it
                }
            }
        }
    }

    fun saveProfile(name: String, department: String, year: Int, bio: String, phone: String, skillsToTeach: List<String>, skillsToLearn: List<String>, interests: List<String>) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            if (name.isBlank() || department.isBlank() || year == 0 || bio.isBlank() || phone.isBlank()) {
                _profileState.value = ProfileState.Error("Please fill in all required fields.")
                return@launch
            }
            if (skillsToTeach.isEmpty() && interests.isEmpty()) {
                _profileState.value = ProfileState.Error("Please add at least one skill or interest.")
                return@launch
            }
            val currentUser = auth.currentUser ?: run {
                _profileState.value = ProfileState.Error("User not logged in.")
                return@launch
            }
            val userProfile = User(uid = currentUser.uid, name = name, email = currentUser.email ?: "", department = department, year = year, bio = bio, phone = phone, skillsCanTeach = skillsToTeach, skillsWantToLearn = skillsToLearn, interests = interests)
            userRepository.createUserProfile(userProfile).onSuccess {
                _profileState.value = ProfileState.Success
            }.onFailure {
                _profileState.value = ProfileState.Error(it.message ?: "Save failed.")
            }
        }
    }
}