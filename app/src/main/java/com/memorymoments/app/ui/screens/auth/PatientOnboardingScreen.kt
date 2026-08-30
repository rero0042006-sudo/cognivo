package com.memorymoments.app.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroPanel
import com.memorymoments.app.ui.components.RetroTextField
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun PatientOnboardingScreen(
    viewModel: AuthViewModel,
    onFinish: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val state by viewModel.patientOnboardingState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(currentUser?.id) {
        if (currentUser != null) {
            viewModel.resetForUser(currentUser)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            if (event is AuthNavigationEvent.NavigateToHome) {
                onFinish()
            }
        }
    }

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.screenHorizontal, vertical = dimens.screenVertical),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimens.md)
            ) {
                ArcadeTopBar(
                    title = "PATIENT ONBOARDING",
                    onBack = if (state.currentStep > 0) {
                        { viewModel.previousPatientStep() }
                    } else null
                )

                Text(
                    text = "STEP ${state.currentStep + 1} OF 4",
                    style = MmTheme.arcade.label,
                    color = colors.secondary,
                    textAlign = TextAlign.Center
                )

                AnimatedContent(
                    targetState = state.currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "patientStepTransition"
                ) { step ->
                    when (step) {
                        0 -> WelcomeStepContent(colors = colors, dimens = dimens)
                        1 -> BasicDetailsStepContent(
                            state = state,
                            viewModel = viewModel,
                            colors = colors,
                            dimens = dimens
                        )
                        2 -> CaregiverContactStepContent(
                            state = state,
                            viewModel = viewModel,
                            colors = colors,
                            dimens = dimens
                        )
                        3 -> HealthContextStepContent(
                            state = state,
                            viewModel = viewModel,
                            colors = colors,
                            dimens = dimens
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.xl))

            // Navigation Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimens.md)
            ) {
                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.reward)
                    }
                } else if (state.currentStep < 3) {
                    RetroButton(
                        text = if (state.currentStep == 0) "LET'S SET UP YOUR PROFILE" else "NEXT STEP",
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        onClick = viewModel::nextPatientStep,
                        style = RetroButtonStyle.Primary,
                        minHeight = dimens.playButtonMin,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.currentStep == 0) {
                        RetroButton(
                            text = "SKIP TO HOME",
                            onClick = viewModel::completePatientOnboarding,
                            style = RetroButtonStyle.Ghost,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    RetroButton(
                        text = "COMPLETE & ENTER COGNIVA",
                        icon = Icons.Filled.Check,
                        onClick = viewModel::completePatientOnboarding,
                        style = RetroButtonStyle.Primary,
                        minHeight = dimens.playButtonMin,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(dimens.md))
            }
        }
    }
}

@Composable
private fun WelcomeStepContent(
    colors: com.memorymoments.app.ui.theme.MmColors,
    dimens: com.memorymoments.app.ui.theme.MmDimens
) {
    RetroPanel(borderColor = colors.reward.copy(alpha = 0.85f)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.md)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = colors.reward,
                modifier = Modifier.size(56.dp)
            )

            Text(
                text = "WELCOME TO COGNIVA",
                style = MmTheme.arcade.titleSmall,
                color = colors.reward,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )

            Text(
                text = "A friendly, cognitive-training and memory companion designed to help you stay sharp and celebrate moments with your loved ones.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.text,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimens.xs))

            Text(
                text = "Let's complete your basic profile first. You can always access your games and exercises from the home screen whenever you are ready.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BasicDetailsStepContent(
    state: PatientOnboardingUiState,
    viewModel: AuthViewModel,
    colors: com.memorymoments.app.ui.theme.MmColors,
    dimens: com.memorymoments.app.ui.theme.MmDimens
) {
    val conditionOptions = remember {
        listOf(
            "Alzheimer's Disease",
            "Dementia",
            "Mild Cognitive Impairment",
            "Parkinson's Disease",
            "Stroke",
            "Other",
            "None"
        )
    }

    RetroPanel(borderColor = colors.secondary) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimens.md)
        ) {
            Text(
                text = "ABOUT YOU",
                style = MmTheme.arcade.titleSmall,
                color = colors.secondary
            )

            Text(
                text = "Please enter your basic information and any diagnosed conditions.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted
            )

            RetroTextField(
                value = state.fullName,
                onValueChange = viewModel::onPatientFullNameChange,
                label = "Full Name",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = colors.textMuted
                    )
                }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(dimens.sm)) {
                RetroTextField(
                    value = state.age,
                    onValueChange = viewModel::onPatientAgeChange,
                    label = "Age (e.g. 74)",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next
                )

                RetroTextField(
                    value = state.dateOfBirth,
                    onValueChange = viewModel::onPatientDobChange,
                    label = "DOB (YYYY-MM-DD)",
                    modifier = Modifier.weight(1.4f),
                    imeAction = ImeAction.Next
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(dimens.sm)) {
                RetroTextField(
                    value = state.gender,
                    onValueChange = viewModel::onPatientGenderChange,
                    label = "Gender (e.g. Female)",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next
                )

                RetroTextField(
                    value = state.language,
                    onValueChange = viewModel::onPatientLanguageChange,
                    label = "Language",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next
                )
            }

            RetroTextField(
                value = state.contactInfo,
                onValueChange = viewModel::onPatientContactInfoChange,
                label = "Contact Phone or Email",
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "DIAGNOSED CONDITIONS / DISEASES:",
                style = MmTheme.arcade.label,
                color = colors.secondary
            )

            Text(
                text = "Select any conditions that apply (multiple selections supported):",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    listOf("Alzheimer's Disease", "Dementia"),
                    listOf("Mild Cognitive Impairment", "Parkinson's Disease"),
                    listOf("Stroke", "Other"),
                    listOf("None")
                ).forEach { rowList ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowList.forEach { condition ->
                            val isSelected = state.diagnosedConditions.contains(condition)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.toggleDiagnosedCondition(condition) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) colors.secondary else colors.panelInner,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) colors.secondary else colors.border
                                )
                            ) {
                                Text(
                                    text = condition,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isSelected) colors.panel else colors.text,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        if (rowList.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CaregiverContactStepContent(
    state: PatientOnboardingUiState,
    viewModel: AuthViewModel,
    colors: com.memorymoments.app.ui.theme.MmColors,
    dimens: com.memorymoments.app.ui.theme.MmDimens
) {
    RetroPanel(borderColor = colors.primary) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimens.md)
        ) {
            Text(
                text = "EMERGENCY CONTACT & SUPPORT",
                style = MmTheme.arcade.titleSmall,
                color = colors.primary
            )

            Text(
                text = "Add your emergency contact details so help is always accessible.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted
            )

            RetroTextField(
                value = state.emergencyContactName,
                onValueChange = viewModel::onPatientEmergencyContactNameChange,
                label = "Emergency Contact Name",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = colors.textMuted
                    )
                }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(dimens.sm)) {
                RetroTextField(
                    value = state.emergencyContactRelationship,
                    onValueChange = viewModel::onPatientEmergencyContactRelationshipChange,
                    label = "Relationship (e.g. Daughter)",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next
                )

                RetroTextField(
                    value = state.emergencyContactPhone,
                    onValueChange = viewModel::onPatientEmergencyContactPhoneChange,
                    label = "Phone Number",
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ContactPhone,
                            contentDescription = null,
                            tint = colors.textMuted
                        )
                    }
                )
            }

            RetroTextField(
                value = state.caregiverName,
                onValueChange = viewModel::onPatientCaregiverNameChange,
                label = "Caregiver Name / Link Code",
                imeAction = ImeAction.Done,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = colors.textMuted
                    )
                }
            )
        }
    }
}

@Composable
private fun HealthContextStepContent(
    state: PatientOnboardingUiState,
    viewModel: AuthViewModel,
    colors: com.memorymoments.app.ui.theme.MmColors,
    dimens: com.memorymoments.app.ui.theme.MmDimens
) {
    RetroPanel(borderColor = colors.reward) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimens.md)
        ) {
            Text(
                text = "HEALTH & PREFERENCES",
                style = MmTheme.arcade.titleSmall,
                color = colors.reward
            )

            Text(
                text = "Optional: Add any notes or memory preferences that help you feel comfortable.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted
            )

            RetroTextField(
                value = state.healthContext,
                onValueChange = viewModel::onPatientHealthContextChange,
                label = "Notes / Preferences (Optional)",
                singleLine = false,
                minLines = 3,
                imeAction = ImeAction.Done,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.HealthAndSafety,
                        contentDescription = null,
                        tint = colors.textMuted
                    )
                }
            )
        }
    }
}
