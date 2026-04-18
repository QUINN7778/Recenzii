package com.sianov.stepan.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sianov.stepan.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    val scrollState = rememberScrollState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onAuthSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = if (isLoginMode) "С возвращением!" else "Создать аккаунт",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                AuthTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Логин",
                    icon = Icons.Default.Person,
                    enabled = !isLoading
                )

                if (!isLoginMode) {
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        enabled = !isLoading
                    )
                }

                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Пароль",
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordToggle = { passwordVisible = !passwordVisible },
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isLoginMode) {
                            viewModel.login(username, "", password)
                        } else {
                            viewModel.register(username, email, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading && username.isNotEmpty() && password.isNotEmpty()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (isLoginMode) "Войти" else "Зарегистрироваться")
                    }
                }

                TextButton(
                    onClick = { 
                        isLoginMode = !isLoginMode
                        viewModel.clearError()
                    },
                    enabled = !isLoading
                ) {
                    Text(if (isLoginMode) "Нет аккаунта? Создать" else "Уже есть аккаунт? Войти")
                }
                
                TextButton(
                    onClick = { onAuthSuccess() },
                    enabled = !isLoading
                ) {
                    Text("Продолжить как гость", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = onPasswordToggle, enabled = enabled) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            autoCorrectEnabled = true
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        enabled = enabled
    )
}
