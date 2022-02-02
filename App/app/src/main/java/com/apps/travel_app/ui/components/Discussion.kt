package com.apps.travel_app.ui.components
/**
 * Composable function that holds all of the elements of the message field, that allows
 * users to communicate and comment on trips. Features both message creation and
 * message display.
 */
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.apps.travel_app.R
import com.apps.travel_app.models.Message
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.sendPostRequest
import com.apps.travel_app.user
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.guru.fontawesomecomposelib.FaIcon
import com.skydoves.landscapist.glide.GlideImage
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun MessageField(message: Message? = null, tripId: String, onAdd: (Message) -> Unit) {
    var newMessageText by remember { mutableStateOf("") }
    TextField(
        value = newMessageText, onValueChange = { newMessageText = it },
        shape = RoundedCornerShape(cardRadius / 2),
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(cardPadding / 2),
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            backgroundColor = MaterialTheme.colors.onBackground,
        ),
        trailingIcon = {
            IconButton(
                onClick = {
                    val newMessage = createMessage(newMessageText)
                    newMessage.parent = message?.id
                    newMessage.entityId = tripId
                    Thread {
                        val gson = Gson()
                        val request = gson.toJson(newMessage)
                        val tripText = sendPostRequest(
                            request,
                            action = "newMessage"
                        ) // NON-NLS
                        if (!tripText.isNullOrEmpty()) {
                            newMessage.id = tripText
                            newMessageText = String()
                            FirebaseMessaging.getInstance().subscribeToTopic("trip$tripId")
                            onAdd(newMessage)
                        }

                    }.start()


                }) {
                FaIcon(FaIcons.PaperPlaneRegular, tint = MaterialTheme.colors.surface)
            }
        },
        textStyle = TextStyle(
            color = MaterialTheme.colors.surface,
            fontWeight = FontWeight.Bold
        ),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        maxLines = 20
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MessageCard(message: Message, level: Int = 0, tripId: String) {
    fun epochToDate(netDate: Long): String {
        if (netDate > 0) {
            val simpleDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = Date(netDate)
            return simpleDateFormat.format(date)
        }
        return ""
    }

    val open = remember { mutableStateOf(false) }
    var write by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf(*message.messages.toTypedArray()) }

    Column(
        Modifier.padding(
            top = cardPadding / 2,
            end = cardPadding / 2,
            bottom = cardPadding / 2,
            start = cardPadding / 1.5f * level
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = cardElevation / 2,
            shape = RoundedCornerShape(cardRadius / 2),
            backgroundColor = MaterialTheme.colors.onBackground
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlideImage(
                    imageModel = R.mipmap.icon,
                    contentDescription = "",
                    modifier = Modifier
                        .padding(cardPadding)
                        .width(40.dp)
                        .height(40.dp)
                        .graphicsLayer {
                            shape = RoundedCornerShape(100)
                            clip = true
                        }
                )
                Column(
                    modifier = Modifier
                        .padding(cardPadding)
                        .weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = epochToDate(message.time),
                            fontSize = textExtraSmall,
                            color = MaterialTheme.colors.surface,
                        )
                    }
                    Text(
                        text = message.username,
                        color = MaterialTheme.colors.surface,
                        fontSize = textNormal,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = message.body,
                        fontSize = textNormal,
                        lineHeight = textSmall / 2,
                        color = MaterialTheme.colors.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                        //.heightIn(0.dp, maxHeight.dp)
                    )
                }
                if (level < 3) {
                    Button(onClick = {
                        open.value = true
                        write = !write
                    }, background = Color.Transparent) {
                        FaIcon(
                            FaIcons.EditRegular,
                            tint = MaterialTheme.colors.surface,
                            size = 20.dp
                        )
                    }
                }
            }

        }
        if (messages.size > 0) {
            Button(
                onClick = {
                    open.value = !open.value
                },
                background = Color.Transparent
            ) {
                Text(
                    "Other " + message.messages.size,
                    color = primaryColor,
                    fontSize = textSmall,
                    modifier = Modifier.padding(cardPadding / 3)
                )
            }
        }
        if (open.value) {

            Column(
                Modifier.padding(
                    cardPadding / 2
                )
            ) {
                messages.forEach {
                    MessageCard(it, level + 1, tripId)
                }

            }
        }
        if (write) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = {
                    write = false
                },
            ) {

                    MessageField(message = message, tripId = tripId) {
                        messages.add(0, it)
                        write = false
                    }


            }
        }
    }
}

fun createMessage(text: String): Message {
    val message = Message()
    message.body = text
    message.username = user.displayName ?: String()
    message.userId = user.id
    message.time = System.currentTimeMillis()
    return message
}