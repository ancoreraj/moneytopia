package com.dimrnhhh.moneytopia.pages

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("moneytopia_prefs", Context.MODE_PRIVATE)
    val pages = listOf("Welcome to Moneytopia!", "Track Your Expenses", "Gain Insights")
    val pagerState = rememberPagerState(
        pageCount = { pages.size }
    )

    var hasSmsPermission by remember { mutableStateOf(checkSmsPermission(context)) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasSmsPermission = isGranted
        if (isGranted) {
            ingestSmsData(context)
        }
    }


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
                // Show SMS Permission Button Only If Not Granted
                if (!hasSmsPermission) {
                    Button(onClick = {
                        requestPermissionLauncher.launch(Manifest.permission.READ_SMS)
                    }) {
                        Text("Grant SMS Permission")
                    }
                }


                Button(onClick = {
                    sharedPreferences.edit().putBoolean("isFirstTime", false).apply()

                    // Force recomposition by using rememberSaveable
                    (context as? ComponentActivity)?.let { activity ->
                        activity.recreate() // Restart activity to apply changes immediately
                    }

                    navController.navigate("expenses") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }) {
                    Text("Get Started")
                }

            }
        }
    }
}

fun checkSmsPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_SMS
    ) == PackageManager.PERMISSION_GRANTED
}

fun ingestSmsData(context: Context) {
    // TODO: Implement SMS Parsing Logic Here
    println("✅ SMS Data Ingested Successfully!")
}