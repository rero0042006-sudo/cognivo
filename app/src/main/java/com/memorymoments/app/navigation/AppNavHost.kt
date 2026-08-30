package com.memorymoments.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.memorymoments.app.model.UserRole
import com.memorymoments.app.repository.AuthRepository
import com.memorymoments.app.repository.GameSettingsRepository
import com.memorymoments.app.ui.screens.auth.AuthViewModel
import com.memorymoments.app.ui.screens.auth.LoginScreen
import com.memorymoments.app.ui.screens.auth.PatientOnboardingScreen
import com.memorymoments.app.ui.screens.auth.SignUpScreen
import com.memorymoments.app.ui.screens.caregiver.CaregiverChatScreen
import com.memorymoments.app.ui.screens.caregiver.CaregiverDashboardScreen
import com.memorymoments.app.ui.screens.distractor.DistractorLabScreen
import com.memorymoments.app.ui.screens.family.FamilyMemberFormScreen
import com.memorymoments.app.ui.screens.family.FamilySetupScreen
import com.memorymoments.app.ui.screens.game.GameScreen
import com.memorymoments.app.ui.screens.game.GameSessionResult
import com.memorymoments.app.ui.screens.gamesetup.GameSelectionScreen
import com.memorymoments.app.ui.screens.gamesetup.GameSetupScreen
import com.memorymoments.app.ui.screens.home.HomeScreen
import com.memorymoments.app.ui.screens.memories.MemoriesScreen
import com.memorymoments.app.ui.screens.memories.MemoryFormScreen
import com.memorymoments.app.ui.screens.memorytalk.MemoryTalkScreen
import com.memorymoments.app.ui.screens.music.MemoryMusicScreen
import com.memorymoments.app.ui.screens.music.MusicListScreen
import com.memorymoments.app.ui.screens.music.SongFormScreen
import com.memorymoments.app.ui.screens.musicgame.MusicGameScreen
import com.memorymoments.app.ui.screens.onboarding.OnboardingScreen
import com.memorymoments.app.ui.screens.places.PlaceFormScreen
import com.memorymoments.app.ui.screens.places.PlacesListScreen
import com.memorymoments.app.ui.screens.placesgame.PlacesGameScreen
import com.memorymoments.app.ui.screens.portal.PatientPortalScreen
import com.memorymoments.app.ui.screens.results.ResultsScreen
import com.memorymoments.app.ui.screens.settings.SettingsScreen
import com.memorymoments.app.ui.screens.timeline.LifeEventFormScreen
import com.memorymoments.app.ui.screens.timeline.LifeTimelineScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val settingsRepo = remember { GameSettingsRepository(context) }
    val authRepo = remember { AuthRepository(context) }
    val currentUserId by authRepo.currentUserId.collectAsStateWithLifecycle(initialValue = null)
    val currentUserRole by authRepo.currentUserRole.collectAsStateWithLifecycle(initialValue = null)
    val currentUser by authRepo.currentUser.collectAsStateWithLifecycle(initialValue = null)
    val coroutineScope = rememberCoroutineScope()

    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = if (!currentUserId.isNullOrBlank()) {
            when {
                currentUserRole == UserRole.CAREGIVER -> Routes.CAREGIVER
                currentUser?.patientProfile?.isCompleted == false -> Routes.PATIENT_ONBOARDING
                else -> Routes.HOME
            }
        } else {
            Routes.LOGIN
        }
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = { navController.navigate(Routes.SIGN_UP) },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToCaregiverDashboard = {
                    navController.navigate(Routes.CAREGIVER) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToPatientOnboarding = {
                    navController.navigate(Routes.PATIENT_ONBOARDING) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.SIGN_UP) {
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateBackToLogin = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToCaregiverDashboard = {
                    navController.navigate(Routes.CAREGIVER) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToPatientOnboarding = {
                    navController.navigate(Routes.PATIENT_ONBOARDING) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.PATIENT_ONBOARDING) {
            PatientOnboardingScreen(
                viewModel = authViewModel,
                onFinish = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PATIENT_ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    coroutineScope.launch {
                        settingsRepo.setHasSeenOnboarding(true)
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                isPreview = false,
                onPlay = { navController.navigate(Routes.GAME_SELECTION) },
                onFamily = { navController.navigate(Routes.FAMILY) },
                onPlaces = { navController.navigate(Routes.PLACES) },
                onMemories = { navController.navigate(Routes.MEMORIES) },
                onMusic = { navController.navigate(Routes.MUSIC) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onCaregiver = { navController.navigate(Routes.CAREGIVER) },
                onTryDemo = { navController.navigate(Routes.gameSetup(demo = true)) },
                onNavigateRoute = { route -> navController.navigate(route) }
            )
        }
        composable(
            route = Routes.HOME_ROUTE,
            arguments = listOf(
                navArgument(Routes.PREVIEW_ARG) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { entry ->
            val isPreview = entry.arguments?.getBoolean(Routes.PREVIEW_ARG) == true
            if (isPreview) {
                HomeScreen(
                    isPreview = true,
                    onExitPreview = { navController.popBackStack() },
                    onPlay = { navController.navigate(Routes.GAME_SELECTION) },
                    onFamily = { navController.navigate(Routes.FAMILY) },
                    onPlaces = { navController.navigate(Routes.PLACES) },
                    onMemories = { navController.navigate(Routes.MEMORIES) },
                    onMusic = { navController.navigate(Routes.MUSIC) },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                    onCaregiver = { navController.navigate(Routes.CAREGIVER) },
                    onTryDemo = { navController.navigate(Routes.gameSetup(demo = true)) },
                    onNavigateRoute = { route -> navController.navigate(route) }
                )
            } else {
                PatientPortalScreen(
                    onPlay = { navController.navigate(Routes.GAME_SELECTION) },
                    onFamily = { navController.navigate(Routes.FAMILY) },
                    onPlaces = { navController.navigate(Routes.PLACES) },
                    onMemories = { navController.navigate(Routes.MEMORIES) },
                    onMusic = { navController.navigate(Routes.MUSIC) },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                    onCaregiver = { navController.navigate(Routes.CAREGIVER_CHAT) },
                    onTryDemo = { navController.navigate(Routes.gameSetup(demo = true)) },
                    onNavigateRoute = { route -> navController.navigate(route) },
                    onLogout = {
                        authViewModel.logout {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
        composable(Routes.CAREGIVER) {
            CaregiverDashboardScreen(
                onBack = { navController.popBackStack() },
                onPeople = { navController.navigate(Routes.FAMILY) },
                onMemories = { navController.navigate(Routes.MEMORIES) },
                onPlaces = { navController.navigate(Routes.PLACES) },
                onMusic = { navController.navigate(Routes.MUSIC) },
                onTimeline = { navController.navigate(Routes.TIMELINE) },
                onPreview = { navController.navigate(Routes.home(preview = true)) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenChat = { navController.navigate(Routes.CAREGIVER_CHAT) }
            )
        }
        composable(Routes.CAREGIVER_CHAT) {
            CaregiverChatScreen(
                currentViewerRole = UserRole.CAREGIVER,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.MEMORIES) {
            MemoriesScreen(
                onBack = { navController.popBackStack() },
                onAddMemory = { navController.navigate(Routes.ADD_MEMORY) },
                onEditMemory = { id -> navController.navigate(Routes.editMemory(id)) },
                onMemoryTalk = { id -> navController.navigate(Routes.memoryTalk(id)) },
                onOpenTimeline = { navController.navigate(Routes.TIMELINE) }
            )
        }
        composable(Routes.ADD_MEMORY) {
            MemoryFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.EDIT_MEMORY,
            arguments = listOf(
                navArgument(Routes.MEMBER_ID_ARG) { type = NavType.StringType }
            )
        ) {
            MemoryFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(Routes.TIMELINE) {
            LifeTimelineScreen(
                onBack = { navController.popBackStack() },
                onAddEvent = { navController.navigate(Routes.ADD_LIFE_EVENT) },
                onEditEvent = { id -> navController.navigate(Routes.editLifeEvent(id)) },
                onOpenPerson = { id -> navController.navigate(Routes.editFamily(id)) },
                onOpenPlace = { id -> navController.navigate(Routes.editPlace(id)) },
                onOpenMemory = { id -> navController.navigate(Routes.memoryTalk(id)) }
            )
        }
        composable(Routes.ADD_LIFE_EVENT) {
            LifeEventFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.EDIT_LIFE_EVENT,
            arguments = listOf(
                navArgument(Routes.MEMBER_ID_ARG) { type = NavType.StringType }
            )
        ) {
            LifeEventFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(Routes.MEMORY_TALK) {
            MemoryTalkScreen(
                onBack = { navController.popBackStack() },
                onAddMemory = { navController.navigate(Routes.MEMORIES) }
            )
        }
        composable(
            route = Routes.MEMORY_TALK_ROUTE,
            arguments = listOf(
                navArgument(Routes.MEMBER_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            MemoryTalkScreen(
                onBack = { navController.popBackStack() },
                onAddMemory = { navController.navigate(Routes.MEMORIES) }
            )
        }
        composable(Routes.MUSIC) {
            MusicListScreen(
                onBack = { navController.popBackStack() },
                onAddSong = { navController.navigate(Routes.ADD_SONG) },
                onEditSong = { id -> navController.navigate(Routes.editSong(id)) },
                onMemoryMusic = { navController.navigate(Routes.MEMORY_MUSIC) }
            )
        }
        composable(Routes.ADD_SONG) {
            SongFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.EDIT_SONG,
            arguments = listOf(
                navArgument(Routes.MEMBER_ID_ARG) { type = NavType.StringType }
            )
        ) {
            SongFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(Routes.MEMORY_MUSIC) {
            MemoryMusicScreen(
                onBack = { navController.popBackStack() },
                onViewMemories = {
                    navController.navigate(Routes.MEMORIES)
                }
            )
        }
        composable(Routes.GAME_SELECTION) {
            GameSelectionScreen(
                onBack = { navController.popBackStack() },
                onSelectWhosWho = { navController.navigate(Routes.gameSetup(demo = false)) },
                onSelectWhereWasIt = { navController.navigate(Routes.placesGame(style = "NORMAL", demo = false)) },
                onSelectNameThatTune = { navController.navigate(Routes.musicGame(style = "NORMAL", demo = false)) }
            )
        }
        composable(Routes.FAMILY) {
            FamilySetupScreen(
                onBack = { navController.popBackStack() },
                onAddMember = { navController.navigate(Routes.ADD_FAMILY) },
                onEditMember = { id -> navController.navigate(Routes.editFamily(id)) }
            )
        }
        composable(Routes.ADD_FAMILY) {
            FamilyMemberFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.EDIT_FAMILY,
            arguments = listOf(
                navArgument(Routes.MEMBER_ID_ARG) { type = NavType.StringType }
            )
        ) {
            FamilyMemberFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(Routes.PLACES) {
            PlacesListScreen(
                onBack = { navController.popBackStack() },
                onAddPlace = { navController.navigate(Routes.ADD_PLACE) },
                onEditPlace = { id -> navController.navigate(Routes.editPlace(id)) }
            )
        }
        composable(Routes.ADD_PLACE) {
            PlaceFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.EDIT_PLACE,
            arguments = listOf(
                navArgument(Routes.MEMBER_ID_ARG) { type = NavType.StringType }
            )
        ) {
            PlaceFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.GAME_SETUP_ROUTE,
            arguments = listOf(
                navArgument(Routes.DEMO_ARG) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { entry ->
            val isDemo = entry.arguments?.getBoolean(Routes.DEMO_ARG) == true
            GameSetupScreen(
                isDemo = isDemo,
                onBack = { navController.popBackStack() },
                onStartGame = { style ->
                    navController.navigate(Routes.distractorLab(style.name, isDemo))
                }
            )
        }
        composable(
            route = Routes.DISTRACTOR_LAB_ROUTE,
            arguments = listOf(
                navArgument(Routes.STYLE_ARG) {
                    type = NavType.StringType
                    defaultValue = "NORMAL"
                },
                navArgument(Routes.DEMO_ARG) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { entry ->
            val style = entry.arguments?.getString(Routes.STYLE_ARG) ?: "NORMAL"
            val isDemo = entry.arguments?.getBoolean(Routes.DEMO_ARG) == true
            DistractorLabScreen(
                onBack = { navController.popBackStack() },
                onReady = {
                    navController.navigate(Routes.game(style, isDemo)) {
                        popUpTo(Routes.GAME_SETUP_ROUTE) { inclusive = false }
                    }
                }
            )
        }
        composable(
            route = Routes.GAME_ROUTE,
            arguments = listOf(
                navArgument(Routes.STYLE_ARG) {
                    type = NavType.StringType
                    defaultValue = "NORMAL"
                },
                navArgument(Routes.DEMO_ARG) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            GameScreen(
                onBack = { navController.popBackStack() },
                onFinish = { result ->
                    navController.navigate(
                        Routes.results(
                            stars = result.starsEarned,
                            xp = result.xpEarned,
                            combo = result.bestCombo,
                            total = result.totalRounds,
                            correct = result.correctAnswers
                        )
                    ) {
                        popUpTo(Routes.GAME_ROUTE) { inclusive = true }
                    }
                },
                onAddFamily = {
                    navController.navigate(Routes.FAMILY) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
        composable(
            route = Routes.PLACES_GAME_ROUTE,
            arguments = listOf(
                navArgument(Routes.STYLE_ARG) {
                    type = NavType.StringType
                    defaultValue = "NORMAL"
                },
                navArgument(Routes.DEMO_ARG) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            PlacesGameScreen(
                onBack = { navController.popBackStack() },
                onFinish = { result ->
                    navController.navigate(
                        Routes.results(
                            stars = result.starsEarned,
                            xp = result.xpEarned,
                            combo = result.bestCombo,
                            total = result.totalRounds,
                            correct = result.correctAnswers
                        )
                    ) {
                        popUpTo(Routes.PLACES_GAME_ROUTE) { inclusive = true }
                    }
                },
                onAddPlaces = {
                    navController.navigate(Routes.PLACES) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
        composable(
            route = Routes.MUSIC_GAME_ROUTE,
            arguments = listOf(
                navArgument(Routes.STYLE_ARG) {
                    type = NavType.StringType
                    defaultValue = "NORMAL"
                },
                navArgument(Routes.DEMO_ARG) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            MusicGameScreen(
                onBack = { navController.popBackStack() },
                onFinish = { result ->
                    navController.navigate(
                        Routes.results(
                            stars = result.starsEarned,
                            xp = result.xpEarned,
                            combo = result.bestCombo,
                            total = result.totalRounds,
                            correct = result.correctAnswers
                        )
                    ) {
                        popUpTo(Routes.MUSIC_GAME_ROUTE) { inclusive = true }
                    }
                },
                onAddSongs = {
                    navController.navigate(Routes.MUSIC) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
        composable(
            route = Routes.RESULTS_ROUTE,
            arguments = listOf(
                navArgument(Routes.STARS_ARG) {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument(Routes.XP_ARG) {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument(Routes.COMBO_ARG) {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument(Routes.TOTAL_ARG) {
                    type = NavType.IntType
                    defaultValue = 10
                },
                navArgument(Routes.CORRECT_ARG) {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { entry ->
            val stars = entry.arguments?.getInt(Routes.STARS_ARG) ?: 0
            val xp = entry.arguments?.getInt(Routes.XP_ARG) ?: 0
            val combo = entry.arguments?.getInt(Routes.COMBO_ARG) ?: 0
            val total = entry.arguments?.getInt(Routes.TOTAL_ARG) ?: 10
            val correct = entry.arguments?.getInt(Routes.CORRECT_ARG) ?: 0

            ResultsScreen(
                stars = stars,
                xp = xp,
                bestCombo = combo,
                totalRounds = total,
                correctAnswers = correct,
                onHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNextActivity = {
                    navController.navigate(Routes.GAME_SELECTION) {
                        popUpTo(Routes.HOME)
                    }
                },
                onPlayAgain = {
                    navController.navigate(Routes.GAME_SELECTION) {
                        popUpTo(Routes.HOME)
                    }
                },
                onViewFamily = {
                    navController.navigate(Routes.FAMILY) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onFamily = { navController.navigate(Routes.FAMILY) },
                onCaregiver = { navController.navigate(Routes.CAREGIVER) },
                onDistractors = { navController.navigate(Routes.distractorLab("NORMAL", false)) },
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
