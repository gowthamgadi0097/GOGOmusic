package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.SessionManager
import com.example.data.SoundVaultRepository
import com.example.player.MusicPlayerController
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.vault.MainVaultScreen
import com.example.ui.vault.MainVaultViewModel

class MainActivity : ComponentActivity() {

    private lateinit var playerController: MusicPlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val sessionManager = SessionManager(applicationContext)
        val repository = SoundVaultRepository(
            userDao = database.userDao(),
            songDao = database.songDao(),
            playlistDao = database.playlistDao()
        )

        playerController = MusicPlayerController(applicationContext)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    val currentUserId by sessionManager.currentUserId.collectAsStateWithLifecycle()

                    Crossfade(
                        targetState = currentUserId != null,
                        animationSpec = tween(400),
                        label = "auth_vault_crossfade"
                    ) { isAuthenticated ->
                        if (isAuthenticated) {
                            val mainVaultViewModel = remember(currentUserId) {
                                MainVaultViewModel(
                                    repository = repository,
                                    sessionManager = sessionManager,
                                    playerController = playerController
                                )
                            }
                            MainVaultScreen(viewModel = mainVaultViewModel)
                        } else {
                            val authViewModel = remember {
                                AuthViewModel(
                                    repository = repository,
                                    sessionManager = sessionManager
                                )
                            }
                            AuthScreen(viewModel = authViewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            playerController.release()
        }
    }
}
