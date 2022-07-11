package com.foxwoosh.radio.ui.settings

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foxwoosh.radio.R
import com.foxwoosh.radio.adjustBrightness
import com.foxwoosh.radio.domain.models.LyricsReportState
import com.foxwoosh.radio.ui.settings.models.LyricsReportUiModel
import com.foxwoosh.radio.ui.settings.models.UserReportsUiState
import com.foxwoosh.radio.ui.settings.viewmodels.UserReportsViewModel
import com.foxwoosh.radio.ui.singleCondition
import com.foxwoosh.radio.ui.theme.*
import java.lang.Integer.max
import java.text.SimpleDateFormat

private val DetailsItemBackground = CodGray.adjustBrightness(1.5f)

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun UserReportsScreen(
    navigateBack: () -> Unit
) {
    val viewModel = hiltViewModel<UserReportsViewModel>()

    val reportsState by viewModel.reportsState.collectAsState()

    Surface(color = CodGray) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.settings_item_user_reports)) },
                    navigationIcon = {
                        IconButton(onClick = navigateBack) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    elevation = 0.dp,
                    backgroundColor = Color.Transparent
                )
            },
            modifier = Modifier
                .systemBarsPadding()
                .imePadding()
        ) {
            ReportsContent(state = reportsState)
        }
    }
}

@Composable
fun ReportsContent(state: UserReportsUiState) {
    when (state) {
        is UserReportsUiState.Loading -> Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
        is UserReportsUiState.Empty -> Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(dp32)
        ) {
            Text(
                text = stringResource(R.string.settings_user_reports_empty_title),
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(dp8))
            Text(
                text = stringResource(R.string.settings_user_reports_empty_description),
                color = White_70,
                textAlign = TextAlign.Center
            )
        }
        is UserReportsUiState.Ready -> {
            var openedReportID by remember { mutableStateOf("") }

            LazyColumn(
                contentPadding = WindowInsets.navigationBars.asPaddingValues()
            ) {
                items(state.list) { report ->
                    val currentReportOpened = openedReportID == report.reportID

                    ReportItem(
                        report = report,
                        opened = currentReportOpened,
                        action = {
                            openedReportID = if (currentReportOpened) {
                                ""
                            } else {
                                report.reportID
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReportItem(
    report: LyricsReportUiModel,
    opened: Boolean,
    action: () -> Unit
) {
    Column(
        Modifier
            .clickable(onClick = action)
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dp16),
            modifier = Modifier.padding(horizontal = dp16, vertical = dp12)
        ) {
            Icon(
                imageVector = when (report.state) {
                    LyricsReportState.SUBMITTED -> Icons.Filled.Done
                    LyricsReportState.SOLVED -> Icons.Filled.DoneAll
                    LyricsReportState.DECLINED -> Icons.Filled.Close
                    null -> Icons.Filled.ErrorOutline
                },
                contentDescription = "Report item icon"
            )
            Text(text = "${report.title} - ${report.artist}")
        }

        if (opened) {
            val locale = LocalConfiguration.current.locale
            val sdf = remember(locale) {
                SimpleDateFormat(
                    "dd.MM.yyyy HH:mm",
                    locale
                )
            }

            report.state?.let { state ->
                ReportDetailsRow(
                    key = stringResource(R.string.settings_user_reports_details_key_state),
                    value = stringResource(
                        when (state) {
                            LyricsReportState.SUBMITTED -> R.string.settings_user_reports_details_key_state_submitted
                            LyricsReportState.SOLVED -> R.string.settings_user_reports_details_key_state_solved
                            LyricsReportState.DECLINED -> R.string.settings_user_reports_details_key_state_declined
                        }
                    )
                )
            }
            if (report.comment.isNotEmpty()) {
                ReportDetailsRow(
                    key = stringResource(R.string.settings_user_reports_details_key_comment),
                    value = report.comment
                )
            }
            ReportDetailsRow(
                key = stringResource(R.string.settings_user_reports_details_key_created),
                value = sdf.format(report.createdAt)
            )
            report.updatedAt?.let {
                ReportDetailsRow(
                    key = stringResource(R.string.settings_user_reports_details_key_updated),
                    value = sdf.format(it)
                )
            }
        }
    }
}

@Composable
private fun ReportDetailsRow(
    key: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val density = LocalDensity.current
        var keyHeight by remember { mutableStateOf(0) }
        var valueHeight by remember { mutableStateOf(0) }

        val actualHeight = max(keyHeight, valueHeight)

        Text(
            text = key,
            modifier = Modifier
                .weight(0.5f)
                .padding(1.dp)
                .background(DetailsItemBackground)
                .padding(horizontal = dp16, vertical = dp12)
                .singleCondition(actualHeight > 0) {
                    height(
                        with(density) {
                            actualHeight.toDp()
                        }
                    )
                }
                .onSizeChanged { keyHeight = it.height }
        )

        Text(
            text = value,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(0.5f)
                .padding(1.dp)
                .background(DetailsItemBackground)
                .padding(horizontal = dp16, vertical = dp12)
                .singleCondition(actualHeight > 0) {
                    height(
                        with(density) {
                            actualHeight.toDp()
                        }
                    )
                }
                .onSizeChanged { valueHeight = it.height }
        )
    }
}