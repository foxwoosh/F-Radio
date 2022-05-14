package com.foxwoosh.radio.ui.player

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.foxwoosh.radio.ui.theme.FoxyRadioTheme
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun Player(
    viewModel: PlayerViewModel = viewModel()
) {

    val state by viewModel.stateFlow.collectAsState()

    when (state) {
        PlayerState.Loading -> PlayerLoading()
        is PlayerState.Done -> PlayerReady(done = state as PlayerState.Done)
    }
}

@Composable
fun PlayerLoading() {

}

@Composable
fun PlayerReady(done: PlayerState.Done) {
    var surfaceColor by remember {
        mutableStateOf(Color.Black)
    }

    Surface(
        color = surfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlideImage(
                imageModel = done.image,
                contentScale = ContentScale.Inside,
                contentDescription = "Avatar",
                modifier = Modifier
                    .fillMaxWidth()
                    .size(260.dp),
                requestListener = object : RequestListener<Drawable> {
                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource?.let {
                            Palette.Builder(it.toBitmap()).generate { palette ->
                                palette?.getDominantColor(Color.Black.value.toInt())?.let { colorInt ->
                                    surfaceColor = Color(colorInt)
                                }
                            }
                        }
                        return false
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ) = true
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = done.title,
                style = MaterialTheme.typography.h6,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = done.artist,
                style = MaterialTheme.typography.body1,
                color = Color.White
            )

            Row {
                Box(
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    FoxyRadioTheme {
        Player()
    }
}
