package com.example.hiddenbeats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun CircularSeekBar(
    progress: Float, // 0f..1f, posición real de la canción
    onProgressChange: (Float) -> Unit, // Se llama cuando el usuario suelta el drag o toca el círculo
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onStop: () -> Unit
) {
    var tempProgress by remember { mutableStateOf(progress) } // progreso durante drag
    var isDragging by remember { mutableStateOf(false) }

    var center by remember { mutableStateOf(Offset.Zero) }
    var radius by remember { mutableFloatStateOf(0f) }

    // Tamaño fijo para el círculo
    val sizeDp = 300.dp

    val minTouchRadius = radius * 0.6f // o 0.7f si quieres más tolerancia

    Box(
        modifier = Modifier
            .size(sizeDp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val dist = (offset - center).getDistance()

                    // Detecta tap y mueve thumb ahí
                    if (dist >= minTouchRadius) {
                        val sizeF = androidx.compose.ui.geometry.Size(size.width.toFloat(), size.height.toFloat())
                        val angle = angleFromCenter(offset, sizeF)
                        val normalized = angle / 360f
                        onProgressChange(normalized)
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                    },
                    onDrag = { change, _ ->
                        val sizeF = androidx.compose.ui.geometry.Size(size.width.toFloat(), size.height.toFloat())
                        val angle = angleFromCenter(change.position, sizeF)
                        tempProgress = angle / 360f
                    },
                    onDragEnd = {
                        isDragging = false
                        onProgressChange(tempProgress)
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 20.dp.toPx()

            // Centro del canvas
            center = Offset(size.width / 2f, size.height / 2f)

            // Radio del círculo (considerando stroke para centrar el trazo)
            radius = size.minDimension / 2f - stroke / 2f

            // Progreso a mostrar (durante drag se usa tempProgress)
            val displayProgress = if (isDragging) tempProgress else progress

            // Fondo gris claro
            drawCircle(
                color = Color.LightGray,
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Arco verde que muestra progreso
            drawArc(
                color = Color.Blue,
                startAngle = -90f,
                sweepAngle = 360f * displayProgress,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Dibuja el "thumb" (bolita negra) al final del progreso
            val angleInRad = Math.toRadians((360 * displayProgress - 90).toDouble())
            val thumbX = center.x + radius * cos(angleInRad).toFloat()
            val thumbY = center.y + radius * sin(angleInRad).toFloat()

            drawCircle(
                color = Color.LightGray,
                radius = 12f,
                center = Offset(thumbX, thumbY)
            )
        }

        // Botón central play/pause
        IconButton(onClick = onPlayPause, modifier = Modifier.size(72.dp)) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.LightGray,
                modifier = Modifier.size(56.dp)
            )
        }
    }

    // Botón detener debajo del círculo
    Spacer(modifier = Modifier.height(64.dp)) // más separación desde el anillo

    Button(
        onClick = onStop,
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
    ) {
        Text(
            "Detener",
            color = Color.Black,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        )
    }
}

// Función auxiliar para calcular ángulo en grados desde el centro
fun angleFromCenter(position: Offset, size: androidx.compose.ui.geometry.Size): Float {
    val centerX = size.width / 2f
    val centerY = size.height / 2f

    val dx = position.x - centerX
    val dy = position.y - centerY

    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

    // Ajustar para que 0 grados esté arriba (12 en punto)
    angle = (angle + 450f) % 360f

    return angle
}
