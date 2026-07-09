package com.atlas.agent.ui.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atlas.agent.permission.PermissionItem
import com.atlas.agent.permission.PermissionState

@Composable
fun PermissionCard(
    permission: PermissionItem,
    onActionClick: (PermissionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = permission.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when (permission.state) {
                        PermissionState.Granted -> "✅ Granted"
                        PermissionState.NotGranted -> "❌ Not Granted"
                    },
                    color = when (permission.state) {
                        PermissionState.Granted -> MaterialTheme.colorScheme.primary
                        PermissionState.NotGranted -> MaterialTheme.colorScheme.error
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (permission.state == PermissionState.Granted) {
                OutlinedButton(
                    onClick = { onActionClick(permission) }
                ) {
                    Text("Open Settings")
                }
            } else {
                Button(
                    onClick = { onActionClick(permission) }
                ) {
                    Text("Grant")
                }
            }
        }
    }
}
