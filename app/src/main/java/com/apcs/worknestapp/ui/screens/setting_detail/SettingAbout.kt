package com.apcs.worknestapp.ui.screens.setting_detail

import androidx.compose.foundation.Image
import com.apcs.worknestapp.ui.components.topbar.CustomTopBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingAbout(
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { page ->
        when(page) {
            0 -> OverviewPage()
            1 -> AboutUsPage()
        }
    }
}

@Composable
fun OverviewPage() {
    val textStyle = TextStyle(
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 32.dp, horizontal = 20.dp)
    ) {
        item {
            Image(
                painter = painterResource(R.drawable.overview),
                contentDescription = "Overview illustration",
                modifier = Modifier.size(280.dp)
            )
        }
        item {
            Text(
                text = "Work Nest is a productivity and collaboration application developed to help individuals and teams plan, organize, and manage daily tasks and larger projects. ",
                style = textStyle,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            Text(
                text = "This app supports both personal task tracking and team-based project organization, with data stored securely in the cloud to enable seamless access from any device.",
                style = textStyle,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun AboutUsPage() {
    val nameTextStyle = TextStyle(
        fontSize = 18.sp, lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
    )
    val subContentStyle = TextStyle(
        fontSize = 16.sp, lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 32.dp, horizontal = 20.dp)
    ) {
        item {
            Image(
                painter = painterResource(R.drawable.about),
                contentDescription = "About illustration",
                modifier = Modifier.size(280.dp)
            )
        }
        item {
            Text(
                text = "About Us",
                fontSize = 24.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Lê Minh Nghĩa", style = nameTextStyle)
                Text("Email: lmnghia23@apcs.fitus.edu.vn", style = subContentStyle)
                Text("VNU-HCM - University of Science", style = subContentStyle)
            }
        }
        item { Spacer(modifier = Modifier.height(22.dp)) }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Hoàng Tuấn Khoa", style = nameTextStyle)
                Text("Email: htkhoa23@apcs.fitus.edu.vn", style = subContentStyle)
                Text("VNU-HCM - University of Science", style = subContentStyle)
            }
        }

    }
}
