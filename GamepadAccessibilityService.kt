package com.virtualx.gamepad.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

/**
 * ルート不要でタップ・スワイプをシミュレートする Accessibility Service
 */
class GamepadAccessibilityService : AccessibilityService() {

    companion object {
        var instance: GamepadAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 特に処理しない
    }

    override fun onInterrupt() {
        // 特に処理しない
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    /**
     * 指定座標を短くタップする
     */
    fun performTap(x: Float, y: Float, durationMs: Long = 50) {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()
        dispatchGesture(gesture, null, null)
    }

    /**
     * スワイプ（スティック用）
     * fromX/fromY から toX/toY へ durationMs かけて移動
     */
    fun performSwipe(
        fromX: Float,
        fromY: Float,
        toX: Float,
        toY: Float,
        durationMs: Long = 100
    ) {
        val path = Path().apply {
            moveTo(fromX, fromY)
            lineTo(toX, toY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()
        dispatchGesture(gesture, null, null)
    }

    /**
     * 長押し（ボタンホールド用）
     */
    fun performLongPress(x: Float, y: Float, durationMs: Long = 300) {
        performTap(x, y, durationMs)
    }
}
