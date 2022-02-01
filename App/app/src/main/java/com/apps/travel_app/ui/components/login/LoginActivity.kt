package com.apps.travel_app.ui.components.login

import FaIcons
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.apps.travel_app.MainActivity
import com.apps.travel_app.models.UserPreferences
import com.apps.travel_app.models.addUser
import com.apps.travel_app.models.addUserPeferences
import com.apps.travel_app.ui.components.Button
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.components.login.buttons.GoogleSignInButtonUI
import com.apps.travel_app.ui.components.login.models.User
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.errorMessage
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
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
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
    val db: FirebaseFirestore = Firebase.firestore
    private lateinit var functions: FirebaseFunctions
    private lateinit var secondCredential: AuthCredential
    var isNewUser: Boolean? = null

    var googleLog = true
    var logScreen = true
    var regScreen = false


    val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        var currentUser: FirebaseUser? = null
        var loginEntry = false
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                currentUser == null && !loginEntry
            }

        }
        auth = Firebase.auth
        auth.addAuthStateListener { auth ->
            Log.d("Tag---", "addAuthStateListener: ${auth.currentUser}")
        }

        currentUser = auth.currentUser

        functions = Firebase.functions


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("939417698638-a7pboqvvs0auptiglk9n17qd3re1b1dj.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        if (currentUser != null) {
            finalLoginPart()
        } else {
            loginEntry = true
            setContent {
                Travel_AppTheme {
                    Surface(color = primaryColor) {
                        LoginAndRegistrationUI()
                    }
                }
            }
        }
    }


    fun finalLoginPart(){

        //var inte: Uri? = null
        var inte: String? = null
        if(intent.getStringExtra("findTripID") != null) {
            inte = intent.getStringExtra("findTripID")
            //Log.i("AAA ", inte.toString())
        }
        val intent = Intent(this, MainActivity::class.java)
        if(inte != null) intent.putExtra("findTripID", inte)
        startActivity(intent)
        //intent.removeExtra("findTripID")
        //finish()
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


    @Composable
    private fun FacebookSignInButtonUI(context: Context) {
        val buttonFacebookLogin = LoginButton(context)
        buttonFacebookLogin.visibility = View.GONE
        buttonFacebookLogin.setPermissions("email", "public_profile")
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
                Text(text = "Login via Facebook", color = primaryColor, fontSize = textNormal)

            }
        }

    }


    private fun createAccount(email: String, password: String, displayName: String) {
        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let { auth.updateCurrentUser(it) }
                    Log.d("CurUsrBefore - ", auth.currentUser?.email.toString())
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("Registration info", "createUserWithEmail:success")
                                Log.d("DisplayName ", displayName)
                                //val user = auth.currentUser
                                val profileUpdates: UserProfileChangeRequest  = UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName).build()
                                auth.currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener(this ) {
                                        task -> if (task.isSuccessful){
                                    Log.d("Update user profile ", " displayName")
                                    /*Log.d("CurUsr - ", auth.currentUser?.email.toString())
                                    Log.d("CurUsr Name - ", auth.currentUser?.displayName.toString())
                                    Log.d("CurUsr Name CU - ", auth.currentUser?.displayName.toString())*/

                                    Toast.makeText(
                                        baseContext, "Account creation successful.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    addUserCheck(auth.currentUser, displayName)
                                    loginSuccess(auth.currentUser, this,)
                                    finalLoginPart()
                                }
                                else Log.d("Error ", "- Something doesn't work")
                                }
                            } else {
                                Log.w(
                                    "Registration error - Registration completed, sign-in failed- ",
                                    "createUserWithEmail:success; signInWithEmailAndPassword:failure",
                                    task.exception
                                )
                            }
                        }
                }
                else {
                    Log.w(
                        "Registration error - Registration failed - ",
                        "createUserWithEmail:failure",
                        task.exception
                    )
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
                    loginSuccess(user, this)
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
        val displayName = remember { mutableStateOf(TextFieldValue()) }
        val displayNameErrorState = remember { mutableStateOf(false) }

        val topColor = Color(0xFF00EEFF)

        val bottomColor = primaryColor


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
                            .weight(1f).semantics { testTag = "username" },
                        placeholder = {
                            Text(
                                "Email",
                                color = White,
                                fontSize = textNormal,
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
                    Text(text = "Required", color = Color.Yellow, fontSize = textNormal)
                }
                if (!logreg) {
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
                                .weight(1f).semantics { testTag = "username" },
                            placeholder = {
                                Text(
                                    "Username",
                                    fontSize = textNormal,
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
                            value = displayName.value,
                            onValueChange = {
                                if (displayNameErrorState.value) {
                                    displayNameErrorState.value = false
                                }
                                displayName.value = it
                            },
                            isError = displayNameErrorState.value
                        )
                    }
                    if (displayNameErrorState.value) {
                        Text(text = "Required", fontSize = textNormal, color = Color.Yellow)
                    }
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
                            .weight(1f).semantics { testTag = "password" },
                        placeholder = {
                            Text(
                                "Password",
                                color = White,
                                fontSize = textNormal,
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
                    Text(text = "Required", fontSize = textNormal, color = Color.Yellow)
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
                                fontSize = textNormal,
                                color = primaryColor,
                                modifier = Modifier.padding(5.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp).semantics { testTag = "login" },
                        background = White
                    )
                    Spacer(Modifier.padding(cardPadding))
                    Text(
                        text = "Don't have an account?",
                        fontSize = textNormal,
                        color = White,
                        textAlign = Center)
                    Text(
                        text = "Sign up here!",
                        fontSize = textNormal,
                        style = TextStyle(textDecoration = TextDecoration.Underline),
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
                                displayName.value.text.isEmpty() -> {
                                    displayNameErrorState.value = true
                                }
                                else -> {
                                    passwordErrorState.value = false
                                    emailErrorState.value = false
                                    displayNameErrorState.value = false
                                    Log.i("Account check", "Opening function")
                                    Log.i("Email try", email.value.text)
                                    Log.i("Pass try", password.value.text)
                                    createAccount(email.value.text, password.value.text, displayName.value.text)
                                    Log.i("Account check 2", "Results")
                                }
                            }

                        },
                        content = {
                            Text(
                                text = "Register",
                                fontSize = textNormal,
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
                        text = "Already have an account?",
                        color = White,
                        fontSize = textNormal,
                        textAlign = Center)
                    Text(
                        text = "Log in!",
                        style = TextStyle(textDecoration = TextDecoration.Underline),
                        color = White,
                        fontSize = textNormal,
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

                    FacebookSignInButtonUI(LocalContext.current)
                    Spacer(modifier = Modifier.padding(cardPadding))
                    GoogleSignInButtonUI(
                        "Login via Google",
                        "Logging in...",
                        onClicked = {
                            googleLog = true
                            googleSignIn()
                        })
                }
            }
        }
    }

    fun sharedPrefUserEdit(user:FirebaseUser?){
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        Log.d("NEWW ", isNewUser.toString())
        var editor = sharedPref.edit()
        if(isNewUser == false) {
            Log.d("NEWW ", user?.uid.toString())
            val userPref =
                user?.uid?.let {
                    db.collection("users").document(it).collection("user_preferences").document(user.uid).get().addOnSuccessListener{
                        Log.d("NEWW ", it.get("colourMode").toString())
                        editor.putBoolean("darkTheme", it.get("colourMode").toString().toBoolean())
                        editor.putBoolean("receiveNotification", it.get("notifications").toString().toBoolean())
                        editor.putString("realName", it.get("realName").toString())
                        editor.putString("realSurname",  it.get("realSurname").toString())
                        editor.commit()
                    }.addOnFailureListener(){ e-> Log.w("Add tag ", "Error.", e)
                    }
                }
        }
        editor.putString("userId", user?.uid)
        editor.putString("email", user?.email)
        editor.putString("displayName", user?.displayName)
        editor.commit()
    }

    private fun loginSuccess(user: FirebaseUser?,context: Context){
        // Update user preferences!!!

            sharedPrefUserEdit(user)
        //
        finalLoginPart()
    }

   /* private fun updateLocalUserPref(){

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        with(sharedPref.edit()) {
            putBoolean("receiveNotification", )
            apply()
        }
    }*/

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Google Login ", "signInWithCredential:success")
                    val user = auth.currentUser
                    Log.d("CURRENT USER 1", auth.currentUser.toString())
                    // Log.i("UserTT toString ", task.getResult().additionalUserInfo?.isNewUser().toString())
                    isNewUser = task.getResult().additionalUserInfo?.isNewUser()
                    if(isNewUser == true) addUserCheck(auth.currentUser, auth.currentUser?.displayName)
                    if(googleLog) loginSuccess(user, this)
                    else linkAccount(secondCredential)
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
                    //Log.i("UserTT toString ", task.getResult().additionalUserInfo?.isNewUser().toString())
                    isNewUser = task.getResult().additionalUserInfo?.isNewUser()
                    if(isNewUser == true) addUserCheck(auth.currentUser, auth.currentUser?.displayName)
                    else
                    loginSuccess(user,this)
                } else {
                    Log.w("Facebook Login - Token", "signInWithCredential:failure", task.exception)
                    /*com.google.android.material.snackbar.Snackbar.make(
                        View(this@LoginActivity),
                        "Authentication failed." + task.exception?.localizedMessage,
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                    ).show()*/
                    googleLog = false
                    secondCredential = credential
                    googleSignIn()

                }
            }
    }

    private fun addUserCheck(user: FirebaseUser?, displayName: String?){
        displayName?.let { it1 ->
            Firebase.auth.currentUser?.uid?.let {
                    it2 ->
                //Log.i("Detaljcici--- ", it1 + " " + it2)
                addUser(Firebase.firestore, it1, it2)
            }
        }
    }

    private fun linkAccount(credential: AuthCredential) {
        // Create EmailAuthCredential with email and password
        //val credential = EmailAuthProvider.getCredential("", "")
        // [START link_credential]
        Log.d("CURRENT USER 2", auth.currentUser.toString())
        auth.currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LinkCredential", "linkWithCredential:success")
                    val user = task.result?.user
                    loginSuccess(user,this)
                } else {
                    Log.w("LinkCredential", "linkWithCredential:failure", task.exception)
                    errorMessage(window.decorView.rootView, "Ops, authentication failed").show()
                }
            }
        // [END link_credential]
    }
}