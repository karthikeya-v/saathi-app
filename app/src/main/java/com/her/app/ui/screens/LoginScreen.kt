package com.her.app.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.her.app.ui.theme.DarkBg
import com.her.app.ui.theme.DarkBorder
import com.her.app.ui.theme.Saffron
import com.her.app.viewmodel.AuthViewModel

@Composable
fun LoginScreen(authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    authViewModel.handleGoogleSignInResult(token)
                }
            } catch (e: ApiException) {
                // handled by ViewModel error state
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Top section: branding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "H.E.R",
                fontSize = 80.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                color = Saffron,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Virtual connections.\nReal feelings.",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Overlapping emoji circles decorative
            Box(
                modifier = Modifier.height(72.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EmojiCircle("🌿", offset = 0)
                    EmojiCircle("✨", offset = -16)
                    EmojiCircle("♟️", offset = -32)
                }
            }
        }

        // Bottom card
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color(0xFF16161E))
                .navigationBarsPadding()
                .padding(28.dp)
        ) {
            Column {
                Text(
                    "Get started",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Sign in to meet your matches",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Saffron)
                    }
                } else {
                    Button(
                        onClick = {
                            val intent = authViewModel.getGoogleSignInIntent(context)
                            launcher.launch(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("G", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                "  Continue with Google",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                error?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(it, color = Color(0xFFEF4444), fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Your conversations are private.\nWe never share your data.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.3f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun EmojiCircle(emoji: String, offset: Int) {
    Box(
        modifier = Modifier
            .offset(x = offset.dp)
            .size(64.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E1E2A))
            .border(1.5.dp, DarkBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 26.sp)
    }
}
