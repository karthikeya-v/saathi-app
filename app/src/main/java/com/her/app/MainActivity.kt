package com.her.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.her.app.data.FirestoreRepository
import com.her.app.data.UserProfile
import com.her.app.ui.screens.ChatScreen
import com.her.app.ui.screens.LoginScreen
import com.her.app.ui.screens.OnboardingScreen
import com.her.app.ui.screens.PersonalityPickerScreen
import com.her.app.ui.screens.SwipeScreen
import com.her.app.ui.theme.DarkBg
import com.her.app.ui.theme.HERTheme
import com.her.app.ui.theme.Saffron
import com.her.app.viewmodel.AuthViewModel
import com.her.app.viewmodel.ChatViewModel
import com.her.app.viewmodel.OnboardingViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HERTheme {
                HERApp(authViewModel)
            }
        }
    }
}

@Composable
fun HERApp(authViewModel: AuthViewModel) {
    val user by authViewModel.user.collectAsState()

    if (user == null) {
        LoginScreen(authViewModel = authViewModel)
        return
    }

    // Check whether the user has completed onboarding
    var profileState by remember { mutableStateOf<UserProfile?>(null) }
    var profileLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user?.uid) {
        profileLoading = true
        try {
            profileState = FirestoreRepository().loadProfile()
        } catch (_: Exception) {
            profileState = null
        }
        profileLoading = false
    }

    if (profileLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(DarkBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Saffron)
        }
        return
    }

    val navController = rememberNavController()
    val chatViewModel: ChatViewModel = viewModel()
    val onboardingViewModel: OnboardingViewModel = viewModel()

    val startDest = if (profileState != null) "swipe" else "onboarding"

    // Expose loaded profile to nav graph
    val loadedProfile = profileState ?: UserProfile()

    NavHost(navController = navController, startDestination = startDest) {

        composable("onboarding") {
            val isDone by onboardingViewModel.isDone.collectAsState()
            LaunchedEffect(isDone) {
                if (isDone) {
                    navController.navigate("swipe") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            }
            OnboardingScreen(viewModel = onboardingViewModel)
        }

        composable("swipe") {
            // Use the onboarding profile if just completed, otherwise use loaded
            val onboardingName by onboardingViewModel.name.collectAsState()
            val profile = if (onboardingName.isNotBlank()) {
                UserProfile(
                    name = onboardingViewModel.name.value,
                    age = onboardingViewModel.age.value,
                    gender = onboardingViewModel.gender.value,
                    interestedIn = onboardingViewModel.interestedIn.value,
                    vibe = onboardingViewModel.vibe.value
                )
            } else loadedProfile

            SwipeScreen(
                userProfile = profile,
                onPersonalitySelected = { personality ->
                    chatViewModel.selectPersonality(personality)
                    navController.navigate("chat")
                }
            )
        }

        composable("chat") {
            ChatScreen(
                viewModel = chatViewModel,
                onBack = { navController.popBackStack() },
                onSwitchPersonality = { navController.navigate("picker") }
            )
        }

        composable("picker") {
            PersonalityPickerScreen(
                onPersonalitySelected = { personality ->
                    chatViewModel.selectPersonality(personality)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
