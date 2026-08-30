package com.memorymoments.app.ui.screens.auth

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.memorymoments.app.model.UserRole
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroPanel
import com.memorymoments.app.ui.components.RetroTextField
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onNavigateBackToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToCaregiverDashboard: () -> Unit,
    onNavigateToPatientOnboarding: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val state by viewModel.signUpState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is AuthNavigationEvent.NavigateToLogin -> onNavigateBackToLogin()
                is AuthNavigationEvent.NavigateToHome -> onNavigateToHome()
                is AuthNavigationEvent.NavigateToCaregiverDashboard -> onNavigateToCaregiverDashboard()
                is AuthNavigationEvent.NavigateToPatientOnboarding -> onNavigateToPatientOnboarding()
                else -> {}
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
            verticalArrangement = Arrangement.spacedBy(dimens.lg)
        ) {
            ArcadeTopBar(
                title = "CREATE ACCOUNT",
                onBack = onNavigateBackToLogin
            )

            // Header info
            Text(
                text = "Join Cogniva",
                style = MmTheme.arcade.titleSmall,
                color = colors.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )

            Text(
                text = "Create your account to save your profile and progress safely on this device.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Role Selector
            RetroPanel(
                borderColor = colors.secondary.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(dimens.sm)) {
                    Text(
                        text = "REGISTER AS:",
                        style = MmTheme.arcade.label,
                        color = colors.textMuted
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.md)
                    ) {
                        RoleSelectionTab(
                            title = "Patient",
                            icon = Icons.Filled.Person,
                            isSelected = state.selectedRole == UserRole.PATIENT,
                            onClick = { viewModel.onSignUpRoleChange(UserRole.PATIENT) },
                            modifier = Modifier.weight(1f)
                        )
                        RoleSelectionTab(
                            title = "Caregiver",
                            icon = Icons.Filled.Favorite,
                            isSelected = state.selectedRole == UserRole.CAREGIVER,
                            onClick = { viewModel.onSignUpRoleChange(UserRole.CAREGIVER) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // General Error Banner if present
            if (state.generalError != null) {
                RetroPanel(
                    borderColor = colors.error,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Error",
                            tint = colors.error,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = state.generalError ?: "",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.error
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Registration Form
            RetroPanel(
                borderColor = colors.border,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.md)
                ) {
                    Text(
                        text = "ACCOUNT CREDENTIALS",
                        style = MmTheme.arcade.label,
                        color = colors.textMuted
                    )

                    RetroTextField(
                        value = state.identifier,
                        onValueChange = viewModel::onSignUpIdentifierChange,
                        label = "Gmail Address OR 10-digit Phone",
                        required = true,
                        error = state.identifierError,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = colors.textMuted
                            )
                        }
                    )

                    RetroTextField(
                        value = state.password,
                        onValueChange = viewModel::onSignUpPasswordChange,
                        label = "Password",
                        required = true,
                        error = state.passwordError,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next,
                        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = colors.textMuted
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = viewModel::toggleSignUpPasswordVisibility) {
                                Icon(
                                    imageVector = if (state.isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (state.isPasswordVisible) "Hide password" else "Show password",
                                    tint = colors.textMuted
                                )
                            }
                        }
                    )

                    RetroTextField(
                        value = state.confirmPassword,
                        onValueChange = viewModel::onSignUpConfirmPasswordChange,
                        label = "Confirm Password",
                        required = true,
                        error = state.confirmPasswordError,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        visualTransformation = if (state.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = colors.textMuted
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = viewModel::toggleSignUpConfirmPasswordVisibility) {
                                Icon(
                                    imageVector = if (state.isConfirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (state.isConfirmPasswordVisible) "Hide password" else "Show password",
                                    tint = colors.textMuted
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(dimens.xs))

                    if (state.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.reward)
                        }
                    } else {
                        RetroButton(
                            text = "CREATE ACCOUNT",
                            icon = Icons.Filled.PersonAdd,
                            onClick = viewModel::submitSignUp,
                            style = RetroButtonStyle.Primary,
                            minHeight = dimens.playButtonMin,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.sm))

            // Log In link
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimens.sm)
            ) {
                Text(
                    text = "Already registered?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center
                )

                RetroButton(
                    text = "LOG IN TO EXISTING ACCOUNT",
                    onClick = onNavigateBackToLogin,
                    style = RetroButtonStyle.Ghost,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(dimens.xl))
        }
    }
}
