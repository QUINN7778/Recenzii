package com.sianov.stepan.ui.screens

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.DownloadListener
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
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
    var showDownloadDialog by remember { mutableStateOf(false) }
    var pendingDownloadUrl by remember { mutableStateOf("") }
    var pendingMimetype by remember { mutableStateOf("") }
    var pendingContentDisposition by remember { mutableStateOf("") }
    var pendingUserAgent by remember { mutableStateOf("") }

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
                        // Используем агрессивный WebViewClient для полной очистки интерфейса сайта
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                val script = """
                                    (function() {
                                        function nukeElements() {
                                            // 1. Массив селекторов всех возможных элементов шапки и подвала
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
                                                    el.style.height = '0px';
                                                    el.style.overflow = 'hidden';
                                                });
                                            });

                                            // 2. Скрываем элементы по тексту (навигационные кнопки)
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

                                            // 3. Убираем лишние отступы у body и main
                                            document.body.style.paddingTop = '0px';
                                            document.body.style.marginTop = '0px';
                                            var main = document.querySelector('main, .main-content, #content');
                                            if (main) {
                                                main.style.paddingTop = '0px';
                                                main.style.marginTop = '0px';
                                            }
                                        }

                                        // Запускаем очистку сразу
                                        nukeElements();

                                        // И следим за изменениями (на случай динамической подгрузки)
                                        var observer = new MutationObserver(nukeElements);
                                        observer.observe(document.body, { childList: true, subtree: true });

                                        // Дополнительный "добивающий" таймер на 5 секунд
                                        var count = 0;
                                        var interval = setInterval(function() {
                                            nukeElements();
                                            if (count++ > 10) clearInterval(interval);
                                        }, 500);
                                    })();
                                """.trimIndent()
                                view?.evaluateJavascript(script, null)
                            }
                        }

                        setDownloadListener { downloadUrl, userAgent, contentDisposition, mimetype, _ ->
                            pendingDownloadUrl = downloadUrl
                            pendingUserAgent = userAgent
                            pendingContentDisposition = contentDisposition
                            pendingMimetype = mimetype
                            showDownloadDialog = true
                        }

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true

                            // ЭТО УБИРАЕТ ГИГА-ЗУМ: Отключаем автоматическое увеличение текста
                            layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.NORMAL

                            // ВКЛЮЧАЕМ ЗУМ: разрешаем пользователю самому масштабировать страницу
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false // Скрываем уродливые кнопки +/- (зум будет пальцами)
                            
                            // Эти настройки помогут странице изначально вписаться в экран
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            
                            // Масштаб текста стандартный
                            textZoom = 100
                            
                            // Мобильный User Agent
                            userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/537.36"
                        }

                        // Устанавливаем начальный масштаб в 0 (по умолчанию), 
                        // чтобы WebView сам определил плотность пикселей
                        setInitialScale(0)
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            title = { Text("Загрузка файла") },
            text = { Text("Вы хотите скачать электронную копию билета?") },
            confirmButton = {
                Button(onClick = {
                    showDownloadDialog = false
                    try {
                        val request = DownloadManager.Request(Uri.parse(pendingDownloadUrl))
                        request.setMimeType(pendingMimetype)
                        val fileName = android.webkit.URLUtil.guessFileName(pendingDownloadUrl, pendingContentDisposition, pendingMimetype)
                        
                        request.addRequestHeader("User-Agent", pendingUserAgent)
                        request.setDescription("Загрузка билета...")
                        request.setTitle(fileName)
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                        
                        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        dm.enqueue(request)
                        
                        Toast.makeText(context, "Загрузка началась", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }) {
                    Text("Скачать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
