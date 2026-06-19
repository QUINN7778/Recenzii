package com.sianov.stepan

import android.os.Bundle
import android.os.Build
import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.sianov.stepan.ui.screens.*
import com.sianov.stepan.ui.theme.ForStepanTheme
import com.sianov.stepan.ui.viewmodel.SettingsViewModel
import com.sianov.stepan.ui.viewmodel.AuthViewModel
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val titleRes: Int, val icon: ImageVector, val index: Int) {
    object Posters : Screen("posters", R.string.screen_posters, Icons.Default.DateRange, 0)
    object MyTheatre : Screen("my_theatre", R.string.screen_my_theatre, Icons.Default.Star, 1)
    object News : Screen("news", R.string.screen_news, Icons.AutoMirrored.Filled.List, 2)
    object Settings : Screen("settings", R.string.screen_settings, Icons.Default.Settings, 3)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Результат можно не обрабатывать тостом, если пользователь так хочет
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Создаем каналы уведомлений
        com.sianov.stepan.utils.NotificationHelper(this)

        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val authViewModel: AuthViewModel = hiltViewModel()
            
            val isDarkThemePref by settingsViewModel.isDarkTheme.collectAsState(initial = false)
            val fontSizeMultiplier by settingsViewModel.fontSizeMultiplier.collectAsState(initial = 1.0f)
            val themeColorIndex by settingsViewModel.themeColorIndex.collectAsState(initial = 0)
            val dynamicColor by settingsViewModel.dynamicColor.collectAsState(initial = false)
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

            val darkTheme = isDarkThemePref ?: androidx.compose.foundation.isSystemInDarkTheme()
            val navController = rememberNavController()

            ForStepanTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColor,
                fontSizeMultiplier = fontSizeMultiplier,
                themeColorIndex = themeColorIndex
            ) {
                NavHost(navController = navController, startDestination = if (isLoggedIn) "main" else "auth") {
                    composable("auth") {
                        AuthScreen(authViewModel, onAuthSuccess = {
                            navController.navigate("main") { popUpTo("auth") { inclusive = true } }
                        })
                    }
                    composable("main") {
                        MainScreen(
                            onNavigateToDetail = { item ->
                                val encodedUrl = URLEncoder.encode(item.detailUrl, StandardCharsets.UTF_8.toString())
                                val encodedDate = URLEncoder.encode(item.date, StandardCharsets.UTF_8.toString())
                                navController.navigate("detail/$encodedUrl/$encodedDate")
                            },
                            onNavigateToWebView = { url ->
                                val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                                navController.navigate("webview/$encodedUrl")
                            },
                            onNavigateToNewsDetail = { news ->
                                val encodedUrl = URLEncoder.encode(news.detailUrl, StandardCharsets.UTF_8.toString())
                                val encodedTitle = URLEncoder.encode(news.title, StandardCharsets.UTF_8.toString())
                                val encodedDate = URLEncoder.encode(news.date, StandardCharsets.UTF_8.toString())
                                val encodedImg = URLEncoder.encode(news.imageUrl, StandardCharsets.UTF_8.toString())
                                navController.navigate("news_detail/$encodedUrl/$encodedTitle/$encodedDate/$encodedImg")
                            },
                            onNavigateToAbout = {
                                navController.navigate("about")
                            },
                            onNavigateToPermissions = {
                                navController.navigate("permissions")
                            },
                            onNavigateToAppearance = {
                                navController.navigate("appearance")
                            },
                            onLogout = {
                                navController.navigate("auth") { popUpTo("main") { inclusive = true } }
                            }
                        )
                    }
                    composable(
                        route = "detail/{url}/{date}",
                        arguments = listOf(
                            navArgument("url") { type = NavType.StringType },
                            navArgument("date") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val decodedUrl = URLDecoder.decode(backStackEntry.arguments?.getString("url") ?: "", StandardCharsets.UTF_8.toString())
                        val decodedDate = URLDecoder.decode(backStackEntry.arguments?.getString("date") ?: "", StandardCharsets.UTF_8.toString())
                        PerformanceDetailScreen(
                            url = decodedUrl,
                            performanceDate = decodedDate,
                            onBack = { navController.popBackStack() },
                            onNavigateToWebView = { targetUrl ->
                                val encodedUrl = URLEncoder.encode(targetUrl, StandardCharsets.UTF_8.toString())
                                navController.navigate("webview/$encodedUrl")
                            }
                        )
                    }
                    composable(
                        route = "webview/{url}",
                        arguments = listOf(navArgument("url") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val decodedUrl = URLDecoder.decode(backStackEntry.arguments?.getString("url") ?: "", StandardCharsets.UTF_8.toString())
                        WebViewScreen(url = decodedUrl, onBack = { navController.popBackStack() })
                    }
                    composable(
                        route = "news_detail/{url}/{title}/{date}/{imageUrl}",
                        arguments = listOf(
                            navArgument("url") { type = NavType.StringType },
                            navArgument("title") { type = NavType.StringType },
                            navArgument("date") { type = NavType.StringType },
                            navArgument("imageUrl") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val url = URLDecoder.decode(backStackEntry.arguments?.getString("url") ?: "", StandardCharsets.UTF_8.toString())
                        val title = URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "", StandardCharsets.UTF_8.toString())
                        val date = URLDecoder.decode(backStackEntry.arguments?.getString("date") ?: "", StandardCharsets.UTF_8.toString())
                        val imageUrl = URLDecoder.decode(backStackEntry.arguments?.getString("imageUrl") ?: "", StandardCharsets.UTF_8.toString())
                        
                        NewsDetailScreen(
                            url = url,
                            title = title,
                            date = date,
                            imageUrl = imageUrl,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("about") {
                        AboutScreen(onBack = { navController.popBackStack() })
                    }
                    composable("permissions") {
                        PermissionsScreen(onBack = { navController.popBackStack() })
                    }
                    composable("appearance") {
                        AppearanceScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
@Composable
fun MainScreen(
    onNavigateToDetail: (com.sianov.stepan.data.model.AppItem) -> Unit,
    onNavigateToWebView: (String) -> Unit,
    onNavigateToNewsDetail: (com.sianov.stepan.data.model.AppItem) -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onLogout: () -> Unit
) {
    val items = listOf(Screen.Posters, Screen.MyTheatre, Screen.News, Screen.Settings)
    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, null) },
                        label = { Text(stringResource(screen.titleRes)) },
                        selected = pagerState.currentPage == screen.index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(screen.index) } }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) { page ->
            when (page) {
                0 -> PosterScreen(onItemClick = { item -> onNavigateToDetail(item) })
                1 -> MyTheatreScreen(onNavigateToDetail = onNavigateToDetail)
                2 -> NewsScreen(onNewsClick = onNavigateToNewsDetail)
                3 -> SettingsScreen(
                    onLogout = onLogout, 
                    onNavigateToAbout = onNavigateToAbout,
                    onNavigateToPermissions = onNavigateToPermissions,
                    onNavigateToAppearance = onNavigateToAppearance
                )
            }
        }
    }
}
