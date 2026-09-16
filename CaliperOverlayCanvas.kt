package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DetectedGrainItem
import com.example.model.GrainMeasurement
import com.example.model.ReferenceScale
import com.example.viewmodel.CanvasMode
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun CaliperOverlayCanvas(
    measurement: GrainMeasurement,
    referenceScale: ReferenceScale,
    canvasMode: CanvasMode,
    capturedBitmap: Bitmap?,
    scaleReticleX: Float,
    scaleReticleY: Float,
    scaleReticleSpanPx: Float,
    onCaliperChange: (lengthPx: Float, widthPx: Float) -> Unit,
    onReticleChange: (centerX: Float, centerY: Float, spanPx: Float) -> Unit,
    onGrainSelected: (DetectedGrainItem) -> Unit,
    onCanvasTapped: (x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var caliperLen by remember(measurement.caliperLengthPx) { mutableFloatStateOf(measurement.caliperLengthPx) }
    var caliperWid by remember(measurement.caliperWidthPx) { mutableFloatStateOf(measurement.caliperWidthPx) }

    var reticleX by remember(scaleReticleX) { mutableFloatStateOf(scaleReticleX) }
    var reticleY by remember(scaleReticleY) { mutableFloatStateOf(scaleReticleY) }
    var reticleSpan by remember(scaleReticleSpanPx) { mutableFloatStateOf(scaleReticleSpanPx) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(310.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111A13))
            .testTag("grain_caliper_canvas_box")
    ) {
        // 1. Underlaid Grain Image (Camera Capture, Gallery, or Lab Sample Photo)
        if (capturedBitmap != null) {
            Image(
                bitmap = capturedBitmap.asImageBitmap(),
                contentDescription = "Camera Grain Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_grain_sample_scale),
                contentDescription = "Grain Sample Scale Reference Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Optical Contrast Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x38000000))
        )

        // 3. Interactive Vector Canvas Overlay
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(canvasMode) {
                    detectTapGestures { offset ->
                        onCanvasTapped(offset.x, offset.y)
                        if (canvasMode == CanvasMode.MEASURE_GRAINS) {
                            // Check if tapped near any detected grain
                            val tappedGrain = measurement.detectedGrains.find { grain ->
                                hypot(grain.centerX - offset.x, grain.centerY - offset.y) < 45f
                            }
                            if (tappedGrain != null) {
                                onGrainSelected(tappedGrain)
                            }
                        }
                    }
                }
                .pointerInput(canvasMode) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        if (canvasMode == CanvasMode.CALIBRATE_SCALE) {
                            // Dragging moves or resizes the scale calibration reticle
                            val newX = (reticleX + dragAmount.x).coerceIn(30f, 450f)
                            val newY = (reticleY + dragAmount.y).coerceIn(30f, 290f)
                            reticleX = newX
                            reticleY = newY
                            onReticleChange(newX, newY, reticleSpan)
                        } else {
                            // Measuring Grains: drag horizontal adjusts width, vertical adjusts length
                            val newLen = (caliperLen + dragAmount.y).coerceIn(25f, 260f)
                            val newWid = (caliperWid + dragAmount.x).coerceIn(12f, 180f)
                            caliperLen = newLen
                            caliperWid = newWid
                            onCaliperChange(newLen, newWid)
                        }
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val ppm = referenceScale.pixelsPerMm

            // --- Subtle Optical Grid ---
            val gridStep = (10f * ppm).coerceIn(25f, 140f)
            var gx = 0f
            while (gx < canvasW) {
                drawLine(
                    color = Color(0x22FFFFFF),
                    start = Offset(gx, 0f),
                    end = Offset(gx, canvasH),
                    strokeWidth = 1f
                )
                gx += gridStep
            }
            var gy = 0f
            while (gy < canvasH) {
                drawLine(
                    color = Color(0x22FFFFFF),
                    start = Offset(0f, gy),
                    end = Offset(canvasW, gy),
                    strokeWidth = 1f
                )
                gy += gridStep
            }

            if (canvasMode == CanvasMode.CALIBRATE_SCALE) {
                // ==========================================
                // SCALE CALIBRATION ON IMAGE RETICLE MODE
                // ==========================================
                val cx = reticleX.coerceIn(40f, canvasW - 40f)
                val cy = reticleY.coerceIn(40f, canvasH - 40f)
                val span = reticleSpan.coerceIn(35f, 280f)
                val radius = span / 2f
                val isCirc = referenceScale.isCircular

                // Outer guide circle / boundary
                if (isCirc) {
                    // Circular Coin Calibration Reticle
                    drawCircle(
                        color = Color(0x3300E5FF),
                        radius = radius,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = radius,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                    )
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = radius * 0.75f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.2f)
                    )

                    // 12 Radial tick marks around perimeter
                    for (i in 0 until 12) {
                        val angle = (i * 30.0 * Math.PI / 180.0).toFloat()
                        val r1 = radius - 8f
                        val r2 = radius + 8f
                        drawLine(
                            color = Color(0xFF00E5FF),
                            start = Offset(cx + r1 * cos(angle), cy + r1 * sin(angle)),
                            end = Offset(cx + r2 * cos(angle), cy + r2 * sin(angle)),
                            strokeWidth = 2f
                        )
                    }

                    // Sizing drag anchor handle on right edge
                    drawCircle(
                        color = Color(0xFFFF5252),
                        radius = 9f,
                        center = Offset(cx + radius, cy)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(cx + radius, cy)
                    )
                } else {
                    // Rectangular / Bar Calibration Reticle (for 10mm target or card)
                    val half = span / 2f
                    drawRoundRect(
                        color = Color(0x33FFD54F),
                        topLeft = Offset(cx - half, cy - half),
                        size = Size(span, span),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    drawRoundRect(
                        color = Color(0xFFFFD54F),
                        topLeft = Offset(cx - half, cy - half),
                        size = Size(span, span),
                        cornerRadius = CornerRadius(8f, 8f),
                        style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                    )

                    // Dimension arrow
                    drawLine(
                        color = Color(0xFFFF5252),
                        start = Offset(cx - half, cy),
                        end = Offset(cx + half, cy),
                        strokeWidth = 2.5f
                    )
                    drawCircle(
                        color = Color(0xFFFF5252),
                        radius = 8f,
                        center = Offset(cx + half, cy)
                    )
                }

                // Center crosshair
                drawLine(
                    color = Color(0xFF00E5FF),
                    start = Offset(cx - 16f, cy),
                    end = Offset(cx + 16f, cy),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFF00E5FF),
                    start = Offset(cx, cy - 16f),
                    end = Offset(cx, cy + 16f),
                    strokeWidth = 2f
                )
                drawCircle(color = Color.White, radius = 4f, center = Offset(cx, cy))

            } else {
                // ==========================================
                // GRAIN MEASUREMENT & CALIPER MODE
                // ==========================================
                // 1. Draw detected grain contour boxes on the image
                measurement.detectedGrains.forEachIndexed { idx, grain ->
                    val gx = grain.centerX.coerceIn(20f, canvasW - 20f)
                    val gy = grain.centerY.coerceIn(20f, canvasH - 20f)
                    val glen = grain.lengthPx
                    val gwid = grain.widthPx

                    rotate(degrees = grain.angleDegrees, pivot = Offset(gx, gy)) {
                        // Contour bounding box
                        drawRoundRect(
                            color = Color(0xFF6EDC96).copy(alpha = 0.85f),
                            topLeft = Offset(gx - gwid / 2f - 4f, gy - glen / 2f - 4f),
                            size = Size(gwid + 8f, glen + 8f),
                            cornerRadius = CornerRadius(6f, 6f),
                            style = Stroke(
                                width = 1.8f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                            )
                        )
                        // Kernel center dot
                        drawCircle(
                            color = Color(0xFFFFD54F),
                            radius = 3.5f,
                            center = Offset(gx, gy)
                        )
                    }
                }

                // 2. Draw Active Precision Caliper at caliper center
                val cX = measurement.caliperCenterX.coerceIn(40f, canvasW - 40f)
                val cY = measurement.caliperCenterY.coerceIn(40f, canvasH - 40f)
                val halfLen = caliperLen / 2f
                val halfWid = caliperWid / 2f

                val caliperTop = cY - halfLen
                val caliperBottom = cY + halfLen
                val caliperLeft = cX - halfWid
                val caliperRight = cX + halfWid

                // Laser alignment crosshairs
                drawLine(
                    color = Color(0x6600E5FF),
                    start = Offset(0f, cY),
                    end = Offset(canvasW, cY),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )
                drawLine(
                    color = Color(0x6600E5FF),
                    start = Offset(cX, 0f),
                    end = Offset(cX, canvasH),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )

                // Length caliper jaws (Top & Bottom, Red)
                drawLine(
                    color = Color(0xFFFF5252),
                    start = Offset(caliperLeft - 18f, caliperTop),
                    end = Offset(caliperRight + 18f, caliperTop),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color(0xFFFF5252),
                    start = Offset(caliperLeft - 18f, caliperBottom),
                    end = Offset(caliperRight + 18f, caliperBottom),
                    strokeWidth = 3f
                )

                // Width caliper jaws (Left & Right, Green)
                drawLine(
                    color = Color(0xFF4CAF50),
                    start = Offset(caliperLeft, caliperTop - 18f),
                    end = Offset(caliperLeft, caliperBottom + 18f),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color(0xFF4CAF50),
                    start = Offset(caliperRight, caliperTop - 18f),
                    end = Offset(caliperRight, caliperBottom + 18f),
                    strokeWidth = 3f
                )

                // Measurement arrows
                // Vertical Length arrow
                drawLine(
                    color = Color(0xFFFF5252),
                    start = Offset(caliperRight + 24f, caliperTop),
                    end = Offset(caliperRight + 24f, caliperBottom),
                    strokeWidth = 2f
                )
                // Horizontal Width arrow
                drawLine(
                    color = Color(0xFF4CAF50),
                    start = Offset(caliperLeft, caliperBottom + 24f),
                    end = Offset(caliperRight, caliperBottom + 24f),
                    strokeWidth = 2f
                )

                // Corner brackets
                val bLen = 14f
                val bCol = Color(0xFFFFD54F)
                drawLine(bCol, Offset(caliperLeft, caliperTop), Offset(caliperLeft + bLen, caliperTop), 3f)
                drawLine(bCol, Offset(caliperLeft, caliperTop), Offset(caliperLeft, caliperTop + bLen), 3f)
                drawLine(bCol, Offset(caliperRight, caliperTop), Offset(caliperRight - bLen, caliperTop), 3f)
                drawLine(bCol, Offset(caliperRight, caliperTop), Offset(caliperRight, caliperTop + bLen), 3f)
                drawLine(bCol, Offset(caliperLeft, caliperBottom), Offset(caliperLeft + bLen, caliperBottom), 3f)
                drawLine(bCol, Offset(caliperLeft, caliperBottom), Offset(caliperLeft, caliperBottom - bLen), 3f)
                drawLine(bCol, Offset(caliperRight, caliperBottom), Offset(caliperRight - bLen, caliperBottom), 3f)
                drawLine(bCol, Offset(caliperRight, caliperBottom), Offset(caliperRight, caliperBottom - bLen), 3f)

                // Drag handles
                drawCircle(Color(0xFFFF5252), radius = 7f, center = Offset(cX, caliperTop))
                drawCircle(Color(0xFFFF5252), radius = 7f, center = Offset(cX, caliperBottom))
                drawCircle(Color(0xFF4CAF50), radius = 7f, center = Offset(caliperLeft, cY))
                drawCircle(Color(0xFF4CAF50), radius = 7f, center = Offset(caliperRight, cY))
            }
        }

        // 4. Overlaid Mode Status Badge
        Surface(
            color = if (canvasMode == CanvasMode.CALIBRATE_SCALE) Color(0xDD004D40) else Color(0xBB000000),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Text(
                text = if (canvasMode == CanvasMode.CALIBRATE_SCALE) {
                    "CALIBRATE: Align circle over ${referenceScale.realMm}mm scale object"
                } else {
                    "OPTICAL CALIPER: Tap grain or drag jaws"
                },
                color = if (canvasMode == CanvasMode.CALIBRATE_SCALE) Color(0xFF80CBC4) else Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // 5. Reference Scale Bar (Bottom Right)
        Surface(
            color = Color(0xBB000000),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            Text(
                text = "${"%.1f".format(referenceScale.pixelsPerMm)} px/mm",
                color = Color(0xFF6EDC96),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
