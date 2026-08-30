package com.memorymoments.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.memorymoments.app.model.UserRole
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroPanel
import com.memorymoments.app.ui.components.RetroTextField
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToSignUp: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToCaregiverDashboard: () -> Unit,
    onNavigateToPatientOnboarding: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val state by viewModel.loginState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is AuthNavigationEvent.NavigateToSignUp -> onNavigateToSignUp()
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
            Spacer(modifier = Modifier.height(dimens.md))

            // App title / branding
            Text(
                text = "COGNIVA",
                style = MmTheme.arcade.title,
                color = colors.reward,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )

            Text(
                text = "Welcome Back",
                style = MmTheme.arcade.titleSmall,
                color = colors.secondary,
                textAlign = TextAlign.Center
            )

            // Role Selector
            RetroPanel(
                borderColor = colors.secondary.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(dimens.sm)) {
                    Text(
                        text = "I AM A:",
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
                            onClick = { viewModel.onLoginRoleChange(UserRole.PATIENT) },
                            modifier = Modifier.weight(1f)
                        )
                        RoleSelectionTab(
                            title = "Caregiver",
                            icon = Icons.Filled.Favorite,
                            isSelected = state.selectedRole == UserRole.CAREGIVER,
                            onClick = { viewModel.onLoginRoleChange(UserRole.CAREGIVER) },
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

            // Login Form Panel
            RetroPanel(
                borderColor = colors.border,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.md)
                ) {
                    Text(
                        text = "ACCOUNT LOGIN",
                        style = MmTheme.arcade.label,
                        color = colors.textMuted
                    )

                    RetroTextField(
                        value = state.identifier,
                        onValueChange = viewModel::onLoginIdentifierChange,
                        label = "Phone number OR Gmail address",
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
                        onValueChange = viewModel::onLoginPasswordChange,
                        label = "Password",
                        required = true,
                        error = state.passwordError,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = colors.textMuted
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = viewModel::toggleLoginPasswordVisibility) {
                                Icon(
                                    imageVector = if (state.isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (state.isPasswordVisible) "Hide password" else "Show password",
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
                            text = "LOG IN",
                            icon = Icons.AutoMirrored.Filled.Login,
                            onClick = viewModel::submitLogin,
                            style = RetroButtonStyle.Primary,
                            minHeight = dimens.playButtonMin,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.sm))

            // Create Account Option
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimens.sm)
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center
                )

                RetroButton(
                    text = "CREATE ACCOUNT",
                    onClick = onNavigateToSignUp,
                    style = RetroButtonStyle.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(dimens.xl))
        }
    }
}

@Composable
fun RoleSelectionTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens

    val backgroundColor = if (isSelected) colors.panelInner else colors.panel
    val borderColor = if (isSelected) colors.reward else colors.border
    val contentColor = if (isSelected) colors.reward else colors.textMuted

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(role = Role.RadioButton, onClick = onClick),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = dimens.md, horizontal = dimens.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(dimens.sm))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp
                ),
                color = contentColor
            )
        }
    }
}
