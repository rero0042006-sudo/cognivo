package com.memorymoments.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.memorymoments.app.model.UiMode
import com.memorymoments.app.navigation.AppNavHost
import com.memorymoments.app.repository.GameSettingsRepository
import com.memorymoments.app.ui.theme.MemoryMomentsTheme
import com.memorymoments.app.ui.theme.MmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsRepo = remember { GameSettingsRepository(applicationContext) }
            val uiMode by settingsRepo.uiMode.collectAsStateWithLifecycle(initialValue = UiMode.DEFAULT)
            val appLanguage by settingsRepo.appLanguage.collectAsStateWithLifecycle(initialValue = "en")

            val localizedContext = remember(appLanguage) {
                com.memorymoments.app.utils.AppLanguageManager.applyLocale(applicationContext, appLanguage)
            }

            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalContext provides localizedContext
            ) {
                MemoryMomentsTheme(uiMode = uiMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MmTheme.colors.background
                    ) {
                        AppNavHost()
                    }
                }
            }
        }
    }
}
