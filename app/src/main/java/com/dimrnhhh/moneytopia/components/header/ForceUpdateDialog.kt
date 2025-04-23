package com.dimrnhhh.moneytopia.components.header

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ForceUpdateDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null
            )
        },
        title = {
            Text(
                text = "Update Available!",
                fontWeight = FontWeight.Medium
            )
        },
        text = {
            Text(
                text = "Woah! We have got an update for you.\nPlease update the app to continue using it.",
                textAlign = TextAlign.Center,
            )
        },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(
                onClick = onConfirmation
            ) {
                Text(
                    text = "Go to Play Store",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}