package com.kushwahahardware.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.R
import com.kushwahahardware.ui.theme.DesignTokens
import com.kushwahahardware.ui.viewmodel.SessionState
import com.kushwahahardware.ui.viewmodel.SessionViewModel

@Composable
fun LoginScreen(viewModel: SessionViewModel = hiltViewModel()) {
    val sessionState by viewModel.sessionState.collectAsState()
    val loginError by viewModel.loginError.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.DeepTeal)
    ) {
        // Sticky Header / Background Graphic
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderSection()
        }

        // Bottom Sheet Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f) 
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(Color.White)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 40.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                when (sessionState) {
                        is SessionState.NeedsLogin -> {
                            LoginSection(
                                email = email,
                                onEmailChange = { email = it },
                                password = password,
                                onPasswordChange = { password = it },
                                loginError = loginError,
                                isLoading = isLoading,
                                onLogin = { viewModel.loginWithEmail(email, password) },
                                onRegister = { viewModel.setSessionState(SessionState.NeedsSignUp) },
                                onForgot = { viewModel.setSessionState(SessionState.ForgotPassword) }
                            )
                        }
                        is SessionState.NeedsSignUp -> {
                            RegistrationSection(
                                name = name,
                                onNameChange = { name = it },
                                email = email,
                                onEmailChange = { email = it },
                                password = password,
                                onPasswordChange = { password = it },
                                loginError = loginError,
                                isLoading = isLoading,
                                onSignUp = { viewModel.signUp(email, password, name) },
                                onBackToLogin = { viewModel.setSessionState(SessionState.NeedsLogin) },
                                isProfileCompletion = false,
                                phone = phone,
                                onPhoneChange = { phone = it }
                            )
                        }
                        is SessionState.NeedsProfileCompletion -> {
                            val state = sessionState as SessionState.NeedsProfileCompletion
                            RegistrationSection(
                                name = name,
                                onNameChange = { name = it },
                                email = state.email,
                                onEmailChange = {}, 
                                password = password,
                                onPasswordChange = { password = it },
                                loginError = loginError,
                                isLoading = isLoading,
                                onSignUp = {}, 
                                onBackToLogin = { viewModel.setSessionState(SessionState.NeedsLogin) },
                                isProfileCompletion = true,
                                phone = phone,
                                onPhoneChange = { phone = it },
                                onComplete = { viewModel.completeRegistration(name, state.email, phone) }
                            )
                        }
                        is SessionState.NeedsVerification -> {
                            val state = sessionState as SessionState.NeedsVerification
                            VerificationSection(
                                email = state.email,
                                otp = otp,
                                onOtpChange = { otp = it },
                                loginError = loginError,
                                isLoading = isLoading,
                                onVerify = { viewModel.verifyOtp(state.email, otp, state.nextAction) }
                            )
                        }
                        is SessionState.ForgotPassword -> {
                            ForgotSection(
                                email = email,
                                onEmailChange = { email = it },
                                loginError = loginError,
                                isLoading = isLoading,
                                onReset = { viewModel.startForgotPassword(email) },
                                onBackToLogin = { viewModel.setSessionState(SessionState.NeedsLogin) }
                            )
                        }
                        is SessionState.ForgotPasswordSuccess -> {
                            ForgotSuccessSection(
                                onBackToLogin = { viewModel.setSessionState(SessionState.NeedsLogin) }
                            )
                        }
                        else -> {
                            // Default or Loading
                            CircularProgressIndicator(color = DesignTokens.PremiumTeal)
                        }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.VpnKey, contentDescription = null, tint = DesignTokens.GoldAccent, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("KUSHWAHA", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
        Text("HARDWARE", color = DesignTokens.GoldAccent, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Premium ERP Solutions", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
    }
}

@Composable
private fun LoginSection(
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    loginError: String?, isLoading: Boolean,
    onLogin: () -> Unit, onRegister: () -> Unit, onForgot: () -> Unit
) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text("Welcome Back", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = DesignTokens.DeepTeal)
        Text("Sign in to continue", fontSize = 16.sp, color = DesignTokens.SubtitleGray, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))
    }
    
    PremiumTextField(
        value = email, 
        onValueChange = onEmailChange,
        hint = "Email Address",
        icon = Icons.Default.Email
    )
    
    PremiumTextField(
        value = password, 
        onValueChange = onPasswordChange,
        hint = "Password",
        icon = Icons.Default.Lock,
        isPassword = true
    )

    if (loginError != null) {
        Text(loginError, color = DesignTokens.ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp).fillMaxWidth(), textAlign = TextAlign.Start)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Text(
            "Forgot Password?", 
            modifier = Modifier.clickable(onClick = onForgot), 
            color = DesignTokens.PremiumTeal, 
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    PremiumButton(text = if (isLoading) "LOGGING IN..." else "LOGIN", onClick = onLogin, enabled = !isLoading)

    GoogleSignInButton(onClick = { /* Handle Google Sign In logs */ })

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
        Text("Don't have an account? ", color = DesignTokens.SubtitleGray, fontSize = 15.sp)
        Text("Register", color = DesignTokens.PremiumTeal, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.clickable(onClick = onRegister))
    }
}

@Composable
private fun VerificationSection(
    email: String, otp: String, onOtpChange: (String) -> Unit,
    loginError: String?, isLoading: Boolean, onVerify: () -> Unit
) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text("Verify Account", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = DesignTokens.DeepTeal)
        Text("Enter the 6-digit OTP sent to $email", color = DesignTokens.SubtitleGray, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))
    }

    PremiumTextField(
        value = otp, onValueChange = onOtpChange,
        hint = "6-Digit OTP",
        icon = Icons.Default.Lock,
        keyboardType = KeyboardType.Number,
        textAlign = TextAlign.Center
    )

    if (loginError != null) {
        Text(loginError, color = DesignTokens.ErrorRed, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
    }

    PremiumButton(text = if (isLoading) "VERIFYING..." else "VERIFY & CONTINUE", onClick = onVerify, enabled = otp.length == 6 && !isLoading)
}

@Composable
private fun RegistrationSection(
    name: String, onNameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    loginError: String?, isLoading: Boolean, onSignUp: () -> Unit, onBackToLogin: () -> Unit,
    isProfileCompletion: Boolean = false,
    phone: String = "", onPhoneChange: (String) -> Unit = {},
    onComplete: () -> Unit = {}
) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text(if (isProfileCompletion) "Complete Profile" else "Create Account", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = DesignTokens.DeepTeal)
        Text(if (isProfileCompletion) "Just a few more details" else "Sign up to get started", color = DesignTokens.SubtitleGray, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))
    }
    
    PremiumTextField(value = name, onValueChange = onNameChange, hint = "Full Name", icon = Icons.Default.Person)
    PremiumTextField(value = phone, onValueChange = onPhoneChange, hint = "Phone Number", icon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)

    if (!isProfileCompletion) {
        PremiumTextField(value = email, onValueChange = onEmailChange, hint = "Email Address", icon = Icons.Default.Email)
        PremiumTextField(value = password, onValueChange = onPasswordChange, hint = "Password", icon = Icons.Default.Lock, isPassword = true)
    }

    if (loginError != null) {
        Text(loginError, color = DesignTokens.ErrorRed, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
    }

    PremiumButton(text = if (isLoading) "PLEASE WAIT..." else if (isProfileCompletion) "FINISH SETUP" else "CREATE ACCOUNT", onClick = if (isProfileCompletion) onComplete else onSignUp, enabled = !isLoading)

    if (!isProfileCompletion) {
        Text("Back to Login", fontWeight = FontWeight.Bold, color = DesignTokens.PremiumTeal, fontSize = 15.sp, modifier = Modifier.clickable(onClick = onBackToLogin).padding(top = 8.dp))
    }
}

@Composable
private fun ForgotSection(
    email: String, onEmailChange: (String) -> Unit,
    loginError: String?, isLoading: Boolean, onReset: () -> Unit, onBackToLogin: () -> Unit
) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text("Reset Password", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = DesignTokens.DeepTeal)
        Text("Enter your email to receive a recovery link", color = DesignTokens.SubtitleGray, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))
    }

    PremiumTextField(value = email, onValueChange = onEmailChange, hint = "Email Address", icon = Icons.Default.Email)

    if (loginError != null) {
        Text(loginError, color = DesignTokens.ErrorRed, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
    }

    PremiumButton(text = if (isLoading) "SENDING..." else "SEND LINK", onClick = onReset, enabled = !isLoading)

    Text("Back to Login", fontWeight = FontWeight.Bold, color = DesignTokens.PremiumTeal, fontSize = 15.sp, modifier = Modifier.clickable(onClick = onBackToLogin).padding(top = 8.dp))
}

@Composable
private fun ForgotSuccessSection(onBackToLogin: () -> Unit) {
    Icon(
        Icons.Default.CheckCircle, 
        contentDescription = null, 
        tint = DesignTokens.PremiumTeal, 
        modifier = Modifier.size(80.dp).padding(bottom = 16.dp)
    )
    Text("Check Your Email", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = DesignTokens.DeepTeal)
    Text(
        "A password reset link has been sent to your email address. Please follow the link to reset your password.", 
        textAlign = TextAlign.Center, 
        color = DesignTokens.SubtitleGray, 
        fontSize = 16.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
    )
    
    PremiumButton(text = "BACK TO LOGIN", onClick = onBackToLogin)
}

@Composable
private fun GoogleSignInButton(onClick: () -> Unit) {
    Button(
        onClick = { /* Disabled for now */ },
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DesignTokens.InputBackground,
            contentColor = DesignTokens.SubtitleGray
        ),
        enabled = false
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Cloud, contentDescription = null, tint = Color(0xFF4285F4).copy(alpha = 0.5f)) 
            Spacer(modifier = Modifier.width(12.dp))
            Text("Google Sign-In (Coming Soon)", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

// New Custom Components
@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    textAlign: TextAlign = TextAlign.Start
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(hint, color = DesignTokens.InputHint) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = DesignTokens.PremiumTeal) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = LocalTextStyle.current.copy(textAlign = textAlign, color = Color.Black, fontSize = 16.sp),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = DesignTokens.InputBackground,
            unfocusedContainerColor = DesignTokens.InputBackground,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),
        singleLine = true
    )
}

@Composable
fun PremiumButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DesignTokens.PremiumTeal),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled
    ) {
        Text(text, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp, letterSpacing = 1.sp)
    }
}
