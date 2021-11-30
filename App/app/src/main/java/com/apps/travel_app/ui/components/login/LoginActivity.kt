package com.apps.travel_app.ui.components.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.apps.travel_app.ui.theme.Travel_AppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import com.apps.travel_app.R
import com.apps.travel_app.ui.components.BottomBarItem
import com.google.firebase.auth.FirebaseUser
import com.facebook.login.LoginManager
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.viewinterop.AndroidView
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import androidx.compose.foundation.ExperimentalFoundationApi
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
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
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        if (currentUser != null) {
            setContent {
                Travel_AppTheme {
                    Log.i("LOGG", "LOGGED IN ALREADYY")
                    updateUI(currentUser)
                }
            }
        }
        else {
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

    private val login = {
        LoginManager.getInstance().logIn(this, CallbackManager.Factory.create(), listOf("email"))
    }

    private val logout = {
        LoginManager.getInstance().logOut()
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
                    setContent{
                        updateUI(user)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Registration info", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Account creation failed failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    //updateUI(null)
                }
            }
        }


    private fun signIn(email: String, password: String) {
        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("SignIn info", "signInWithEmail:success")
                    val user = auth.currentUser
                    setContent{
                        updateUI(user)
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("SignIn Info", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
        // [END sign_in_with_email]
    }
        @Composable
        private fun updateUI(user: FirebaseUser?) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append("You are logged in " + user.toString())
                    }
                }, fontSize = 30.sp)
                Button(onClick = {
                    logout;
                    auth.signOut();
                    setContent {
                        Travel_AppTheme {LoginAndRegistrationUI()}
                    }}, content = {
                        Text(text = "Back to the login screen", color = Color.White)
                    })
                }

            }

    @Composable
    fun LoginAndRegistrationUI(){
        val navigationController = rememberNavController()
        val profileViewState by viewModel.profileViewState.observeAsState(ProfileViewState())
        NavHost(navController=navigationController, startDestination="loginScreen", builder=
        {
            composable("loginScreen", content = { EmailPassScreen(navigationController=navigationController, profileViewState = profileViewState,
                login = login, logout = logout, logScreen)
            })
            //composable("registrationScreen", content = { RegistrationScreen(navigationController=navigationController)})
            composable("registrationScreen", content = { EmailPassScreen(navigationController=navigationController, profileViewState = profileViewState,
                login = login, logout = logout, regScreen)})
            /*
            composable("facebookScreen", content = { })
            composable("googleScreen", content = { })
               */
        })

    }


@Composable
fun EmailPassScreen (navigationController: NavController, profileViewState: ProfileViewState, login: () -> Unit, logout: () -> Unit, logreg: Boolean) {
    val context = LocalContext.current
    val email = remember { mutableStateOf(TextFieldValue()) }
    val emailErrorState = remember { mutableStateOf(false) }
    val passwordErrorState = remember { mutableStateOf(false) }
    val password = remember { mutableStateOf(TextFieldValue()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Red)) {
                if(logreg) append("Sign in")
                else append ("Register")
            }
        }, fontSize = 30.sp)
        Spacer(Modifier.size(16.dp))
        OutlinedTextField(
            value = email.value,
            onValueChange = {
                if (emailErrorState.value) {
                    emailErrorState.value = false
                }
                email.value = it
            },
            isError = emailErrorState.value,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "Enter Email*")
            },
        )
        if (emailErrorState.value) {
            Text(text = "Required", color = Color.Red)
        }
        Spacer(Modifier.size(16.dp))
        val passwordVisibility = remember { mutableStateOf(true) }
        OutlinedTextField(
            value = password.value,
            onValueChange = {
                if (passwordErrorState.value) {
                    passwordErrorState.value = false
                }
                password.value = it
            },
            isError = passwordErrorState.value,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "Enter Password*")
            },
            /* trailingIcon = {
                IconButton(onClick = {
                    passwordVisibility.value = !passwordVisibility.value
                }) {
                    Icon(
                        ImageVector = if (passwordVisibility.value) ,
                        contentDescription = "visibility",
                        tint = Color.Red
                    )
                }
            },*/
            visualTransformation = if (passwordVisibility.value) PasswordVisualTransformation() else VisualTransformation.None
        )
        if (passwordErrorState.value) {
            Text(text = "Required", color = Color.Red)
        }
        Spacer(Modifier.size(16.dp))

        Log.i("LOGREG", logreg.toString())

        // --------------------------------------------------
        // LOGIN PART
        if (logreg == true) // login screen
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
                            /*Toast.makeText(
                                context,
                                "Logged in successfully",
                                Toast.LENGTH_SHORT
                            ).show()*/
                        }
                    }

                },
                content = {
                    Text(text = "Login", color = Color.White)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            )
            Spacer(Modifier.size(16.dp))
            Button(
                onClick = {

                    Toast.makeText(
                        context,
                        "Registration screen",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigationController.navigate("registrationScreen") {
                        popUpTo(navigationController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                content = {
                    Text(text = "Click here for Registration", color = Color.White)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
            )
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
                            /* Toast.makeText(
                                context,
                                "Logged in successfully",
                                Toast.LENGTH_SHORT
                            ).show()*/
                        }
                    }

                },
                content = {
                    Text(text = "Register", color = Color.White)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            )
            Spacer(Modifier.size(16.dp))
            Button(
                onClick = {

                    Toast.makeText(
                        context,
                        "Login screen",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigationController.navigate("loginScreen") {
                        popUpTo(navigationController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                content = {
                    Text(text = "Click here for login", color = Color.White)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
            )
        }
        // --------------------------------------------------
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            var buttonFacebookLogin = LoginButton(context)
            buttonFacebookLogin.setPermissions("email", "public_profile")
            AndroidView(
                factory = {

                    context -> buttonFacebookLogin.apply {
                    var callbackManager = CallbackManager.Factory.create()
                    buttonFacebookLogin.registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
                    override fun onSuccess(loginResult: LoginResult) {
                        Log.d("FacebookLogin - Success", "facebook:onSuccess:$loginResult")
                        handleFacebookAccessToken(loginResult.accessToken)
                    }

                    override fun onCancel() {
                        Log.d("FacebookLogin - Cancel", "facebook:onCancel")
                    }

                    override fun onError(error: FacebookException) {
                        Log.d("FacebookLogin - Error", "facebook:onError", error)
                    }
                }) }
                }
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = profileViewState.profile?.name ?: "Logged Out"
            )
            Spacer(modifier = Modifier.height(15.dp))
            //AuthScreen(AuthViewModel())
            val currentUser = auth.currentUser
            Log.i("USER LOGGED IN - ", currentUser.toString())
            Log.i("USER LOGGED IN NAME - ", currentUser?.displayName.toString())
            Log.i("USER LOGGED IN MAIL - ", currentUser?.email.toString())
            GoogleSignInButtonUI("Sign in with Google","Signing in...",onClicked = {googleSignIn()})
        }

        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Google Login ", "signInWithCredential:success")
                    val user = auth.currentUser
                    setContent {
                        updateUI(user)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Google Login ", "signInWithCredential:failure", task.exception)
                    //updateUI(null)
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
                    setContent{
                        updateUI(user)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Facebook Login - Token", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }
            }
    }

}
