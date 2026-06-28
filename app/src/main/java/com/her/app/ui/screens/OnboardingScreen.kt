package com.her.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.her.app.ui.theme.DarkBg
import com.her.app.ui.theme.DarkBorder
import com.her.app.ui.theme.Saffron
import com.her.app.ui.theme.TextMuted
import com.her.app.ui.theme.TextSecondary
import com.her.app.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel) {
    val step by viewModel.currentStep.collectAsState()
    val name by viewModel.name.collectAsState()
    val age by viewModel.age.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val interestedIn by viewModel.interestedIn.collectAsState()
    val vibe by viewModel.vibe.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val error by viewModel.error.collectAsState()

    val canContinue = when (step) {
        0 -> name.isNotBlank()
        1 -> true
        2 -> gender.isNotBlank()
        3 -> interestedIn.isNotBlank()
        4 -> vibe.isNotBlank()
        else -> false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
        ) {
            // Progress bar
            Spacer(modifier = Modifier.height(16.dp))
            ProgressBar(step = step, total = 5)
            Spacer(modifier = Modifier.height(8.dp))

            // Back button (hidden on step 0)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (step > 0) {
                    IconButton(onClick = { viewModel.back() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Step content with animation
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()) togetherWith
                                (slideOutHorizontally { it } + fadeOut())
                    }
                },
                modifier = Modifier.weight(1f),
                label = "step"
            ) { currentStep ->
                Column(modifier = Modifier.fillMaxSize()) {
                    when (currentStep) {
                        0 -> NameStep(name = name, onNameChange = viewModel::setName)
                        1 -> AgeStep(age = age, onAgeChange = viewModel::setAge)
                        2 -> ChoiceStep(
                            question = "How do you identify?",
                            options = listOf("Man", "Woman", "Other"),
                            selected = gender,
                            onSelect = viewModel::setGender
                        )
                        3 -> ChoiceStep(
                            question = "Who are you open to connecting with?",
                            options = listOf("Men", "Women", "Everyone"),
                            selected = interestedIn,
                            onSelect = viewModel::setInterestedIn
                        )
                        4 -> ChoiceStep(
                            question = "What brings you here?",
                            options = listOf(
                                "Explore connection",
                                "Practice vulnerability",
                                "Find what I want",
                                "Just curious"
                            ),
                            selected = vibe,
                            onSelect = viewModel::setVibe
                        )
                    }
                }
            }

            // Error
            error?.let {
                Text(it, color = Color(0xFFEF4444), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            // Continue button
            Button(
                onClick = { viewModel.next() },
                enabled = canContinue && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .imePadding(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Saffron,
                    disabledContainerColor = Saffron.copy(alpha = 0.3f)
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        if (step == 4) "Let's go →" else "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProgressBar(step: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i <= step) Saffron else DarkBorder)
            )
        }
    }
}

@Composable
private fun NameStep(name: String, onNameChange: (String) -> Unit) {
    Column {
        StepLabel("Step 1 of 5")
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "What should we call you?",
            fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White,
            lineHeight = 36.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Your first name", color = TextMuted) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Saffron,
                unfocusedBorderColor = DarkBorder,
                cursorColor = Saffron,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun AgeStep(age: Int, onAgeChange: (Int) -> Unit) {
    Column {
        StepLabel("Step 2 of 5")
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "How old are you?",
            fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White,
            lineHeight = 36.sp
        )
        Spacer(modifier = Modifier.height(48.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .clickable { onAgeChange(age - 1) },
                contentAlignment = Alignment.Center
            ) {
                Text("−", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(32.dp))
            Text(
                text = "$age",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(32.dp))
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Saffron)
                    .clickable { onAgeChange(age + 1) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Ages 18 – 65", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoiceStep(
    question: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column {
        val stepNum = when {
            options.size == 3 && options.contains("Man") -> 3
            options.size == 3 && options.contains("Men") -> 4
            else -> 5
        }
        StepLabel("Step $stepNum of 5")
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            question,
            fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White,
            lineHeight = 36.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.forEach { option ->
                val isSelected = selected == option
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected) Saffron else DarkBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(if (isSelected) Saffron else Color.Transparent)
                        .clickable { onSelect(option) }
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        option,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun StepLabel(text: String) {
    Text(text, color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
}
