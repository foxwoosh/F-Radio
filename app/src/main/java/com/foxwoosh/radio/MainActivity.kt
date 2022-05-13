package com.foxwoosh.radio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.foxwoosh.radio.ui.theme.FoxyRadioTheme
import com.foxwoosh.radio.ui.theme.Tundora
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
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
//        Player(d)
    }
}