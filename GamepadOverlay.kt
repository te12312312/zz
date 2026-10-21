package com.virtualx.gamepad.ui

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virtualx.gamepad.service.GamepadAccessibilityService
import kotlin.math.roundToInt

@Composable
fun GamepadOverlay(onClose: () -> Unit) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // パッド全体の透明度
    var alpha by remember { mutableFloatStateOf(0.75f) }

    // 画面サイズ（簡易）
    val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
    val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()

    // バイブレーター
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(VibratorManager::class.java)
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
    }

    fun vibrate(ms: Long = 30) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(ms)
        }
    }

    // ゲーム画面上の実際の操作座標（簡易マッピング）
    // 実際のゲームに合わせて調整が必要です
    val leftStickCenter = Offset(screenWidth * 0.2f, screenHeight * 0.7f)
    val rightStickCenter = Offset(screenWidth * 0.8f, screenHeight * 0.7f)
    val buttonA = Offset(screenWidth * 0.85f, screenHeight * 0.55f)
    val buttonB = Offset(screenWidth * 0.75f, screenHeight * 0.45f)
    val buttonX = Offset(screenWidth * 0.75f, screenHeight * 0.65f)
    val buttonY = Offset(screenWidth * 0.65f, screenHeight * 0.55f)
    val dpadCenter = Offset(screenWidth * 0.2f, screenHeight * 0.4f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
    ) {
        // ===== 左スティック =====
        AnalogStick(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 80.dp),
            label = "L",
            onMove = { dx, dy ->
                val targetX = leftStickCenter.x + dx * 80f
                val targetY = leftStickCenter.y + dy * 80f
                GamepadAccessibilityService.instance?.performSwipe(
                    leftStickCenter.x, leftStickCenter.y,
                    targetX, targetY,
                    80
                )
            },
            onRelease = {
                // 中心に戻すスワイプ
                GamepadAccessibilityService.instance?.performSwipe(
                    leftStickCenter.x + 10f, leftStickCenter.y,
                    leftStickCenter.x, leftStickCenter.y,
                    50
                )
            },
            onVibrate = { vibrate() }
        )

        // ===== 右スティック =====
        AnalogStick(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 80.dp),
            label = "R",
            onMove = { dx, dy ->
                val targetX = rightStickCenter.x + dx * 80f
                val targetY = rightStickCenter.y + dy * 80f
                GamepadAccessibilityService.instance?.performSwipe(
                    rightStickCenter.x, rightStickCenter.y,
                    targetX, targetY,
                    80
                )
            },
            onRelease = {
                GamepadAccessibilityService.instance?.performSwipe(
                    rightStickCenter.x + 10f, rightStickCenter.y,
                    rightStickCenter.x, rightStickCenter.y,
                    50
                )
            },
            onVibrate = { vibrate() }
        )

        // ===== D-Pad =====
        DPad(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
            onDirection = { dir ->
                vibrate()
                val offset = when (dir) {
                    "up" -> Offset(0f, -60f)
                    "down" -> Offset(0f, 60f)
                    "left" -> Offset(-60f, 0f)
                    "right" -> Offset(60f, 0f)
                    else -> Offset.Zero
                }
                GamepadAccessibilityService.instance?.performTap(
                    dpadCenter.x + offset.x,
                    dpadCenter.y + offset.y
                )
            }
        )

        // ===== ABXY ボタン =====
        ActionButtons(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            onButton = { button ->
                vibrate()
                val pos = when (button) {
                    "A" -> buttonA
                    "B" -> buttonB
                    "X" -> buttonX
                    "Y" -> buttonY
                    else -> buttonA
                }
                GamepadAccessibilityService.instance?.performTap(pos.x, pos.y)
            }
        )

        // ===== 上部コントロール（透明度・閉じる） =====
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "透明度",
                color = Color.White,
                fontSize = 12.sp
            )
            // 簡易スライダー代わり
            listOf(0.4f, 0.6f, 0.8f, 1.0f).forEach { value ->
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (alpha == value) Color(0xFFBB86FC) else Color.Gray
                        )
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { alpha = value },
                                onDrag = { _, _ -> }
                            )
                        }
                        .then(
                            Modifier.pointerInput(Unit) {
                                // タップでも変更
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        if (event.changes.any { it.pressed }) {
                                            alpha = value
                                        }
                                    }
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(value * 100).toInt()}",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Red.copy(alpha = 0.8f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onClose() },
                            onDrag = { _, _ -> }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("閉じる", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AnalogStick(
    modifier: Modifier = Modifier,
    label: String,
    onMove: (dx: Float, dy: Float) -> Unit,
    onRelease: () -> Unit,
    onVibrate: () -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    val maxRadius = 50f

    Box(
        modifier = modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        // ベース
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
                .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
        )
        // ノブ
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(offset.x.roundToInt(), offset.y.roundToInt())
                }
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFFBB86FC).copy(alpha = 0.9f))
                .border(2.dp, Color.White, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { onVibrate() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = offset + dragAmount
                            val distance = newOffset.getDistance()
                            offset = if (distance > maxRadius) {
                                newOffset / distance * maxRadius
                            } else {
                                newOffset
                            }
                            onMove(offset.x / maxRadius, offset.y / maxRadius)
                        },
                        onDragEnd = {
                            offset = Offset.Zero
                            onRelease()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DPad(
    modifier: Modifier = Modifier,
    onDirection: (String) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DPadButton("↑") { onDirection("up") }
        Row {
            DPadButton("←") { onDirection("left") }
            Spacer(modifier = Modifier.width(40.dp))
            DPadButton("→") { onDirection("right") }
        }
        DPadButton("↓") { onDirection("down") }
    }
}

@Composable
fun DPadButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onClick() },
                    onDrag = { _, _ -> }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 18.sp)
    }
}

@Composable
fun ActionButtons(
    modifier: Modifier = Modifier,
    onButton: (String) -> Unit
) {
    Box(modifier = modifier.size(160.dp)) {
        // Y (上)
        ActionButton(
            text = "Y",
            color = Color(0xFF4CAF50),
            modifier = Modifier.align(Alignment.TopCenter)
        ) { onButton("Y") }

        // X (左)
        ActionButton(
            text = "X",
            color = Color(0xFF2196F3),
            modifier = Modifier.align(Alignment.CenterStart)
        ) { onButton("X") }

        // B (右)
        ActionButton(
            text = "B",
            color = Color(0xFFF44336),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) { onButton("B") }

        // A (下)
        ActionButton(
            text = "A",
            color = Color(0xFFFFEB3B),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { onButton("A") }
    }
}

@Composable
fun ActionButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.85f))
            .border(2.dp, Color.White, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onClick() },
                    onDrag = { _, _ -> }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}
