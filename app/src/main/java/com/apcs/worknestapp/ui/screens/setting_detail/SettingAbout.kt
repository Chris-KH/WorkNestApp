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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingAbout(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> OverviewPage()
                1 -> AboutUsPage()
            }
        }
    }
}

@Composable
fun OverviewPage() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Image(
                painter = painterResource(id = R.drawable.overview),
                contentDescription = "Overview illustration",
                modifier = Modifier
                    .size(220.dp)
                    .padding(8.dp)
            )
        }
        item {
            Text(
                text = "Work Nest is a productivity and collaboration application developed to help individuals and teams plan, organize, and manage daily tasks and larger projects. ",
                fontSize = 18.sp,
                lineHeight = 22.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        item {
            Text(
                text = "This app supports both personal task tracking and team-based project organization, with data stored securely in the cloud to enable seamless access from any device.",
                fontSize = 18.sp,
                lineHeight = 22.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun AboutUsPage() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Image(
                painter = painterResource(id = R.drawable.about),
                contentDescription = "about illustration",
                modifier = Modifier
                    .size(220.dp)
                    .padding(8.dp)
            )
        }

        item {
            Text(
                text = "About Us",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }


        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Lê Minh Nghĩa", fontWeight = FontWeight.Bold)
                Text("Email: lmnghia23@apcs.fitus.edu.vn")
                Text("VNUHCM - University of Science")
            }
        }
        item {Spacer(modifier = Modifier.height(22.dp))}
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Hoàng Tuấn Khoa", fontWeight = FontWeight.Bold)
                Text("Email: htkhoa23@apcs.fitus.edu.vn")
                Text("VNUHCM - University of Science")
            }
        }

    }
}
