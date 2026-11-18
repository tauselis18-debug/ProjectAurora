package com.projectaurora.editor

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix as AndroidColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.pow

private val AppBackground = Color(0xFF101114)
private val Panel = Color(0xFF1A1C21)
private val Accent = Color(0xFFD8B477)
private val Muted = Color(0xFF9297A1)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AuroraApp() }
    }
}

@Composable
private fun AuroraApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var exposure by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(0f) }
    var selectedTab by remember { mutableStateOf("Light") }
    var status by remember { mutableStateOf("Choose a photo to begin") }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                status = "Loading photo…"
                sourceBitmap = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)
                    }
                }
                status = if (sourceBitmap != null) "Ready to edit" else
                    "This file could not be decoded by the starter engine"
            }
        }
    }

    val matrix = remember(exposure, contrast, saturation) {
        buildPreviewMatrix(exposure, contrast, saturation)
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = AppBackground,
            surface = Panel,
            primary = Accent,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("AURORA", color = Accent, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Photo Studio", color = Color.White, fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { picker.launch("image/*") }) {
                        Text("Open")
                    }
                    Button(
                        onClick = {
                            val bmp = sourceBitmap
                            if (bmp == null) {
                                status = "Open a photo first"
                            } else {
                                scope.launch {
                                    status = "Exporting JPEG…"
                                    val saved = withContext(Dispatchers.IO) {
                                        exportJpeg(context, bmp, exposure, contrast, saturation)
                                    }
                                    status = if (saved) "Saved to Pictures/Aurora" else "Export failed"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Accent,
                            contentColor = Color(0xFF171717))
                    ) { Text("Export") }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF08090B), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                val bmp = sourceBitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Photo preview",
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.colorMatrix(matrix)
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("✦", color = Accent, fontSize = 36.sp)
                        Text("Your photograph goes here", color = Color.White,
                            fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("Open an image to start editing", color = Muted, fontSize = 13.sp)
                        Button(onClick = { picker.launch("image/*") }) {
                            Text("Choose photo")
                        }
                    }
                }
            }

            Text(status, color = Muted, fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Light", "Color", "Detail").forEach { tab ->
                    val active = selectedTab == tab
                    TextButton(
                        onClick = { selectedTab = tab },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (active) Accent else Muted
                        )
                    ) {
                        Text(tab, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 190.dp, max = 250.dp)
                    .verticalScroll(rememberScrollState())
                    .background(Panel, RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                when (selectedTab) {
                    "Light" -> {
                        SliderControl("Exposure", exposure, -2f, 2f, " EV") { exposure = it }
                        SliderControl("Contrast", contrast, -100f, 100f, "") { contrast = it }
                    }
                    "Color" -> {
                        SliderControl("Saturation", saturation, -100f, 100f, "") { saturation = it }
                        Text("Temperature, tint, HSL and colour grading are planned for the next engine milestone.",
                            color = Muted, fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
                    }
                    else -> {
                        Text("Detail tools", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Sharpening, luminance/chroma noise reduction and lens corrections are planned for the RAW engine milestone.",
                            color = Muted, fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        exposure = 0f
                        contrast = 0f
                        saturation = 0f
                    }) { Text("Reset adjustments", color = Accent) }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SliderControl(
    title: String,
    value: Float,
    min: Float,
    max: Float,
    suffix: String,
    onValue: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, color = Color.White, fontSize = 13.sp)
            Text("${"%.2f".format(value)}$suffix", color = Accent, fontSize = 12.sp)
        }
        Slider(
            value = value,
            onValueChange = onValue,
            valueRange = min..max,
            colors = SliderDefaults.colors(
                thumbColor = Accent,
                activeTrackColor = Accent,
                inactiveTrackColor = Color(0xFF41444B)
            )
        )
    }
}

private fun buildPreviewMatrix(exposure: Float, contrast: Float, saturation: Float): ColorMatrix {
    val e = 2.0.pow(exposure.toDouble()).toFloat()
    val c = 1f + contrast / 100f
    val s = 1f + saturation / 100f
    val r = 0.213f * (1f - s)
    val g = 0.715f * (1f - s)
    val b = 0.072f * (1f - s)
    val k = c * e
    val offset = 128f * (1f - c)
    return ColorMatrix(floatArrayOf(
        k * (r + s), k * g,       k * b,       0f, offset,
        k * r,       k * (g + s), k * b,       0f, offset,
        k * r,       k * g,       k * (b + s), 0f, offset,
        0f,          0f,          0f,          1f, 0f
    ))
}

private fun exportJpeg(
    context: android.content.Context,
    source: Bitmap,
    exposure: Float,
    contrast: Float,
    saturation: Float
): Boolean {
    return try {
        val androidMatrix = AndroidColorMatrix()
        val e = 2.0.pow(exposure.toDouble()).toFloat()
        val c = 1f + contrast / 100f
        val s = 1f + saturation / 100f
        val r = 0.213f * (1f - s)
        val g = 0.715f * (1f - s)
        val b = 0.072f * (1f - s)
        val k = c * e
        val offset = 128f * (1f - c)
        androidMatrix.set(floatArrayOf(
            k * (r + s), k * g, k * b, 0f, offset,
            k * r, k * (g + s), k * b, 0f, offset,
            k * r, k * g, k * (b + s), 0f, offset,
            0f, 0f, 0f, 1f, 0f
        ))
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(androidMatrix)
        }
        canvas.drawBitmap(source, 0f, 0f, paint)

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Aurora_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Aurora")
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return false
        val success = context.contentResolver.openOutputStream(uri)?.use {
            output.compress(Bitmap.CompressFormat.JPEG, 96, it)
        } ?: false
        output.recycle()
        success
    } catch (_: Exception) {
        false
    }
}
