package com.apps.travel_app.ui.components.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
// import android.graphics.drawable.shapes.Shape
import androidx.compose.ui.graphics.Shape
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.apps.travel_app.ui.theme.Travel_AppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import com.apps.travel_app.R
import com.apps.travel_app.ui.components.BottomBarItem
import com.google.firebase.auth.FirebaseUser
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha.medium
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.apps.travel_app.ui.components.login.ui.theme.FacebookIntegrationTheme
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.apps.travel_app.ui.theme.Shapes

class LoginActivity : ComponentActivity() {

    companion object{
        private const val RL_SIGN = 111
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    //private lateinit var buttonFacebookLogin: LoginButton
    //private lateinit var callbackManager: CallbackManager

    var logScreen = true
    var regScreen = false

    val items = listOf(
        BottomBarItem.Email,
        BottomBarItem.Google,
        BottomBarItem.Facebook
    )
    val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val viewModel: LoginViewModel by viewModels()
        auth = Firebase.auth

        // Google sign in setup



        setContent {
            FacebookIntegrationTheme {
                Surface(color = MaterialTheme.colors.background) {
                //printHashKey(LocalContext.current)
                    val profileViewState by viewModel.profileViewState.observeAsState(ProfileViewState())
                    LoginAndRegistrationUI()
                }
            }
        }
    }

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//         Request id token if you intend to verify google user from your backend server
//        .requestIdToken(context.getString(R.string.backend_client_id))
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, signInOptions)
    }

    private val login = {
        LoginManager.getInstance().logIn(this, CallbackManager.Factory.create(), listOf("email"))
    }

    private val logout = {
        LoginManager.getInstance().logOut()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
            if (currentUser != null) {
                setContent {
                    FacebookIntegrationTheme {
                        Log.i("LOGG", "LOGGED IN ALREADYY")
                        updateUI(currentUser)
                    }
                    }
                }
        else {
            setContent {
                LoginAndRegistrationUI()
            }
        }
        //if(currentUser == null){
          /*  setContent {
                Travel_AppTheme {
                   // LoginAndRegistrationUI()
                }

            }*/
        //}
       /* else Toast.makeText(
            baseContext, "Already logged in!",
            Toast.LENGTH_SHORT
        ).show()*/
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
                        FacebookIntegrationTheme {LoginAndRegistrationUI()}}}, content = {
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
            composable("facebookScreen", content = { SampleView(
                profileViewState = profileViewState,
                login = login, logout = logout)})*/
            //composable("registerScreen")
        })

    }

/*fun printHashKey(context: Context) {
    try {
        val info: PackageInfo = context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
        for (signature in info.signatures) {
            val md: MessageDigest = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            val hashKey: String = String(Base64.encode(md.digest(), 0))
            Log.d("hashkey", "Hash Key: $hashKey")
        }
    } catch (e: NoSuchAlgorithmException) {
        Log.e("Error", "${e.localizedMessage}")
    } catch (e: Exception) {
        Log.e("Exception", "${e.localizedMessage}")
    }
}*/

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
            GoogleButtonPreview()
        }
        /*Button(
            onClick = {


            },
            content = {
                Text(text = "Google Sign In", color = Color.White)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan)
        )*/

        }
    }


    /*private fun signInGoogle() {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent()
        } catch (e: Exception) {
        }
    }*/

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

    @Composable
    fun GoogleButton(
        modifier: Modifier = Modifier,
        text: String = "Sign Up with Google",
        loadingText: String = "Creating Account...",
        icon: Int = R.drawable.ic_google_logo,
        shape: Shape = Shapes.medium,
        borderColor: Color = Color.LightGray,
        backgroundColor: Color = MaterialTheme.colors.surface,
        progressIndicatorColor: Color = MaterialTheme.colors.primary,
        onClicked: () -> Unit
    ) {
        var clicked by remember { mutableStateOf(false) }

        Surface(
            modifier = modifier.clickable { clicked = !clicked },
            shape = shape,
            border = BorderStroke(width = 1.dp, color = borderColor),
            color = backgroundColor
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        start = 12.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 12.dp
                    )
                    .animateContentSize(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = LinearOutSlowInEasing
                        )
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "Google Button",
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (clicked) loadingText else text)
                if (clicked) {
                    Spacer(modifier = Modifier.width(16.dp))
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(16.dp)
                            .width(16.dp),
                        strokeWidth = 2.dp,
                        color = progressIndicatorColor
                    )
                    onClicked()
                }
            }
        }
    }

    @Composable
    @Preview
    private fun GoogleButtonPreview() {
        GoogleButton(
            text = "Sign Up with Google",
            loadingText = "Creating Account...",
            onClicked = {}
        )
    }
}

/*
// @Preview(showBackground = true)
@Composable
fun RegistrationScreen(navigationController:NavController){
    val context = LocalContext.current
    val email = remember {mutableStateOf(TextFieldValue())}
    val emailErrorState = remember {mutableStateOf(false)}
    val passwordErrorState = remember {mutableStateOf(false)}
    val password = remember {mutableStateOf(TextFieldValue())}
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Red)) {
                append("Register")
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
                Text(text = "Login", color = Color.White)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
        )
        Spacer(Modifier.size(16.dp))

    }
        }
    }
*/
/*class AuthResultContract : ActivityResultContract<Int, Task<GoogleSignInAccount>?>() {
    override fun createIntent(context: Context, input: Int?): Intent =
        getGoogleSignInClient(context).signInIntent.putExtra("input", input)

    override fun parseResult(resultCode: Int, intent: Intent?): Task<GoogleSignInAccount>? {
        return when (resultCode) {
            Activity.RESULT_OK -> GoogleSignIn.getSignedInAccountFromIntent(intent)
            else -> null
        }
    }
}*/