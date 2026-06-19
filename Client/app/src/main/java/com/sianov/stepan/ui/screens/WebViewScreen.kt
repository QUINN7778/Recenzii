package com.sianov.stepan.ui.screens

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    url: String,
    onBack: () -> Unit
) {
    // Состояние загрузки для скрытия мерцания
    var isWebLoading by remember { mutableStateOf(true) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Покупка билета") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        
                        // Скрываем WebView пока не применили скрипты
                        alpha = 0f

                        val nukeScript = """
                            (function() {
                                function nukeElements() {
                                    var selectors = [
                                        'header', '.header', '#header', '.top-panel', '.navbar', 
                                        '.top_menu', '.mobile-header', '.footer', '.site-footer', 
                                        '.breadcrumbs', '.breadcrumb', '.top-line', '.top_line',
                                        '.navigation', '.nav-container', '.menu-wrapper',
                                        '.header-middle', '.header-bottom', '.header-top'
                                    ];
                                    
                                    selectors.forEach(function(s) {
                                        document.querySelectorAll(s).forEach(function(el) {
                                            el.style.display = 'none';
                                            el.style.visibility = 'hidden';
                                        });
                                    });

                                    var textToHide = [
                                        'список мероприятий', 'все события', 'главная', 
                                        'иваново концерт', 'история заказов', 'касса', 
                                        'личный кабинет', 'войти', 'регистрация'
                                    ];
                                    
                                    document.querySelectorAll('a, button, span, div, li').forEach(function(el) {
                                        var text = el.textContent.trim().toLowerCase();
                                        textToHide.forEach(function(target) {
                                            if (text === target || (text.includes(target) && el.childNodes.length <= 2)) {
                                                el.style.display = 'none';
                                            }
                                        });
                                    });

                                    document.body.style.paddingTop = '0px';
                                    document.body.style.marginTop = '0px';
                                }

                                nukeElements();
                                var observer = new MutationObserver(nukeElements);
                                observer.observe(document.body, { childList: true, subtree: true });
                            })();
                        """.trimIndent()

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                isWebLoading = true
                                view?.alpha = 0f
                                view?.evaluateJavascript(nukeScript, null)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                view?.evaluateJavascript(nukeScript) {
                                    // Показываем WebView только когда скрипт отработал
                                    isWebLoading = false
                                    view.alpha = 1f
                                }
                            }
                        }

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.NORMAL
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false 
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            textZoom = 100
                            userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/537.36"
                        }
                        setInitialScale(0)
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            if (isWebLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
