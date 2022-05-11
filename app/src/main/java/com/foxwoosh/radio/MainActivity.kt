package com.foxwoosh.radio

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.foxwoosh.radio.ui.theme.FoxyRadioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoxyRadioTheme {
                Conversation(messages = conversationSample)
            }
        }
    }
}

@Composable
fun ChatCell(message: Message) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(10.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "lol",
            modifier = Modifier
                .clip(CircleShape)
                .size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = message.author,
                color = MaterialTheme.colors.secondaryVariant,
                style = MaterialTheme.typography.subtitle2
            )
            Surface(
                shape = MaterialTheme.shapes.medium,
                elevation = 5.dp
            ) {
                Text(
                    text = message.message,
                    modifier = Modifier.padding(horizontal = 5.dp)
                )
            }
        }
    }
}

@Composable
fun Conversation(messages: List<Message>) {
    LazyColumn {
        items(messages) { message ->
            ChatCell(message = message)
        }
    }
}

data class Message(val author: String, val message: String)

val conversationSample = listOf(
    Message(
        "Colleague",
        "Test...Test...Test..."
    ),
    Message(
        "Colleague",
        "List of Android versions:\n" +
                "Android KitKat (API 19)\n" +
                "Android Lollipop (API 21)\n" +
                "Android Marshmallow (API 23)\n" +
                "Android Nougat (API 24)\n" +
                "Android Oreo (API 26)\n" +
                "Android Pie (API 28)\n" +
                "Android 10 (API 29)\n" +
                "Android 11 (API 30)\n" +
                "Android 12 (API 31)"
    ),
    Message(
        "Colleague",
        "I think Kotlin is my favorite programming language.\n" +
                "It's so much fun!"
    ),
    Message(
        "Colleague",
        "Searching for alternatives to XML layouts..."
    ),
    Message(
        "Colleague",
        "Hey, take a look at Jetpack Compose, it's great!\n" +
                "It's the Android's modern toolkit for building native UI." +
                "It simplifies and accelerates UI development on Android." +
                "Less code, powerful tools, and intuitive Kotlin APIs :)"
    ),
    Message(
        "Colleague",
        "It's available from API 21+ :)"
    ),
    Message(
        "Colleague",
        "Writing Kotlin for UI seems so natural, Compose where have you been all my life?"
    ),
    Message(
        "Colleague",
        "Android Studio next version's name is Arctic Fox"
    ),
    Message(
        "Colleague",
        "Android Studio Arctic Fox tooling for Compose is top notch ^_^"
    ),
    Message(
        "Colleague",
        "I didn't know you can now run the emulator directly from Android Studio"
    ),
    Message(
        "Colleague",
        "Compose Previews are great to check quickly how a composable layout looks like"
    ),
    Message(
        "Colleague",
        "Previews are also interactive after enabling the experimental setting"
    ),
    Message(
        "Colleague",
        "Have you tried writing build.gradle with KTS?"
    ),
)

@Preview(
    name = "Light Mode",
    showBackground = true
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun DefaultPreview() {
    FoxyRadioTheme {
        Conversation(messages = conversationSample)
    }
}