package com.apcs.worknestapp.ui.screens.signup

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.domain.logic.Validator
import com.apcs.worknestapp.ui.components.inputfield.EmailInput
import com.apcs.worknestapp.ui.components.inputfield.NameInput
import com.apcs.worknestapp.ui.components.inputfield.PasswordConfirmInput
import com.apcs.worknestapp.ui.components.inputfield.PasswordInput
import kotlinx.coroutines.launch

@Composable
fun SignUpForm(
    onSuccess: suspend () -> Unit,
    onFailure: suspend (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val authViewModel = LocalAuthViewModel.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordConfirmError by remember { mutableStateOf<String?>(null) }

    var firstMount by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        firstMount = false
    }

    fun validData(): Boolean {
        if (
            name.isBlank() ||
            email.isBlank() ||
            password.isBlank() ||
            passwordConfirm.isBlank()
        ) return false
        if (
            nameError != null ||
            emailError != null ||
            passwordError != null ||
            passwordConfirmError != null
        ) return false
        if (password != passwordConfirm) return false
        return true
    }

    fun resetField() {
        email = ""
        name = ""
        password = ""
        passwordConfirm = ""

        nameError = null
        emailError = null
        passwordError = null
        passwordConfirmError = null
    }

    Column(
        modifier = modifier
            //.verticalScroll(scrollState)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailInput(
            value = email,
            isError = emailError != null,
            onValueChange = {
                email = it
                emailError =
                    if (it.isBlank()) "Don't leave this field blank"
                    else if (!Validator.isValidEmail(it)) "Invalid email format"
                    else null
            },
            enabled = !isLoading,
            modifier = Modifier.onFocusChanged { focusState ->
                if (!focusState.isFocused && !firstMount) {
                    emailError =
                        if (email.isBlank()) "Don't leave this field blank"
                        else if (!Validator.isValidEmail(email)) "Invalid email format"
                        else null
                }
            }
        )
        if (emailError != null) ErrorText(
            text = emailError as String,
            modifier = Modifier.fillMaxWidth()
        ) else Spacer(modifier = Modifier.height(8.dp))

        NameInput(
            value = name,
            isError = nameError != null,
            onValueChange = {
                name = it
                nameError =
                    if (it.isBlank()) "Don't leave this field blank"
                    else if (!Validator.isUserName(name)) "Invalid user name"
                    else null
            },
            enabled = !isLoading,
            modifier = Modifier.onFocusChanged { focusState ->
                if (!focusState.isFocused && !firstMount) {
                    nameError =
                        if (name.isBlank()) "Don't leave this field blank"
                        else if (!Validator.isUserName(name)) "Invalid user name"
                        else null
                }
            }
        )
        if (nameError != null) ErrorText(
            text = nameError as String,
            modifier = Modifier.fillMaxWidth()
        ) else Spacer(modifier = Modifier.height(8.dp))

        PasswordInput(
            value = password,
            enabled = !isLoading,
            isError = passwordError != null,
            onValueChange = {
                password = it
                passwordError =
                    if (it.isBlank()) "Don't leave this field blank"
                    else if (!Validator.isStrongPassword(it)) "Password contain spaces or to weak"
                    else null
            },
            modifier = Modifier.onFocusChanged { focusState ->
                if (!focusState.isFocused && !firstMount) {
                    passwordError =
                        if (password.isBlank()) "Don't leave this field blank"
                        else if (!Validator.isStrongPassword(password)) "Password contain spaces or to weak"
                        else null
                }
            }
        )
        if (passwordError != null) ErrorText(
            text = passwordError as String,
            modifier = Modifier.fillMaxWidth()
        ) else Spacer(modifier = Modifier.height(8.dp))

        PasswordConfirmInput(
            value = passwordConfirm,
            enabled = !isLoading,
            isError = passwordConfirmError != null,
            onValueChange = {
                passwordConfirm = it
                passwordConfirmError =
                    if (it.isBlank()) "Don't leave this field blank"
                    else if (it != password) "Confirm password does not match"
                    else null
            },
            modifier = Modifier.onFocusChanged { focusState ->
                if (!focusState.isFocused && !firstMount) {
                    passwordConfirmError =
                        if (passwordConfirm.isBlank()) "Don't leave this field blank"
                        else if (passwordConfirm != password) "Confirm password does not match"
                        else null
                }
            }
        )
        if (passwordConfirmError != null) ErrorText(
            text = passwordConfirmError as String,
            modifier = Modifier.fillMaxWidth()
        ) else Spacer(modifier = Modifier.height(8.dp))


        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (!validData()) return@Button

                coroutineScope.launch {
                    isLoading = true
                    val message = authViewModel.signUpWithEmailPassword(
                        email = email,
                        password = password,
                        name = name
                    )
                    resetField()
                    isLoading = false

                    if (message == null) onSuccess();
                    else onFailure(message)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            enabled = !isLoading,
            contentPadding = PaddingValues(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isLoading)
                Text(
                    "Sign Up",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            else CircularProgressIndicator(modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun ErrorText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}
