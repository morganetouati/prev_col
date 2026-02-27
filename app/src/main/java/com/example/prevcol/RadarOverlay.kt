package com.example.prevcol

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager

/**
 * Overlay radar HUD avec scan animÃ©, traÃ®nÃ©es de mouvement et halos
 */
class RadarOverlay(private val context: Context) {

    private var windowManager: WindowManager? = null
    private var radarView: RadarView? = null
    private var isShowing = false

    private var detectedObjects = mutableListOf<DetectedObject>()

    data class DetectedObject(
        val type: ObjectType,
        val distance: Float,
        val angle: Float,
        val height: Float,
        val direction: DemoDetectionSimulator.MovementDirection
    )

    /**
     * Vérifie si la permission overlay est accordée
     */
    fun canShowOverlay(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun show() {
        if (isShowing) return
        
        // Vérifie la permission AVANT d'essayer d'afficher
        if (!canShowOverlay()) {
            // Pas de permission = pas de radar, mais l'app continue de fonctionner
            return
        }
        
        try {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val params = WindowManager.LayoutParams(
                420, 420,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.CENTER
            radarView = RadarView(context)
            windowManager?.addView(radarView, params)
            isShowing = true
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun hide() {
        if (!isShowing) return
        try {
            radarView?.stopAnimation()
            radarView?.let { windowManager?.removeView(it) }
            isShowing = false
            radarView = null
            windowManager = null
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun updateDetections(distance: Float, angle: Float, objectType: ObjectType, height: Float, direction: DemoDetectionSimulator.MovementDirection) {
        detectedObjects.clear()
        detectedObjects.add(DetectedObject(objectType, distance, angle, height, direction))
        radarView?.updateObjects(detectedObjects)
    }

    inner class RadarView(context: Context) : View(context) {

        private val animHandler = Handler(Looper.getMainLooper())
        private var scanAngle = 0f
        private var isAnimating = false
        private var objects = listOf<DetectedObject>()
        private val trailPositions = ArrayDeque<Pair<Float, Float>>()

        private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CC000000"); style = Paint.Style.FILL
        }
        private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5000FF00"); style = Paint.Style.STROKE; strokeWidth = 1.5f
        }
        private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3000FF00"); style = Paint.Style.STROKE; strokeWidth = 1f
        }
        private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00FF00"); style = Paint.Style.FILL
        }
        private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8800FF00"); textSize = 17f; textAlign = Paint.Align.CENTER
        }
        private val objectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 34f; textAlign = Paint.Align.CENTER
        }
        private val distPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 15f; textAlign = Paint.Align.CENTER
        }
        private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 22f; textAlign = Paint.Align.CENTER; color = Color.WHITE
        }
        private val youPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8800FF00"); textSize = 15f; textAlign = Paint.Align.CENTER
        }

        init { startAnimation() }

        fun startAnimation() {
            if (isAnimating) return
            isAnimating = true
            tick()
        }

        fun stopAnimation() {
            isAnimating = false
            animHandler.removeCallbacksAndMessages(null)
        }

        private fun tick() {
            if (!isAnimating) return
            scanAngle = (scanAngle + 3f) % 360f
            invalidate()
            animHandler.postDelayed(::tick, 33L)
        }

        fun updateObjects(newObjects: List<DetectedObject>) {
            val cx = width / 2f; val cy = height / 2f; val maxR = minOf(width, height) / 2f - 18f
            objects.forEach { obj ->
                val (x, y) = objPos(obj, cx, cy, maxR)
                if (trailPositions.size >= 15) trailPositions.removeFirst()
                trailPositions.addLast(Pair(x, y))
            }
            objects = newObjects
        }

        private fun objPos(obj: DetectedObject, cx: Float, cy: Float, maxR: Float): Pair<Float, Float> {
            val r = maxR * (obj.distance / 10f).coerceIn(0f, 1f)
            val rad = Math.toRadians(obj.angle.toDouble())
            return Pair(cx + (r * Math.sin(rad)).toFloat(), cy - (r * Math.cos(rad)).toFloat())
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val cx = width / 2f; val cy = height / 2f
            val maxR = minOf(width, height) / 2f - 18f

            // Fond circulaire
            canvas.drawCircle(cx, cy, maxR + 8f, bgPaint)

            // Anneaux
            canvas.drawCircle(cx, cy, maxR * 0.2f, ringPaint)
            canvas.drawCircle(cx, cy, maxR * 0.5f, ringPaint)
            canvas.drawCircle(cx, cy, maxR * 1.0f, ringPaint)
            canvas.drawText("2m", cx, cy - maxR * 0.2f + 17f, labelPaint)
            canvas.drawText("5m", cx, cy - maxR * 0.5f + 17f, labelPaint)
            canvas.drawText("10m", cx, cy - maxR * 1.0f + 17f, labelPaint)

            // Axes
            canvas.drawLine(cx - maxR, cy, cx + maxR, cy, axisPaint)
            canvas.drawLine(cx, cy - maxR, cx, cy + maxR, axisPaint)

            // Ligne de scan animÃ©e avec traÃ®nÃ©e lumineuse
            canvas.save()
            val clip = Path().apply { addCircle(cx, cy, maxR, Path.Direction.CW) }
            canvas.clipPath(clip)

            val trailSweep = 100f
            val startRot = scanAngle - 90f - trailSweep
            val sweepGrad = SweepGradient(cx, cy,
                intArrayOf(Color.argb(0,0,255,0), Color.argb(70,0,255,0), Color.argb(0,0,255,0)),
                floatArrayOf(0f, 0.75f, 1f)
            ).also {
                val m = Matrix(); m.setRotate(startRot, cx, cy); it.setLocalMatrix(m)
            }
            canvas.drawCircle(cx, cy, maxR, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = sweepGrad; style = Paint.Style.FILL
            })
            val sRad = Math.toRadians(scanAngle.toDouble())
            val sx = cx + (maxR * Math.sin(sRad)).toFloat()
            val sy = cy - (maxR * Math.cos(sRad)).toFloat()
            canvas.drawLine(cx, cy, sx, sy, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(200, 0, 255, 0); strokeWidth = 2f; style = Paint.Style.STROKE
            })
            canvas.restore()

            // TraÃ®nÃ©es de mouvement
            trailPositions.forEachIndexed { i, (tx, ty) ->
                val alpha = ((i + 1) * 15).coerceAtMost(100)
                canvas.drawCircle(tx, ty, 4f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(alpha, 255, 165, 0); style = Paint.Style.FILL
                })
            }

            // Point central
            canvas.drawCircle(cx, cy, 7f, centerPaint)
            canvas.drawText("VOUS", cx, cy + 22f, youPaint)

            // Objets
            objects.forEach { drawObject(canvas, cx, cy, maxR, it) }
        }

        private fun drawObject(canvas: Canvas, cx: Float, cy: Float, maxR: Float, obj: DetectedObject) {
            val (x, y) = objPos(obj, cx, cy, maxR)
            val color = when {
                obj.distance < 1.5f -> Color.RED
                obj.distance < 2.5f -> Color.parseColor("#FFA500")
                else -> Color.YELLOW
            }
            // Halo
            canvas.drawCircle(x, y, 26f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color; alpha = 55; style = Paint.Style.FILL
            })
            // IcÃ´ne
            objectPaint.color = color
            canvas.drawText(obj.type.icon, x, y + 12f, objectPaint)
            // Distance
            distPaint.color = color
            canvas.drawText("%.1fm".format(obj.distance), x, y + 30f, distPaint)
            // FlÃ¨che
            val arrow = when (obj.direction) {
                DemoDetectionSimulator.MovementDirection.APPROACHING -> "â¬†ï¸"
                DemoDetectionSimulator.MovementDirection.RECEDING -> "â¬‡ï¸"
                DemoDetectionSimulator.MovementDirection.MOVING_LEFT -> "â¬…ï¸"
                DemoDetectionSimulator.MovementDirection.MOVING_RIGHT -> "âž¡ï¸"
                DemoDetectionSimulator.MovementDirection.STATIONARY -> ""
            }
            if (arrow.isNotEmpty()) canvas.drawText(arrow, x + 30f, y + 5f, arrowPaint)
        }
    }
}
    
