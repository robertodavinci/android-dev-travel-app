package com.apps.travel_app.ui.components.login

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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
import com.facebook.CallbackManager
import com.google.firebase.auth.FirebaseUser
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apps.travel_app.ui.components.login.ui.theme.FacebookIntegrationTheme

class LoginActivity : ComponentActivity() {

    companion object{
        private const val RL_SIGN = 111
    }

    private lateinit var auth: FirebaseAuth

    val items = listOf(
        BottomBarItem.Email,
        BottomBarItem.Google,
        BottomBarItem.Facebook
    )
    val viewModel: LoginViewModel by viewModels()
    var fbLogin = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val viewModel: LoginViewModel by viewModels()
        auth = Firebase.auth
        setContent {
            FacebookIntegrationTheme {
                Surface(color = MaterialTheme.colors.background) {
                printHashKey(LocalContext.current)
                    val profileViewState by viewModel.profileViewState.observeAsState(ProfileViewState())
                    if (profileViewState != null) {fbLogin = true}
                    Log.i("NO LOGG", "LOGGED IN ALREADYY")
                    //SampleView(profileViewState, login, logout)
                    LoginAndRegistrationUI()
                }
            }
        }
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
        Log.i("NO LOGG", "LOGGED IN ALREADYY")
            if (currentUser != null || fbLogin != null) {
                setContent {
                    FacebookIntegrationTheme {
                        Log.i("LOGG", "LOGGED IN ALREADYY")
                        updateUI(currentUser)
                    }
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
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Registration info", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    //updateUI(null)
                }
            }
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
                Button(onClick = {logout; auth.signOut(); setContent {
                    FacebookIntegrationTheme {LoginAndRegistrationUI()}}}, content = {
                    Text(text = "Logout", color = Color.White)
                })
                }

            }

    @Composable
    fun LoginAndRegistrationUI(){
        val navigationController = rememberNavController()
        val profileViewState by viewModel.profileViewState.observeAsState(ProfileViewState())
        NavHost(navController=navigationController, startDestination="loginScreen", builder=
        {
            composable("loginScreen", content = { LoginScreen(navigationController=navigationController, profileViewState = profileViewState,
                login = login, logout = logout)})
            composable("registrationScreen", content = { RegistrationScreen(navigationController=navigationController)})
            composable("facebookScreen", content = { SampleView(
                profileViewState = profileViewState,
                login = login, logout = logout)})
            //composable("registerScreen")
        })

    }
    @Composable
    fun LoginScreen(navigationController:NavController, profileViewState: ProfileViewState, login: () -> Unit, logout: () -> Unit) {
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
                    append("Sign")
                }
                withStyle(style = SpanStyle(color = Color.Red)) {
                    append(" In")
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
                            Toast.makeText(
                                context,
                                "Logged in successfully",
                                Toast.LENGTH_SHORT
                            ).show()
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
                    Text(text = "Register", color = Color.White)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
            )
            Spacer(Modifier.size(16.dp))
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                /*CustomLoginButton(
                    profile = profileViewState.profile,
                    login = { login() },
                    logout = { logout() }
                )*/
                Spacer(modifier = Modifier.height(15.dp))

                WrappedLoginButton()

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = profileViewState.profile?.name ?: "Logged Out"
                )
            }
        }
    }

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


            /*
                Image(
                    modifier = Modifier.width(80.dp).height(80.dp),
                    painter = painterResource(id = R.drawable.facebook_icon),
                    contentDescription = null // decorative element
                )

                Image(
                    modifier = Modifier.width(80.dp).height(80.dp),
                    painter = painterResource(id = R.drawable.gmail_icon),
                    contentDescription = null // decorative element
                )
                Image(
                    modifier = Modifier.width(80.dp).height(80.dp),
                    painter = painterResource(id = R.drawable.google_icon),
                    contentDescription = null // decorative element
                )
*

             */
        }
            }
        }
    @Composable
    fun Greeting(name: String) {
        Text(text = "Hello $name!")
    }


    @Composable
    fun DefaultPreview() {
        Travel_AppTheme {
            Greeting("Android")
        }
    }


@Composable
fun SampleView(profileViewState: ProfileViewState, login: () -> Unit, logout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        /*CustomLoginButton(
            profile = profileViewState.profile,
            login = { login() },
            logout = { logout() }
        )*/

        Spacer(modifier = Modifier.height(15.dp))

        WrappedLoginButton()

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = profileViewState.profile?.name ?: "Logged Out"
        )
    }
}

fun printHashKey(context: Context) {
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
}


    /*public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        Log.d("EVOO", "TRY")
        if(currentUser != null){
            reload()
        }
        else Log.i("FAILED", "Success failed")
    }

    private fun reload() {
        Log.i("SUCCESS", "Successful login")
    }

    private fun createAccount(email: String, password: String) {
        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("DDD", "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("WWW", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
        // [END create_user_with_email]
    }

    private fun signIn(email: String, password: String) {
        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("DDD", "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("WWW", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
        // [END sign_in_with_email]
    }
    private fun sendEmailVerification() {
        // [START send_email_verification]
        val user = auth.currentUser!!
        user.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                // Email Verification sent
            }
        // [END send_email_verification]
    }

    private fun updateUI(user: FirebaseUser?) {

    }
}
*/


// Create and launch sign-in intent
/*
fun CreatLogin(){
    val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build()
        /*AuthUI.IdpConfig.PhoneBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build(),
        AuthUI.IdpConfig.FacebookBuilder().build(),
        AuthUI.IdpConfig.TwitterBuilder().build())*/)

    val signInIntent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .build()
    //RL_SIGN
    signInLauncher
}


private val signInLauncher = registerForActivityResult(
    FirebaseAuthUIActivityResultContract()
) { res ->
    this.onSignInResult(res)
}

fun registerForActivityResult(
    firebaseAuthUIActivityResultContract: FirebaseAuthUIActivityResultContract,
    any: Any
): Any {
    TODO("Not yet implemented")
}

private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
    val response = result.idpResponse
    if (result.resultCode == RESULT_OK) {
        // Successfully signed in
        val user = FirebaseAuth.getInstance().currentUser
        // ...
    } else {
        // Sign in failed. If response is null the user canceled the
        // sign-in flow using the back button. Otherwise check
        // response.getError().getErrorCode() and handle the error.
        // ...
    }
}
*/
