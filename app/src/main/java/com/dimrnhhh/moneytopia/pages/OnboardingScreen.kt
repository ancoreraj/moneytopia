package com.dimrnhhh.moneytopia.pages

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("moneytopia_prefs", Context.MODE_PRIVATE)
    val pages = listOf("Welcome to Moneytopia!", "Track Your Expenses", "Gain Insights")
    val pagerState = rememberPagerState(
        pageCount = { pages.size }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            Text(text = pages[page], style = MaterialTheme.typography.headlineMedium)
        }

        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            if (pagerState.currentPage == pages.size - 1) {
                Button(onClick = {
                    sharedPreferences.edit().putBoolean("isFirstTime", false).apply()
                    navController.navigate("expenses") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }) {
                    Text("Get Started")
                }
            }
        }
}    }

