package com.her.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.her.app.ui.screens.ChatScreen
import com.her.app.ui.screens.HomeScreen
import com.her.app.ui.screens.PersonalityPickerScreen
import com.her.app.ui.theme.HERTheme
import com.her.app.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HERTheme {
                HERApp()
            }
        }
    }
}

@Composable
fun HERApp() {
    val navController = rememberNavController()
    val chatViewModel: ChatViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onPersonalitySelected = { personality ->
                    chatViewModel.selectPersonality(personality)
                    navController.navigate("chat")
                }
            )
        }

        composable("chat") {
            ChatScreen(
                viewModel = chatViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onSwitchPersonality = {
                    navController.navigate("picker")
                }
            )
        }

        composable("picker") {
            PersonalityPickerScreen(
                onPersonalitySelected = { personality ->
                    chatViewModel.selectPersonality(personality)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
