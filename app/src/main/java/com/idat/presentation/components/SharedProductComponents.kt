package com.idat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FormFieldLabel(text: String) {
    Text(
        text = text, 
        fontSize = 12.sp, 
        fontWeight = FontWeight.Bold, 
        color = Color(0xFFAB005A), 
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTextField(
    value: String, 
    onValueChange: (String) -> Unit, 
    placeholder: String, 
    isNumber: Boolean = false, 
    prefix: String? = null, 
    singleLine: Boolean = true, 
    minLines: Int = 1
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.5f)) },
        prefix = if (prefix != null) { { Text(prefix, color = Color.Gray) } } else null,
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFFFF8F8),
            unfocusedContainerColor = Color(0xFFFFF8F8),
            disabledContainerColor = Color(0xFFFFF8F8),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Decimal) else KeyboardOptions.Default
    )
}

@Composable
fun CategorySelector(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Hogar y Decoración", "Moda Sostenible", "Artesanía Tradicional", "Gourmet")
    
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFF8F8))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(selected, fontSize = 14.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat) }, 
                    onClick = { onSelect(cat); expanded = false }
                )
            }
        }
    }
}

@Composable
fun MetricBox(
    modifier: Modifier, 
    label: String, 
    value: String, 
    onValueChange: (String) -> Unit, 
    icon: ImageVector
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFEE1E7).copy(alpha = 0.4f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label.uppercase(), 
            fontSize = 11.sp, 
            fontWeight = FontWeight.Bold, 
            color = Color(0xFFAB005A).copy(alpha = 0.7f), 
            letterSpacing = 1.sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                BasicTextField(
                    value = value, 
                    onValueChange = onValueChange, 
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold, 
                        textAlign = TextAlign.Center
                    )
                )
            }
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = if (label == "Calificación") Color(0xFF725000) else Color(0xFF455F88), 
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
