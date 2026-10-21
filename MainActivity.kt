package com.virtualx.gamepad

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virtualx.gamepad.service.OverlayService
import com.virtualx.gamepad.ui.theme.VirtualXTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VirtualXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var isAccessibilityEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var isServiceRunning by remember { mutableStateOf(OverlayService.isRunning) }

    // 権限状態を定期的に更新
    LaunchedEffect(Unit) {
        while (true) {
            hasOverlayPermission = Settings.canDrawOverlays(context)
            isAccessibilityEnabled = isAccessibilityServiceEnabled(context)
            isServiceRunning = OverlayService.isRunning
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "VirtualX",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "ルート不要の仮想ゲームパッド",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // オーバーレイ権限
        PermissionCard(
            title = "1. オーバーレイ権限",
            description = "他のアプリの上にパッドを表示するために必要です",
            isGranted = hasOverlayPermission,
            onRequest = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            }
        )

        // Accessibility権限
        PermissionCard(
            title = "2. アクセシビリティ権限",
            description = "タップやスワイプをシミュレートするために必要です",
            isGranted = isAccessibilityEnabled,
            onRequest = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                Toast.makeText(context, "「VirtualX」を見つけて有効にしてください", Toast.LENGTH_LONG).show()
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // サービス開始/停止ボタン
        Button(
            onClick = {
                if (!hasOverlayPermission) {
                    Toast.makeText(context, "先にオーバーレイ権限を許可してください", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (!isAccessibilityEnabled) {
                    Toast.makeText(context, "先にアクセシビリティを有効にしてください", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (isServiceRunning) {
                    context.stopService(Intent(context, OverlayService::class.java))
                    Toast.makeText(context, "仮想パッドを停止しました", Toast.LENGTH_SHORT).show()
                } else {
                    context.startForegroundService(Intent(context, OverlayService::class.java))
                    Toast.makeText(context, "仮想パッドを開始しました", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isServiceRunning)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (isServiceRunning) "仮想パッドを停止" else "仮想パッドを開始",
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "使い方\n1. 上記2つの権限を許可\n2. 「仮想パッドを開始」を押す\n3. ゲームを起動してパッドを操作",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (isGranted) "許可済み" else "未許可",
                    color = if (isGranted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, style = MaterialTheme.typography.bodySmall)
            if (!isGranted) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onRequest, modifier = Modifier.align(Alignment.End)) {
                    Text("許可する")
                }
            }
        }
    }
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    return enabledServices.any { it.resolveInfo.serviceInfo.packageName == context.packageName }
}
