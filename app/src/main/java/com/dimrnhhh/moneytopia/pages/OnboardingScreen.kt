package com.dimrnhhh.moneytopia.pages

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dimrnhhh.moneytopia.components.charts.HorizontalPagerIndicator
import com.dimrnhhh.moneytopia.smsHandling.checkNotificationPermission
import com.dimrnhhh.moneytopia.smsHandling.checkSmsPermission
import com.dimrnhhh.moneytopia.smsHandling.ingestSmsData

@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("moneytopia_prefs", Context.MODE_PRIVATE)
    val pages = listOf("Welcome to Moneytopia!", "Track Your Expenses", "Gain Insights")
    val pagerState = rememberPagerState(
        pageCount = { pages.size }
    )

    val permissionsList =
        arrayOf(Manifest.permission.READ_SMS, Manifest.permission.POST_NOTIFICATIONS)

    var hasSmsPermission by remember { mutableStateOf(checkSmsPermission(context)) }
    var hasNotificationPermission by remember { mutableStateOf(checkNotificationPermission(context)) }

    val smsPermissionRequestCount = remember { mutableStateOf(0) }
    val notificationPermissionRequestCount = remember { mutableStateOf(0) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasSmsPermission = permissions[Manifest.permission.READ_SMS] ?: false
        hasNotificationPermission = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false

        smsPermissionRequestCount.value++
        notificationPermissionRequestCount.value++

        if (hasSmsPermission) {
            ingestSmsData(context)
        } else {
            Toast.makeText(context, "SMS Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(smsPermissionRequestCount.value, notificationPermissionRequestCount.value) {
        if ((!hasSmsPermission && smsPermissionRequestCount.value == 1) ||
            (!hasNotificationPermission && notificationPermissionRequestCount.value == 1)
        ) {
            requestPermissionLauncher.launch(permissionsList)
        } else if ((hasSmsPermission && hasNotificationPermission) ||
            (smsPermissionRequestCount.value == 2 || notificationPermissionRequestCount.value == 2)
        ) {
            onGetStarted(sharedPreferences, context, navController)
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
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = pages[page],
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (pagerState.currentPage == pages.size - 1) {
                        Button(onClick = {
                            if (hasSmsPermission && hasNotificationPermission) {
                                onGetStarted(sharedPreferences, context, navController)
                            } else {
                                requestPermissionLauncher.launch(permissionsList)
                            }
                        }) {
                            Text("Get Started")
                        }

                    }
                }
            }

        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier.padding(bottom = 32.dp),
            isInReverseOrder = false
        )
    }
}

private fun onGetStarted(
    sharedPreferences: SharedPreferences,
    context: Context,
    navController: NavController
) {
    sharedPreferences.edit().putBoolean("isFirstTime", false).apply()

    // Force recomposition by using rememberSaveable
    (context as? ComponentActivity)?.recreate()

    navController.navigate("expenses") {
        popUpTo("onboarding") { inclusive = true }
    }
}