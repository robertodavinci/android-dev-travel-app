package com.apps.travel_app.ui.components.login

import FaIcons
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apps.travel_app.MainActivity
import com.apps.travel_app.ui.components.Button
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.components.login.buttons.GoogleSignInButtonUI
import com.apps.travel_app.ui.theme.Travel_AppTheme
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.danger
import com.apps.travel_app.ui.theme.primaryColor
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.guru.fontawesomecomposelib.FaIcon
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
class LoginActivity : ComponentActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    var logScreen = true
    var regScreen = false

    val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        auth.addAuthStateListener { auth ->
            Log.d("Tag---", "addAuthStateListener: ${auth.currentUser}")
        }
        val currentUser = auth.currentUser


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("939417698638-a7pboqvvs0auptiglk9n17qd3re1b1dj.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            setContent {
                Travel_AppTheme {
                    Surface(color = MaterialTheme.colors.background) {
                        LoginAndRegistrationUI()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 9001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("Google login - ", "firebaseAuthWithGoogle:" + account.id)
                Log.d("Google login - ", "firebaseAuthWithGoogle:" + account.displayName)
                Log.d("Google login - ", "firebaseAuthWithGoogle:" + account.email)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("Google login - ", "Google sign in failed", e)
            }
        }
    }


    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 9001)
    }


    private fun createAccount(email: String, password: String) {
        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Registration info", "createUserWithEmail:success")
                    val user = auth.currentUser

                    Toast.makeText(
                        baseContext, "Account creation successful.",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.w("Registration info", "createUserWithEmail:failure", task.exception)
                    com.google.android.material.snackbar.Snackbar.make(
                        View(this@LoginActivity),
                        "Account creation failed: " + task.exception?.localizedMessage,
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                    ).show()

                }
            }
    }


    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("SignIn info", "signInWithEmail:success")
                    val user = auth.currentUser

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("SignIn Info", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    @Composable
    fun LoginAndRegistrationUI() {
        val navigationController = rememberNavController()
        val profileViewState by viewModel.profileViewState.observeAsState(ProfileViewState())
        NavHost(navController = navigationController, startDestination = "loginScreen", builder =
        {
            composable("loginScreen", content = {
                EmailPassScreen(
                    navigationController = navigationController,
                    profileViewState = profileViewState,
                    logScreen
                )
            })

            composable("registrationScreen", content = {
                EmailPassScreen(
                    navigationController = navigationController,
                    profileViewState = profileViewState,
                    regScreen
                )
            })
        })

    }


    @Composable
    fun EmailPassScreen(
        navigationController: NavController,
        profileViewState: ProfileViewState,
        logreg: Boolean
    ) {


        val context = LocalContext.current
        val email = remember { mutableStateOf(TextFieldValue()) }
        val emailErrorState = remember { mutableStateOf(false) }
        val passwordErrorState = remember { mutableStateOf(false) }
        val password = remember { mutableStateOf(TextFieldValue()) }

        val topColor = Color(0xFF00EEFF)

        val bottomColor = primaryColor

        val systemUiController = rememberSystemUiController()
        systemUiController.setSystemBarsColor(
            color = topColor
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            topColor,
                            bottomColor

                        )
                    )

                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(cardPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Heading(if (logreg) "Login" else "Sign up", color = White)
                Spacer(Modifier.padding(cardPadding))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            shape = RoundedCornerShape(100)
                            clip = true
                        }
                        .background(Color(0x88FFFFFF))
                ) {
                    Box(modifier = Modifier
                        .size(60.dp)
                        .graphicsLayer {
                            shape = RoundedCornerShape(100)
                            clip = true
                        }
                        .background(White)) {
                        FaIcon(
                            FaIcons.UserAstronaut,
                            tint = primaryColor,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    TextField(
                        colors = TextFieldDefaults.textFieldColors(
                            focusedIndicatorColor = Transparent,
                            disabledIndicatorColor = Transparent,
                            unfocusedIndicatorColor = Transparent,
                            backgroundColor = Transparent,
                        ),
                        modifier = Modifier
                            .height(60.dp)
                            .weight(1f),
                        placeholder = {
                            Text(
                                "Email",
                                color = White,
                                modifier = Modifier.alpha(0.5f)
                            )
                        },
                        trailingIcon = { FaIcon(FaIcons.Asterisk, tint = White, size = 12.dp) },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = White,
                            fontWeight = FontWeight.Bold
                        ),
                        value = email.value,
                        onValueChange = {
                            if (emailErrorState.value) {
                                emailErrorState.value = false
                            }
                            email.value = it
                        },
                        isError = emailErrorState.value,
                    )
                }
                if (emailErrorState.value) {
                    Text(text = "Required", color = danger)
                }
                Spacer(Modifier.padding(cardPadding))
                val passwordVisibility = remember { mutableStateOf(true) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            shape = RoundedCornerShape(100)
                            clip = true
                        }
                        .background(Color(0x88FFFFFF))
                ) {
                    Box(modifier = Modifier
                        .size(60.dp)
                        .graphicsLayer {
                            shape = RoundedCornerShape(100)
                            clip = true
                        }
                        .background(White)) {
                        FaIcon(
                            FaIcons.Lock,
                            tint = primaryColor,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    TextField(
                        colors = TextFieldDefaults.textFieldColors(
                            focusedIndicatorColor = Transparent,
                            disabledIndicatorColor = Transparent,
                            unfocusedIndicatorColor = Transparent,
                            backgroundColor = Transparent,
                        ),
                        modifier = Modifier
                            .height(60.dp)
                            .weight(1f),
                        placeholder = {
                            Text(
                                "Password",
                                color = White,
                                modifier = Modifier.alpha(0.5f)
                            )
                        },
                        trailingIcon = { FaIcon(FaIcons.Asterisk, tint = White, size = 12.dp) },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = White,
                            fontWeight = FontWeight.Bold
                        ),
                        value = password.value,
                        onValueChange = {
                            if (passwordErrorState.value) {
                                passwordErrorState.value = false
                            }
                            password.value = it
                        },
                        isError = passwordErrorState.value,
                        visualTransformation = if (passwordVisibility.value) PasswordVisualTransformation() else VisualTransformation.None
                    )
                }
                if (passwordErrorState.value) {
                    Text(text = "Required", color = Color.Red)
                }
                Spacer(Modifier.padding(cardPadding))

                Log.i("LOGREG", logreg.toString())

                // --------------------------------------------------
                // LOGIN PART
                if (logreg) // login screen
                {
                    Button(
                        onClick = {
                            when {
                                email.value.text.isEmpty() -> {
                                    emailErrorState.value = true
                                }
                                password.value.text.isEmpty() -> {
                                    passwordErrorState.value = true
                                }
                                else -> {
                                    passwordErrorState.value = false
                                    emailErrorState.value = false
                                    signIn(email.value.text, password.value.text)
                                }
                            }

                        },
                        content = {
                            Text(
                                text = "Login",
                                color = primaryColor,
                                modifier = Modifier.padding(5.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        background = White
                    )
                    Spacer(Modifier.padding(cardPadding))
                    Text(
                        text = "Don't you have an account? Sign up here!",
                        color = White,
                        textAlign = Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        navigationController.navigate("registrationScreen") {
                                            popUpTo(navigationController.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            })

                }
                // --------------------------------------------------
                // REGISTRATION PART

                else if (!logreg) // registration screen
                {
                    Button(
                        onClick = {
                            when {
                                email.value.text.isEmpty() -> {
                                    emailErrorState.value = true
                                }
                                password.value.text.isEmpty() -> {
                                    passwordErrorState.value = true
                                }
                                else -> {
                                    passwordErrorState.value = false
                                    emailErrorState.value = false
                                    Log.i("Account check", "Opening function")
                                    Log.i("Email try", email.value.text)
                                    Log.i("Pass try", password.value.text)
                                    createAccount(email.value.text, password.value.text)
                                    Log.i("Account check 2", "Results")
                                }
                            }

                        },
                        content = {
                            Text(
                                text = "Register",
                                color = primaryColor,
                                modifier = Modifier.padding(5.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        background = White
                    )
                    Spacer(Modifier.padding(cardPadding))
                    Text(
                        text = "Do you already have an account? Log in!",
                        color = White,
                        textAlign = Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        navigationController.navigate("loginScreen") {
                                            popUpTo(navigationController.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            })
                }
                Spacer(modifier = Modifier.padding(cardPadding))
                // --------------------------------------------------
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val buttonFacebookLogin = LoginButton(context)
                    buttonFacebookLogin.visibility = View.GONE
                    buttonFacebookLogin.setPermissions("email", "public_profile")
                    Button(onClick = { buttonFacebookLogin.callOnClick() }, background = White) {
                        Row(
                            modifier = Modifier
                                .animateContentSize(
                                    animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            FaIcon(FaIcons.Facebook, tint = primaryColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Get in", color = primaryColor)

                        }
                    }
                    AndroidView(
                        factory = {
                            buttonFacebookLogin.apply {
                                val callbackManager = CallbackManager.Factory.create()
                                buttonFacebookLogin.registerCallback(
                                    callbackManager,
                                    object : FacebookCallback<LoginResult> {
                                        override fun onSuccess(loginResult: LoginResult) {
                                            Log.d(
                                                "FacebookLogin - Success",
                                                "facebook:onSuccess:$loginResult"
                                            )
                                            handleFacebookAccessToken(loginResult.accessToken)
                                        }

                                        override fun onCancel() {
                                            Log.d("FacebookLogin - Cancel", "facebook:onCancel")
                                        }

                                        override fun onError(error: FacebookException) {
                                            Log.d(
                                                "FacebookLogin - Error",
                                                "facebook:onError",
                                                error
                                            )
                                        }
                                    })
                            }
                        }
                    )

                    Spacer(modifier = Modifier.padding(cardPadding))
                    GoogleSignInButtonUI(
                        "Get in",
                        "Trying to get in...",
                        onClicked = {
                            googleSignIn()
                        })
                }

            }
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Google Login ", "signInWithCredential:success")
                    val user = auth.currentUser
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.w("Google Login ", "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("Facebook Login - Token", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Facebook Login - Token", "signInWithCredential:success")
                    val user = auth.currentUser
                    Log.i("User toString ", user.toString())
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.w("Facebook Login - Token", "signInWithCredential:failure", task.exception)
                    com.google.android.material.snackbar.Snackbar.make(
                        View(this@LoginActivity),
                        "Authentication failed." + task.exception?.localizedMessage,
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                    ).show()
                }
            }
    }

}
