package com.sianov.stepan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.sianov.stepan.data.model.AppItem
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.sianov.stepan.ui.screens.NewsScreen
import com.sianov.stepan.ui.screens.PosterScreen
import com.sianov.stepan.ui.screens.SettingsScreen
import com.sianov.stepan.ui.screens.PerformanceDetailScreen
import com.sianov.stepan.ui.screens.AuthScreen
import com.sianov.stepan.ui.screens.MyTheatreScreen
import com.sianov.stepan.ui.theme.ForStepanTheme
import com.sianov.stepan.ui.viewmodel.SettingsViewModel
import com.sianov.stepan.ui.viewmodel.AuthViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

sealed class Screen(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector,
    val index: Int
) {
    object Posters : Screen("posters", R.string.screen_posters, Icons.Default.DateRange, 0)
    object MyTheatre : Screen("my_theatre", R.string.screen_my_theatre, Icons.Default.Star, 1)
    object News : Screen("news", R.string.screen_news, Icons.AutoMirrored.Filled.List, 2)
    object Settings : Screen("settings", R.string.screen_settings, Icons.Default.Settings, 3)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val authViewModel: AuthViewModel = hiltViewModel()
            
            val isDarkThemePref by settingsViewModel.isDarkTheme.collectAsState()
            val fontSizeMultiplier by settingsViewModel.fontSizeMultiplier.collectAsState()
            val themeColorIndex by settingsViewModel.themeColorIndex.collectAsState()
            
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
            
            val darkTheme = isDarkThemePref ?: androidx.compose.foundation.isSystemInDarkTheme()
            
            val navController = rememberNavController()
            
            ForStepanTheme(
                darkTheme = darkTheme,
                fontSizeMultiplier = fontSizeMultiplier,
                themeColorIndex = themeColorIndex
            ) {
                NavHost(
                    navController = navController, 
                    startDestination = if (isLoggedIn) "main" else "auth"
                ) {
                    composable("auth") {
                        AuthScreen(
                            viewModel = authViewModel,
                            onAuthSuccess = {
                                navController.navigate("main") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("main") {
                        MainScreen(
                            onNavigateToDetail = { url ->
                                val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                                navController.navigate("detail/$encodedUrl")
                            },
                            onLogout = {
                                navController.navigate("auth") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(
                        route = "detail/{url}",
                        arguments = listOf(navArgument("url") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
                        val decodedUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
                        PerformanceDetailScreen(
                            url = decodedUrl,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    onNavigateToDetail: (String) -> Unit,
    onLogout: () -> Unit
) {
    val items = listOf(Screen.Posters, Screen.MyTheatre, Screen.News, Screen.Settings)
    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = pagerState.currentPage != 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(0)
        }
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = stringResource(screen.titleRes)) },
                        label = { Text(stringResource(screen.titleRes)) },
                        selected = pagerState.currentPage == screen.index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(screen.index)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(
                bottom = innerPadding.calculateBottomPadding(),
                start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
            ),
            userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> PosterScreen(onItemClick = { item -> onNavigateToDetail(item.detailUrl) })
                1 -> MyTheatreScreen(onNavigateToDetail = onNavigateToDetail)
                2 -> NewsScreen()
                3 -> SettingsScreen(onLogout = onLogout)
            }
        }
    }
}
