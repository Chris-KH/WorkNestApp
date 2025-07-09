package com.apcs.worknestapp.ui.components.inputfield

import com.apcs.worknestapp.R
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.ui.theme.Poppins


@Composable
fun EmailInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        enabled = enabled,
        isError = isError,
        singleLine = true,
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.fill_mail),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
        },
        textStyle = TextStyle(
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium
        ),
        onValueChange = onValueChange,
        label = {
            Text(
                text = "Email",
                fontSize = 15.sp,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium
            )
        },
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    )
}
