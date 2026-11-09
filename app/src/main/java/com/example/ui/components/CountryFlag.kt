package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CountryFlag(
    currencyCode: String,
    modifier: Modifier = Modifier,
    width: Dp = 44.dp,
    height: Dp = 32.dp
) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(shape)
            .border(0.75.dp, com.example.ui.theme.AppTheme.colors.cardBorder, shape)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            when (currencyCode.uppercase()) {
                "USD" -> drawUsFlag()
                "SGD" -> drawSingaporeFlag()
                "VND" -> drawVietnamFlag()
                "IDR" -> drawIndonesiaFlag()
                "CNY" -> drawChinaFlag()
                "MYR" -> drawMalaysiaFlag()
                "EUR" -> drawEuroFlag()
                "JPY" -> drawJapanFlag()
                "THB" -> drawThailandFlag()
                "CAD" -> drawCanadaFlag()
                "HKD" -> drawHongKongFlag()
                "KRW" -> drawKoreaFlag()
                "TWD" -> drawTaiwanFlag()
                else -> drawGenericFlag(currencyCode)
            }
        }
    }
}

private fun DrawScope.drawUsFlag() {
    val stripeHeight = size.height / 13f
    val redColor = Color(0xFFB22234)
    val whiteColor = Color(0xFFFFFFFF)
    val blueColor = Color(0xFF3C3B6E)

    for (i in 0 until 13) {
        drawRect(
            color = if (i % 2 == 0) redColor else whiteColor,
            topLeft = Offset(0f, i * stripeHeight),
            size = Size(size.width, stripeHeight + 0.5f)
        )
    }

    val cantonWidth = size.width * 0.45f
    val cantonHeight = stripeHeight * 7f
    drawRect(
        color = blueColor,
        topLeft = Offset.Zero,
        size = Size(cantonWidth, cantonHeight)
    )

    // Draw grid of stars
    val starRows = 4
    val starCols = 5
    val dx = cantonWidth / (starCols + 1)
    val dy = cantonHeight / (starRows + 1)
    for (r in 1..starRows) {
        for (c in 1..starCols) {
            val cx = c * dx + if (r % 2 == 0) dx * 0.25f else 0f
            val cy = r * dy
            drawCircle(color = whiteColor, radius = size.height * 0.022f, center = Offset(cx, cy))
        }
    }
}

private fun DrawScope.drawSingaporeFlag() {
    val redColor = Color(0xFFED2939)
    val whiteColor = Color(0xFFFFFFFF)

    drawRect(color = redColor, topLeft = Offset.Zero, size = Size(size.width, size.height / 2f))
    drawRect(color = whiteColor, topLeft = Offset(0f, size.height / 2f), size = Size(size.width, size.height / 2f))

    // Crescent
    val crescentCenter = Offset(size.width * 0.22f, size.height * 0.25f)
    val radius = size.height * 0.18f
    drawCircle(color = whiteColor, radius = radius, center = crescentCenter)
    drawCircle(color = redColor, radius = radius * 0.82f, center = Offset(crescentCenter.x + radius * 0.38f, crescentCenter.y))

    // 5 stars in pentagon shape
    val starsCenter = Offset(size.width * 0.32f, size.height * 0.25f)
    val orbitRadius = radius * 0.55f
    for (i in 0 until 5) {
        val angle = (i * 72 - 90) * PI / 180
        val sx = starsCenter.x + orbitRadius * cos(angle).toFloat()
        val sy = starsCenter.y + orbitRadius * sin(angle).toFloat()
        drawCircle(color = whiteColor, radius = size.height * 0.024f, center = Offset(sx, sy))
    }
}

private fun DrawScope.drawVietnamFlag() {
    val redColor = Color(0xFFDA251D)
    val yellowColor = Color(0xFFFFEB3B)

    drawRect(color = redColor, topLeft = Offset.Zero, size = size)
    drawStar(
        center = Offset(size.width / 2f, size.height / 2f),
        radius = size.height * 0.34f,
        color = yellowColor
    )
}

private fun DrawScope.drawIndonesiaFlag() {
    val redColor = Color(0xFFCE1126)
    val whiteColor = Color(0xFFFFFFFF)

    drawRect(color = redColor, topLeft = Offset.Zero, size = Size(size.width, size.height / 2f))
    drawRect(color = whiteColor, topLeft = Offset(0f, size.height / 2f), size = Size(size.width, size.height / 2f))
}

private fun DrawScope.drawChinaFlag() {
    val redColor = Color(0xFFEE1C25)
    val yellowColor = Color(0xFFFFD100)

    drawRect(color = redColor, topLeft = Offset.Zero, size = size)

    // Big star
    drawStar(
        center = Offset(size.width * 0.22f, size.height * 0.32f),
        radius = size.height * 0.20f,
        color = yellowColor
    )

    // 4 smaller stars in arc
    val smallStars = listOf(
        Offset(size.width * 0.38f, size.height * 0.16f),
        Offset(size.width * 0.44f, size.height * 0.28f),
        Offset(size.width * 0.44f, size.height * 0.42f),
        Offset(size.width * 0.38f, size.height * 0.54f)
    )
    for (pt in smallStars) {
        drawStar(center = pt, radius = size.height * 0.065f, color = yellowColor)
    }
}

private fun DrawScope.drawMalaysiaFlag() {
    val stripeHeight = size.height / 14f
    val redColor = Color(0xFFCC0000)
    val whiteColor = Color(0xFFFFFFFF)
    val blueColor = Color(0xFF000066)
    val yellowColor = Color(0xFFFFCC00)

    for (i in 0 until 14) {
        drawRect(
            color = if (i % 2 == 0) redColor else whiteColor,
            topLeft = Offset(0f, i * stripeHeight),
            size = Size(size.width, stripeHeight + 0.5f)
        )
    }

    val cantonWidth = size.width * 0.52f
    val cantonHeight = stripeHeight * 8f
    drawRect(color = blueColor, topLeft = Offset.Zero, size = Size(cantonWidth, cantonHeight))

    // Crescent
    val cCenter = Offset(cantonWidth * 0.38f, cantonHeight * 0.5f)
    val r = cantonHeight * 0.35f
    drawCircle(color = yellowColor, radius = r, center = cCenter)
    drawCircle(color = blueColor, radius = r * 0.82f, center = Offset(cCenter.x + r * 0.32f, cCenter.y))

    // 14 point star
    drawStar(center = Offset(cantonWidth * 0.68f, cantonHeight * 0.5f), radius = cantonHeight * 0.32f, color = yellowColor, points = 14)
}

private fun DrawScope.drawEuroFlag() {
    val blueColor = Color(0xFF003399)
    val yellowColor = Color(0xFFFFCC00)

    drawRect(color = blueColor, topLeft = Offset.Zero, size = size)

    val center = Offset(size.width / 2f, size.height / 2f)
    val ringRadius = size.height * 0.32f
    for (i in 0 until 12) {
        val angle = (i * 30 - 90) * PI / 180
        val sx = center.x + ringRadius * cos(angle).toFloat()
        val sy = center.y + ringRadius * sin(angle).toFloat()
        drawStar(center = Offset(sx, sy), radius = size.height * 0.055f, color = yellowColor)
    }
}

private fun DrawScope.drawJapanFlag() {
    drawRect(color = Color.White, topLeft = Offset.Zero, size = size)
    drawCircle(
        color = Color(0xFFBC002D),
        radius = size.height * 0.30f,
        center = Offset(size.width / 2f, size.height / 2f)
    )
}

private fun DrawScope.drawThailandFlag() {
    val h = size.height
    val redColor = Color(0xFFA51931)
    val whiteColor = Color(0xFFF4F5F8)
    val blueColor = Color(0xFF2D2A4A)

    drawRect(color = redColor, topLeft = Offset.Zero, size = Size(size.width, h / 6f))
    drawRect(color = whiteColor, topLeft = Offset(0f, h / 6f), size = Size(size.width, h / 6f))
    drawRect(color = blueColor, topLeft = Offset(0f, 2f * h / 6f), size = Size(size.width, 2f * h / 6f))
    drawRect(color = whiteColor, topLeft = Offset(0f, 4f * h / 6f), size = Size(size.width, h / 6f))
    drawRect(color = redColor, topLeft = Offset(0f, 5f * h / 6f), size = Size(size.width, h / 6f + 0.5f))
}

private fun DrawScope.drawCanadaFlag() {
    val redColor = Color(0xFFFF0000)
    val whiteColor = Color(0xFFFFFFFF)

    drawRect(color = redColor, topLeft = Offset.Zero, size = Size(size.width * 0.25f, size.height))
    drawRect(color = whiteColor, topLeft = Offset(size.width * 0.25f, 0f), size = Size(size.width * 0.5f, size.height))
    drawRect(color = redColor, topLeft = Offset(size.width * 0.75f, 0f), size = Size(size.width * 0.25f, size.height))

    // Maple leaf stylized silhouette
    val cx = size.width / 2f
    val cy = size.height / 2f
    val path = Path().apply {
        moveTo(cx, cy - size.height * 0.34f)
        lineTo(cx + size.width * 0.07f, cy - size.height * 0.12f)
        lineTo(cx + size.width * 0.18f, cy - size.height * 0.16f)
        lineTo(cx + size.width * 0.13f, cy + size.height * 0.04f)
        lineTo(cx + size.width * 0.18f, cy + size.height * 0.16f)
        lineTo(cx + size.width * 0.04f, cy + size.height * 0.12f)
        lineTo(cx + size.width * 0.02f, cy + size.height * 0.28f)
        lineTo(cx - size.width * 0.02f, cy + size.height * 0.28f)
        lineTo(cx - size.width * 0.04f, cy + size.height * 0.12f)
        lineTo(cx - size.width * 0.18f, cy + size.height * 0.16f)
        lineTo(cx - size.width * 0.13f, cy + size.height * 0.04f)
        lineTo(cx - size.width * 0.18f, cy - size.height * 0.16f)
        lineTo(cx - size.width * 0.07f, cy - size.height * 0.12f)
        close()
    }
    drawPath(path = path, color = redColor)
}

private fun DrawScope.drawHongKongFlag() {
    val redColor = Color(0xFFED1C24)
    val whiteColor = Color(0xFFFFFFFF)

    drawRect(color = redColor, topLeft = Offset.Zero, size = size)

    // Bauhinia 5-petal flower
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.height * 0.28f

    for (i in 0 until 5) {
        val angle = (i * 72 - 90) * PI / 180
        val px = cx + r * cos(angle).toFloat()
        val py = cy + r * sin(angle).toFloat()
        drawCircle(color = whiteColor, radius = r * 0.42f, center = Offset(px, py))
        drawCircle(color = redColor, radius = r * 0.15f, center = Offset(px, py))
    }
    drawCircle(color = whiteColor, radius = r * 0.25f, center = Offset(cx, cy))
}

private fun DrawScope.drawKoreaFlag() {
    drawRect(color = Color.White, topLeft = Offset.Zero, size = size)

    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.height * 0.24f

    // Top half red, bottom half blue Taegeuk
    val pathRed = Path().apply {
        moveTo(cx - r, cy)
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(cx, cy - r / 2f, cx + r, cy + r / 2f),
            startAngleDegrees = 0f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(cx - r, cy - r / 2f, cx, cy + r / 2f),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        close()
    }
    drawPath(path = pathRed, color = Color(0xFFC60C30))

    val pathBlue = Path().apply {
        moveTo(cx - r, cy)
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(cx, cy - r / 2f, cx + r, cy + r / 2f),
            startAngleDegrees = 0f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(cx - r, cy - r / 2f, cx, cy + r / 2f),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        close()
    }
    drawPath(path = pathBlue, color = Color(0xFF003478))

    // Trigram markers
    val trigramColor = Color(0xFF111111)
    val dotRadius = size.height * 0.026f
    drawCircle(trigramColor, dotRadius, Offset(size.width * 0.16f, size.height * 0.22f))
    drawCircle(trigramColor, dotRadius, Offset(size.width * 0.84f, size.height * 0.22f))
    drawCircle(trigramColor, dotRadius, Offset(size.width * 0.16f, size.height * 0.78f))
    drawCircle(trigramColor, dotRadius, Offset(size.width * 0.84f, size.height * 0.78f))
}

private fun DrawScope.drawTaiwanFlag() {
    val redColor = Color(0xFFFE0000)
    val blueColor = Color(0xFF000095)
    val whiteColor = Color(0xFFFFFFFF)

    drawRect(color = redColor, topLeft = Offset.Zero, size = size)

    val cantonW = size.width * 0.5f
    val cantonH = size.height * 0.5f
    drawRect(color = blueColor, topLeft = Offset.Zero, size = Size(cantonW, cantonH))

    // 12-ray white sun
    val cx = cantonW / 2f
    val cy = cantonH / 2f
    val rOuter = cantonH * 0.36f
    val rInner = cantonH * 0.18f

    drawStar(
        center = Offset(cx, cy),
        radius = rOuter,
        innerRadius = rInner,
        color = whiteColor,
        points = 12
    )
    drawCircle(color = blueColor, radius = rInner * 0.75f, center = Offset(cx, cy))
    drawCircle(color = whiteColor, radius = rInner * 0.60f, center = Offset(cx, cy))
}

private fun DrawScope.drawGenericFlag(code: String) {
    drawRect(color = Color(0xFF1E293B), topLeft = Offset.Zero, size = size)
    drawCircle(color = Color(0xFF334155), radius = size.height * 0.35f, center = Offset(size.width / 2f, size.height / 2f))
}

private fun DrawScope.drawStar(
    center: Offset,
    radius: Float,
    innerRadius: Float = radius * 0.42f,
    color: Color,
    points: Int = 5
) {
    val path = Path()
    val totalSteps = points * 2
    val stepAngle = 2 * PI / totalSteps
    val startAngle = -PI / 2.0

    for (i in 0 until totalSteps) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = startAngle + i * stepAngle
        val x = center.x + r * cos(angle).toFloat()
        val y = center.y + r * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path = path, color = color)
}
