package com.apps.travel_app.ui.components.login
/**
 * A view model used for fetching and forwarding the data regarding Login.
 * Used in combination with LoginActivity. Has functions of updating, resetting,
 * and tracking the current profile.
 */
import androidx.compose.runtime.Immutable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.facebook.Profile
import com.facebook.ProfileTracker


class LoginViewModel: ViewModel() {
    private val profileTracker =
        object : ProfileTracker() {
            override fun onCurrentProfileChanged(oldProfile: Profile?, currentProfile: Profile?) {
                if (currentProfile != null) {
                    this@LoginViewModel.updateProfile(currentProfile)
                } else {
                    this@LoginViewModel.resetProfile()
                }
            }
        }

    private val _profileViewState = MutableLiveData(ProfileViewState(Profile.getCurrentProfile()))

    val profileViewState: LiveData<ProfileViewState> = _profileViewState

    override fun onCleared() {
        profileTracker.stopTracking()
        super.onCleared()
    }

    private fun updateProfile(profile: Profile) {
        _profileViewState.value = _profileViewState.value?.copy(profile = profile)
    }

    private fun resetProfile() {
        _profileViewState.value = _profileViewState.value?.copy(profile = null)
    }
}

@Immutable
data class ProfileViewState(
    val profile: Profile? = null
)
