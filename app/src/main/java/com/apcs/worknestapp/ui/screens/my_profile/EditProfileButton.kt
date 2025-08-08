package com.apcs.worknestapp.ui.screens.my_profile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.theme.Inter

@Composable
fun EditProfileButton(
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(vertical = 0.dp),
        shape = RoundedCornerShape(50f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painterResource(R.drawable.fill_edit_pen),
            contentDescription = "Edit profile",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Edit profile",
            fontWeight = FontWeight.SemiBold,
            fontFamily = Inter,
            fontSize = 14.sp,
            lineHeight = 14.sp,
        )
    }
}
