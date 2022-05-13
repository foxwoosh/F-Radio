package com.foxwoosh.radio

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.foxwoosh.radio.ui.theme.FoxyRadioTheme
import com.foxwoosh.radio.ui.theme.Tundora

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoxyRadioTheme {
                Player()
            }
        }
    }
}

@Composable
fun Player() {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        TopAppBar(
            title = { Text(text = "Player") },
            actions = {
                IconButton(onClick = { menuExpanded = !menuExpanded }) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            onClick = { menuExpanded = false }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "Item"
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "First item", Modifier.fillMaxSize())
                        }
                    }
                }
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.image_avril),
                contentDescription = "Avatar",
                modifier = Modifier
                    .fillMaxWidth()
                    .size(260.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "My Happy Ending",
                style = MaterialTheme.typography.h6,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Avril Lavigne",
                style = MaterialTheme.typography.body1,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = realProgress,
                onValueChange = { realProgress = it },
                colors = SliderDefaults.colors(
                    thumbColor = Tundora,
                    activeTrackColor = Tundora
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true
)
//@Preview(
//    uiMode = Configuration.UI_MODE_NIGHT_YES,
//    showBackground = true,
//    name = "Dark Mode"
//)
@Composable
fun DefaultPreview() {
    FoxyRadioTheme {
        Player(1f)
    }
}