package com.apcs.worknestapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.R
import com.apcs.worknestapp.domain.logic.DateFormater
import com.apcs.worknestapp.ui.screens.Screen
import com.apcs.worknestapp.ui.theme.Inter
import com.apcs.worknestapp.ui.theme.Roboto
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val authViewModel = LocalAuthViewModel.current
    val profile = authViewModel.profile.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profile.value?.avatar)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.fade_image),
                    error = painterResource(R.drawable.fade_avatar_fallback),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.Medium,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(80.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = profile.value?.name ?: "Anonymous",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = profile.value?.email ?: "",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate(Screen.EditProfile.route) },
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painterResource(R.drawable.fill_edit_pen),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit profile",
                    fontFamily = Inter,
                    fontSize = 14.sp,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "About me",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = profile.value?.bio ?: "_____",
                        fontFamily = Roboto,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Joined since",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text =
                            if (profile.value?.createdAt != null)
                                DateFormater.format(profile.value?.createdAt!!.toDate())
                            else "No information",
                        fontFamily = Roboto,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                    )
                }
            }


            /*     repeat(100) {
                     Button(
                         onClick = {
                             coroutineScope.launch {
                                 val isSuccess = authViewModel.signOut()
                                 if (isSuccess) {
                                     navController.navigate(Screen.Login.route) {
                                         popUpTo(0) {
                                             inclusive = true
                                         }
                                     }
                                 } else {
                                     snackbarHost.showSnackbar(
                                         message = "Fail: Sign out fail, try again late",
                                         withDismissAction = true,
                                     )
                                 }
                             }
                         }
                     ) {
                         Text(
                             text = "Log out",
                             fontFamily = Inter,
                             modifier = Modifier
                         )
                     }
                 }
                 */
        }
    }
}
