package com.apcs.worknestapp.ui.screens.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.ui.components.inputfield.EmailInput
import com.apcs.worknestapp.ui.components.inputfield.PasswordInput
import com.apcs.worknestapp.ui.theme.Poppins
import kotlinx.coroutines.launch

@Composable
fun LoginForm(
    onSubmit: () -> Unit,
    onSuccess: suspend () -> Unit,
    onFailure: suspend () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val authViewModel = LocalAuthViewModel.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailInput(
            value = email,
            onValueChange = {
                email = it
            },
            enabled = enabled,
        )
        PasswordInput(
            value = password,
            onValueChange = {
                password = it
            },
            enabled = enabled,
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) return@Button
                coroutineScope.launch {
                    onSubmit()
                    val isSuccess = authViewModel.login(email.trim(), password.trim())
                    email = ""
                    password = ""

                    if (isSuccess) onSuccess();
                    else onFailure()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            enabled = enabled,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (enabled)
                Text(
                    "Login",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            else CircularProgressIndicator(modifier = Modifier.size(18.dp))
        }
    }
}
