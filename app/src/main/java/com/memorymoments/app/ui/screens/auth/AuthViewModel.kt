package com.memorymoments.app.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.model.CaregiverProfile
import com.memorymoments.app.model.PatientProfile
import com.memorymoments.app.model.UserAccount
import com.memorymoments.app.model.UserRole
import com.memorymoments.app.repository.AuthRepository
import com.memorymoments.app.repository.IdentifierType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AuthNavigationEvent {
    data object NavigateToLogin : AuthNavigationEvent
    data object NavigateToSignUp : AuthNavigationEvent
    data object NavigateToPatientOnboarding : AuthNavigationEvent
    data object NavigateToCaregiverOnboarding : AuthNavigationEvent
    data object NavigateToHome : AuthNavigationEvent
    data object NavigateToCaregiverDashboard : AuthNavigationEvent
}

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val selectedRole: UserRole = UserRole.PATIENT,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val identifierError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
)

data class SignUpUiState(
    val identifier: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedRole: UserRole = UserRole.PATIENT,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val identifierError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null
)

data class PatientOnboardingUiState(
    val currentStep: Int = 0,
    val fullName: String = "",
    val age: String = "",
    val dateOfBirth: String = "",
    val gender: String = "Female",
    val contactInfo: String = "",
    val diagnosedConditions: List<String> = emptyList(),
    val emergencyContactName: String = "",
    val emergencyContactRelationship: String = "",
    val emergencyContactPhone: String = "",
    val language: String = "English",
    val emergencyContact: String = "",
    val caregiverName: String = "",
    val healthContext: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepo = AuthRepository(application)

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow(SignUpUiState())
    val signUpState: StateFlow<SignUpUiState> = _signUpState.asStateFlow()

    private val _patientOnboardingState = MutableStateFlow(PatientOnboardingUiState())
    val patientOnboardingState: StateFlow<PatientOnboardingUiState> = _patientOnboardingState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<AuthNavigationEvent>()
    val navigationEvents: SharedFlow<AuthNavigationEvent> = _navigationEvents.asSharedFlow()

    val currentUser = authRepo.currentUser
    val currentUserRole = authRepo.currentUserRole

    // --- Login Actions ---

    fun onLoginIdentifierChange(value: String) {
        _loginState.update { it.copy(identifier = value, identifierError = null, generalError = null) }
    }

    fun onLoginPasswordChange(value: String) {
        _loginState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    fun onLoginRoleChange(role: UserRole) {
        _loginState.update { it.copy(selectedRole = role, generalError = null) }
    }

    fun toggleLoginPasswordVisibility() {
        _loginState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun submitLogin() {
        val state = _loginState.value
        val identifier = state.identifier.trim()
        val password = state.password

        var hasError = false
        var identError: String? = null
        var passError: String? = null

        if (identifier.isBlank()) {
            identError = "Please enter your email or phone number."
            hasError = true
        } else {
            val type = AuthRepository.getIdentifierType(identifier)
            if (type == IdentifierType.INVALID) {
                identError = if (identifier.contains("@")) {
                    "Please enter a valid email address."
                } else {
                    "Please enter a valid phone number (7 to 15 digits)."
                }
                hasError = true
            }
        }

        if (password.isBlank()) {
            passError = "Please enter your password."
            hasError = true
        }

        if (hasError) {
            _loginState.update { it.copy(identifierError = identError, passwordError = passError) }
            return
        }

        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, generalError = null) }
            val result = authRepo.login(identifier, password, state.selectedRole)
            _loginState.update { it.copy(isLoading = false) }

            result.fold(
                onSuccess = { user ->
                    // Initialize onboarding state strictly with this authenticated user's profile
                    resetForUser(user)
                    _loginState.value = LoginUiState()

                    when (user.role) {
                        UserRole.PATIENT -> {
                            if (user.patientProfile?.isCompleted == true) {
                                _navigationEvents.emit(AuthNavigationEvent.NavigateToHome)
                            } else {
                                _navigationEvents.emit(AuthNavigationEvent.NavigateToPatientOnboarding)
                            }
                        }
                        UserRole.CAREGIVER -> {
                            _navigationEvents.emit(AuthNavigationEvent.NavigateToCaregiverDashboard)
                        }
                    }
                },
                onFailure = { error ->
                    val message = when (error.message) {
                        "user_not_found" -> "No account found with this email or phone. Please sign up."
                        "invalid_credentials" -> "Incorrect email or password. Please try again."
                        "role_mismatch" -> "This account is registered with a different role."
                        else -> "Unable to log in. Please check your credentials and try again."
                    }
                    _loginState.update { it.copy(generalError = message) }
                }
            )
        }
    }

    // --- Sign Up Actions ---

    fun onSignUpIdentifierChange(value: String) {
        _signUpState.update { it.copy(identifier = value, identifierError = null, generalError = null) }
    }

    fun onSignUpPasswordChange(value: String) {
        _signUpState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    fun onSignUpConfirmPasswordChange(value: String) {
        _signUpState.update { it.copy(confirmPassword = value, confirmPasswordError = null, generalError = null) }
    }

    fun onSignUpRoleChange(role: UserRole) {
        _signUpState.update { it.copy(selectedRole = role, generalError = null) }
    }

    fun toggleSignUpPasswordVisibility() {
        _signUpState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleSignUpConfirmPasswordVisibility() {
        _signUpState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun submitSignUp() {
        val state = _signUpState.value
        val identifier = state.identifier.trim()
        val password = state.password
        val confirmPassword = state.confirmPassword

        var hasError = false
        var identError: String? = null
        var passError: String? = null
        var confirmPassError: String? = null

        if (identifier.isBlank()) {
            identError = "Please enter your email or phone number."
            hasError = true
        } else {
            val type = AuthRepository.getIdentifierType(identifier)
            if (type == IdentifierType.INVALID) {
                identError = if (identifier.contains("@")) {
                    "Please enter a valid email address."
                } else {
                    "Please enter a valid phone number (7 to 15 digits)."
                }
                hasError = true
            }
        }

        if (password.isBlank()) {
            passError = "Please enter a password."
            hasError = true
        }

        if (confirmPassword.isBlank()) {
            confirmPassError = "Please confirm your password."
            hasError = true
        } else if (password != confirmPassword) {
            confirmPassError = "Passwords don't match. Please try again."
            hasError = true
        }

        if (hasError) {
            _signUpState.update {
                it.copy(
                    identifierError = identError,
                    passwordError = passError,
                    confirmPasswordError = confirmPassError
                )
            }
            return
        }

        viewModelScope.launch {
            _signUpState.update { it.copy(isLoading = true, generalError = null) }
            val result = authRepo.register(identifier, password, state.selectedRole)
            _signUpState.update { it.copy(isLoading = false) }

            result.fold(
                onSuccess = { user ->
                    // Brand new user starts with empty/clean onboarding state
                    _patientOnboardingState.value = PatientOnboardingUiState(
                        fullName = user.patientProfile?.fullName ?: "",
                        contactInfo = user.identifier,
                        currentStep = 0
                    )
                    _signUpState.value = SignUpUiState()

                    when (user.role) {
                        UserRole.PATIENT -> {
                            _navigationEvents.emit(AuthNavigationEvent.NavigateToPatientOnboarding)
                        }
                        UserRole.CAREGIVER -> {
                            _navigationEvents.emit(AuthNavigationEvent.NavigateToCaregiverDashboard)
                        }
                    }
                },
                onFailure = { error ->
                    val message = if (error.message == "account_exists") {
                        "An account with this email or phone already exists. Please log in."
                    } else {
                        "That information doesn't look right. Please check and try again."
                    }
                    _signUpState.update { it.copy(generalError = message) }
                }
            )
        }
    }

    // --- Patient Onboarding Actions ---

    fun resetForUser(user: UserAccount?) {
        if (user == null) {
            _patientOnboardingState.value = PatientOnboardingUiState()
            return
        }
        val profile = user.patientProfile
        _patientOnboardingState.value = if (profile != null && profile.isCompleted) {
            PatientOnboardingUiState(
                fullName = profile.fullName,
                age = profile.age,
                dateOfBirth = profile.dateOfBirth,
                gender = profile.gender.ifBlank { "Female" },
                contactInfo = profile.contactInfo.ifBlank { user.identifier },
                diagnosedConditions = profile.diagnosedConditions,
                emergencyContactName = profile.emergencyContactName,
                emergencyContactRelationship = profile.emergencyContactRelationship,
                emergencyContactPhone = profile.emergencyContactPhone,
                language = profile.language.ifBlank { "English" },
                caregiverName = profile.caregiverName,
                healthContext = profile.healthContext,
                currentStep = 0
            )
        } else {
            PatientOnboardingUiState(
                fullName = profile?.fullName ?: user.identifier.substringBefore("@").replace(".", " "),
                contactInfo = user.identifier,
                currentStep = 0
            )
        }
    }

    fun onPatientFullNameChange(value: String) {
        _patientOnboardingState.update { it.copy(fullName = value, errorMessage = null) }
    }

    fun onPatientAgeChange(value: String) {
        _patientOnboardingState.update { it.copy(age = value, errorMessage = null) }
    }

    fun onPatientDobChange(value: String) {
        _patientOnboardingState.update { it.copy(dateOfBirth = value, errorMessage = null) }
    }

    fun onPatientGenderChange(value: String) {
        _patientOnboardingState.update { it.copy(gender = value, errorMessage = null) }
    }

    fun onPatientContactInfoChange(value: String) {
        _patientOnboardingState.update { it.copy(contactInfo = value, errorMessage = null) }
    }

    fun toggleDiagnosedCondition(condition: String) {
        _patientOnboardingState.update { current ->
            val list = current.diagnosedConditions.toMutableList()
            if (condition == "None") {
                list.clear()
                list.add("None")
            } else {
                list.remove("None")
                if (list.contains(condition)) {
                    list.remove(condition)
                } else {
                    list.add(condition)
                }
            }
            current.copy(diagnosedConditions = list)
        }
    }

    fun onPatientEmergencyContactNameChange(value: String) {
        _patientOnboardingState.update { it.copy(emergencyContactName = value, errorMessage = null) }
    }

    fun onPatientEmergencyContactRelationshipChange(value: String) {
        _patientOnboardingState.update { it.copy(emergencyContactRelationship = value, errorMessage = null) }
    }

    fun onPatientEmergencyContactPhoneChange(value: String) {
        _patientOnboardingState.update { it.copy(emergencyContactPhone = value, errorMessage = null) }
    }

    fun onPatientLanguageChange(value: String) {
        _patientOnboardingState.update { it.copy(language = value, errorMessage = null) }
    }

    fun onPatientEmergencyContactChange(value: String) {
        _patientOnboardingState.update { it.copy(emergencyContact = value, errorMessage = null) }
    }

    fun onPatientCaregiverNameChange(value: String) {
        _patientOnboardingState.update { it.copy(caregiverName = value, errorMessage = null) }
    }

    fun onPatientHealthContextChange(value: String) {
        _patientOnboardingState.update { it.copy(healthContext = value, errorMessage = null) }
    }

    fun nextPatientStep() {
        val current = _patientOnboardingState.value.currentStep
        if (current < 3) {
            _patientOnboardingState.update { it.copy(currentStep = current + 1) }
        } else {
            completePatientOnboarding()
        }
    }

    fun previousPatientStep() {
        val current = _patientOnboardingState.value.currentStep
        if (current > 0) {
            _patientOnboardingState.update { it.copy(currentStep = current - 1) }
        }
    }

    fun completePatientOnboarding() {
        val state = _patientOnboardingState.value
        val profile = PatientProfile(
            fullName = state.fullName.trim(),
            age = state.age.trim(),
            dateOfBirth = state.dateOfBirth.trim(),
            gender = state.gender.trim(),
            contactInfo = state.contactInfo.trim(),
            diagnosedConditions = state.diagnosedConditions,
            emergencyContactName = state.emergencyContactName.trim(),
            emergencyContactRelationship = state.emergencyContactRelationship.trim(),
            emergencyContactPhone = state.emergencyContactPhone.trim(),
            language = state.language.trim().ifBlank { "English" },
            emergencyContact = state.emergencyContactPhone.trim().ifBlank { state.emergencyContact.trim() },
            caregiverName = state.caregiverName.trim(),
            healthContext = state.healthContext.trim(),
            isCompleted = true
        )

        viewModelScope.launch {
            _patientOnboardingState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepo.updatePatientProfile(profile)
            _patientOnboardingState.update { it.copy(isLoading = false) }

            result.fold(
                onSuccess = {
                    _navigationEvents.emit(AuthNavigationEvent.NavigateToHome)
                },
                onFailure = {
                    _patientOnboardingState.update {
                        it.copy(errorMessage = "Could not save profile. Please try again.")
                    }
                }
            )
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepo.logout()
            // Reset in-memory state completely
            _patientOnboardingState.value = PatientOnboardingUiState()
            _loginState.value = LoginUiState()
            _signUpState.value = SignUpUiState()
            onComplete()
        }
    }
}
