package com.kynex.ai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kynex.ai.core.theme.Kynex

private val SparklePath: androidx.compose.ui.graphics.Path =
    androidx.compose.ui.graphics.Path().apply {
        moveTo(12f, 2f)
        curveTo(11.5f, 4.7f, 10.4f, 6.8f, 9.2f, 8.3f)
        curveTo(8f, 9.8f, 6.5f, 10.7f, 5f, 10.7f)
        curveTo(6.5f, 10.7f, 8f, 11.6f, 9.2f, 13.1f)
        curveTo(10.4f, 14.6f, 11.5f, 16.7f, 12f, 19.4f)
        curveTo(12.5f, 16.7f, 13.6f, 14.6f, 14.8f, 13.1f)
        curveTo(16f, 11.6f, 17.5f, 10.7f, 19f, 10.7f)
        curveTo(17.5f, 10.7f, 16f, 9.8f, 14.8f, 8.3f)
        curveTo(13.6f, 6.8f, 12.5f, 4.7f, 12f, 2f)
        close()
    }

/** Sparkle logo mark from demo.html. */
@Composable
fun SparkleLogo(size: androidx.compose.ui.unit.Dp, tint: androidx.compose.ui.graphics.Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(size)) {
        val scale = size.minDimension / 24f
        androidx.compose.ui.graphics.drawscope.withTransform({
            scale(scale, scale, pivot = androidx.compose.ui.geometry.Offset.Zero)
        }) {
            drawPath(SparklePath, color = tint)
        }
    }
}

@Composable
fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isEmail: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    var visible by remember { mutableStateOf(false) }
    val colors = Kynex.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .background(colors.bgInput, RoundedCornerShape(14.dp))
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.padding(end = 10.dp).size(19.dp)
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.text),
            visualTransformation = if (isPassword && !visible) PasswordVisualTransformation()
            else VisualTransformation.None,
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(label, color = colors.textTertiary, fontSize = 15.sp)
                }
                inner()
            }
        )
        if (isPassword) {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = "Toggle password visibility",
                    tint = colors.textTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AuthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {    val colors = Kynex.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .background(
                if (enabled) colors.accent else colors.accent.copy(alpha = 0.35f),
                RoundedCornerShape(14.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = androidx.compose.ui.graphics.Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun SocialButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = Kynex.colors
    Row(
        modifier = modifier
            .background(colors.bgInput, RoundedCornerShape(14.dp))
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 24.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = colors.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
