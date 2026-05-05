package com.sianov.stepan.data.remote

import android.util.Log
import com.sianov.stepan.data.model.AppItem
import com.sianov.stepan.data.model.CastMember
import com.sianov.stepan.data.model.PerformanceDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.SecureRandom
import java.security.cert.X509Certificate

@Singleton
class IvMuzScraper @Inject constructor() {
    private val baseUrl = "https://www.ivmuz.ru"
    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    private val cleanPlots = mapOf(
        "Робин Гуд" to "Легендарная история о защитнике бедных из Шервудского леса. Робин Гуд и его друзья сражаются против тирании шерифа Ноттингема. Вас ждут динамичные бои на мечах, юмор и история любви к прекрасной Мэриан.",
        "Мата Хари" to "Драматический мюзикл о судьбе самой загадочной женщины XX века. История Маргареты Зелле, ставшей легендарной танцовщицей и шпионкой Матой Хари. Опасные игры разведок, роковая страсть и трагический финал великой авантюристки.",
        "Полнолуние" to "Мистическая и чувственная музыкальная история. Сюжет разворачивается в магической атмосфере лунной ночи, когда открываются тайны сердца, а человеческие страсти и желания обретают небывалую силу.",
        "Легенда" to "Эпическое музыкальное произведение о героях, чьи имена остались в веках. История о борьбе добра со злом, о верности долгу, великих свершениях и любви, которая становится легендой.",
        "Красавица и чудовище" to "Волшебная сказка для всей семьи. История прекрасной Белль, которая своей добротой и чистотой сердца смогла разглядеть прекрасную душу за маской чудовища и разрушить древнее заклятие.",
        "Ромео и Джульетта" to "Бессмертная трагедия Уильяма Шекспира. История самой знаменитой и печальной любви двух юных сердец из враждующих семей Монтекки и Капулетти, чья гибель приносит мир в охваченную враждой Верону.",
        "Анна Каренина" to "Грандиозный мюзикл по роману Л. Толстого. Трагическая история любви Анны Карениной и Алексея Вронского, разворачивающаяся на фоне блеска и лицемерия высшего общества Петербурга XIX века.",
        "Летучая мышь" to "Классическая оперетта Иоганна Штрауса. Запутанная история о том, как на балу муж не узнает собственную жену, скрытую под маской летучей мыши. Вечная музыка, маскарад и искрометный юмор.",
        "Сильва" to "История любви офицера Эдвина и звезды варьете Сильвы Вареску. Несмотря на социальные преграды и протесты родителей-аристократов, настоящие чувства находят путь к счастью под мелодии Имре Кальмана.",
        "Алые паруса" to "Музыкальная фантазия по повести А. Грина. Трогательная история Ассоль, которая вопреки насмешкам продолжает ждать свой корабль под алыми парусами. Спектакль о вере в мечту и любви.",
        "Фантом" to "Загадочная драма о Призраке, живущем в подземельях Оперы, и его безответной любви к молодой певице Кристине. История о таланте, одиночестве и великой силе музыки.",
        "Бесприданница" to "Драма по пьесе А. Островского. Трагическая судьба Ларисы Огудаловой в мире, где чувства приносятся в жертву деньгам. Пронзительная история о любви и разочаровании.",
        "Ханума" to "Веселая комедия о соперничестве двух свах в старинном Тбилиси. Музыка, танцы и кавказский колорит помогут соединить сердца молодых влюбленных вопреки воле старого князя.",
        "12 стульев" to "Музыкальное приключение Остапа Бендера и Кисы Воробьянинова в поисках бриллиантов мадам Петуховой. Искрометный юмор Ильфа и Петрова в яркой музыкальной постановке.",
        "Не в стульях счастье" to "Оригинальное музыкальное прочтение бессмертного романа Ильфа и Петрова. История Остапа Бендера и Кисы Воробьянинова, которые в поисках сокровищ мадам Петуховой попадают в череду невероятных и комичных ситуаций.",
        "Капитанская дочка" to "Драматический мюзикл по повести А.С. Пушкина. История Петра Гринева и Маши Мироновой, разворачивающаяся на фоне грозных событий пугачевского бунта. Рассказ о чести, верности долгу и всепобеждающей любви.",
        "Три мушкетера" to "Мюзикл о дружбе, чести и отваге. Д'Артаньян и его верные друзья Атос, Портос и Арамис спасают честь королевы Франции, сражаясь с интригами кардинала Ришелье.",
        "Дон Сезар де Базан" to "Героическая комедия об обедневшем, но благородном испанском дворянине. История о чести, любви и о том, как находчивость помогает выйти победителем из самых сложных ситуаций.",
        "Марица" to "Великолепная оперетта Имре Кальмана. История любви гордой графини Марицы и ее управляющего, скрывающего свое знатное происхождение. Страстные венгерские танцы и мелодии.",
        "Мистер Икс" to "Драматическая история любви таинственного артиста цирка и знатной дамы Теодоры Вердье. Сможет ли любовь преодолеть тайны прошлого и сословные предрассудки?",
        "Собака на сене" to "Комедия Лопе де Вега о любви знатной дамы Дианы к своему секретарю Теодоро. История о ревности, сословной гордости и о том, что сердце не подчиняется приказам.",
        "Летучий корабль" to "Добрая музыкальная сказка о любви трубочиста Ивана и царевны Забавы. С помощью водяного и бабок-ёжек Иван строит летучий корабль, чтобы победить жадного Полкана.",
        "Кот в сапогах" to "Задорная сказка о самом хитром и находчивом коте в мире, который помогает своему хозяину победить великана и жениться на прекрасной принцессе.",
        "Снежная королева" to "Классическая сказка Г.Х. Андерсена о силе дружбе и любви. Маленькая Герда проходит через множество испытаний, чтобы спасти Кая из ледяного плена Снежной королевы.",
        "Бременские музыканты" to "Веселое путешествие Трубадура и его друзей. История о свободе, дружбе и любви, наполненная любимыми песнями и солнечным настроением.",
        "Золушка" to "Волшебный мюзикл для всей семьи. История о доброй девушке, потерянной туфельке и прекрасном принце, которая учит верить в чудеса и справедливость.",
        "Маугли" to "Музыкальный спектакль о приключениях мальчика в джунглях Индии. История о дружбе, законах стаи и борьбе с тигром Шер-Ханом.",
        "Любовь и голуби" to "Музыкальная комедия по мотивам знаменитого фильма. Добрая и смешная история о семье Кузякиных, деревенском быте, измене и великом прощении.",
        "Свадьба в Малиновке" to "Любимая комедия о жизни в украинском селе в годы Гражданской войны. Яркие персонажи, народный юмор и знаменитые песни.",
        "Дамских дел мастер" to "Музыкальная комедия о парикмахере-авантюристе Голохвастове в декорациях старого Киева. История о хвастовстве, интригах и заслуженном финале.",
        "Веселая вдова" to "Шедевр оперетты Франца Легара. Интрига вокруг наследства богатой вдовы Ганны Главари и ее любви к графу Данило в атмосфере парижского лоска.",
        "Труффальдино" to "Музыкальная комедия о ловком слуге Труффальдино, который умудряется служить двум хозяевам сразу, устраивая счастье влюбленных и свое собственное.",
        "Тетка Чарлея" to "Комедия положений о студентах, которые заставляют друга переодеться в богатую тетушку из Бразилии. Фейерверк смеха, переодеваний и забавных ситуаций.",
        "Здравствуйте, я ваша тетя" to "Музыкальная комедия по мотивам знаменитой пьесы. Безработный Бабс Баберлее в роли миллионерши донны Розы д'Альвадорец покоряет сердца и устраивает судьбы влюбленных.",
        "Обыкновенное чудо" to "Музыкальная сказка о Волшебнике, который превратил Медведя в человека, и о том, как настоящая любовь может вернуть человеческий облик вопреки любым заклятиям.",
        "Джейн Эйр" to "Музыкальный роман о сильной женщине. История сироты Джейн Эйр, ее непростого пути к независимости и глубокой любви к мистеру Рочестеру в таинственном Торнфилде.",
        "Аладдин" to "Восточная сказка о бедном юноше, нашедшем волшебную лампу с Джинном. Приключения в Аграбе, полет на ковре-самолете и победа над злым колдуном Джафаром.",
        "Мэри Поппинс" to "История о самой удивительной няне на свете, которая появляется вместе с восточным ветром. Она учит детей видеть чудеса в самых обычных вещах и верить в невозможное.",
        "Буратино" to "Музыкальные приключения деревянного человечка и его друзей. Путь к золотому ключику через Страну Дураков, встречи с лисой Алисой и котом Базилио, и победа над Карабасом-Барабасом.",
        "Остров сокровищ" to "Захватывающий мюзикл о поисках пиратских сокровищ капитана Флинта. Юный Джим Хокинс и его друзья вступают в схватку с коварным Джоном Сильвером.",
        "Пеппи Длинныйчулок" to "История о самой сильной, веселой и независимой девочке в мире. Пеппи живет на вилле 'Курица' и превращает жизнь маленького городка в бесконечный праздник.",
        "Малыш и Карлсон" to "Добрая сказка о дружбе обычного мальчика и 'мужчины в самом расцвете сил' с пропеллером на спине. Веселые полеты над крышами и укрощение фрекен Бок.",
        "Двенадцать месяцев" to "Зимняя сказка о девочке, которую злая мачеха отправила за подснежниками в новогоднюю ночь. Удивительная встреча у костра со всеми братьями-месяцами меняет ее жизнь.",
        "Бал в Савойе" to "Блестящая оперетта Поля Абрахама. История о молодоженах, чьи чувства подвергаются испытанию во время шикарного бала в отеле 'Савой'. Джазовые ритмы и танцы.",
        "Баядера" to "Восточная сказка Имре Кальмана. Любовь индийского принца Раджами и прекрасной парижской артистки Одетты Даримонд в атмосфере таинственности и изящества.",
        "Белая акация" to "Оперетта Исаака Дунаевского о жизни моряков-китобоев и их верных подруг в Одессе. История о любви, дружбе и ожидании на фоне морских просторов.",
        "Бабий бунт" to "Музыкальная комедия по рассказам Михаила Шолохова. Веселая история о том, как казачки решили проучить своих мужей и устроили настоящий бунт на хуторе.",
        "Человек-амфибия" to "Романтический мюзикл по мотивам фантастического романа Александра Беляева. История любви юноши Ихтиандра, живущего в океане, и прекрасной девушки Гуттиэре.",
        "Кентервильское привидение" to "Музыкальная комедия по Оскару Уайлду. Американская семья покупает старинный английский замок с призраком, но вместо ужаса начинает его воспитывать на свой лад.",
        "Волшебник Изумрудного города" to "Путешествие Элли и ее друзей — Тотошки, Страшилы, Железного Дровосека и Трусливого Льва — по дороге из желтого кирпича в поисках великого Гудвина.",
        "Маскарад" to "Драматический мюзикл по мотивам драмы М. Лермонтова. Роковая ошибка, ревность и трагическая судьба прекрасной Нины в вихре петербургского маскарада.",
        "Принцесса цирка" to "Классическая оперетта о любви и чести. Таинственный Мистер Икс покоряет сердца, выступая под куполом цирка, скрывая свое лицо и знатное прошлое.",
        "Фиалка Монмартра" to "История о жизни талантливых и бедных художников на Монмартре. Любовь, творчество и вера в успех под звуки прекрасной музыки Имре Кальмана.",
        "Цыганский барон" to "Оперетта Иоганна Штрауса. История о возвращении наследника знатного рода в свои владения, о поисках клада и о любви к прекрасной цыганке Саффи.",
        "Синяя птица" to "Философская сказка о детях, которые отправляются в долгое путешествие за Синей птицей счастья, открывая для себя тайны мира и ценность домашнего очага.",
        "Аленький цветочек" to "Волшебная сказка о доброй девушке Настеньке, которая ради спасения отца отправляется в замок к чудовищу и своей любовью возвращает ему человеческий облик.",
        "По щучьему велению" to "Веселая сказка про Емелю, который поймал говорящую щуку и получил возможность исполнять любые свои желания. История о доброте, удаче и настоящем чуде.",
        "Золотой ключик" to "Приключения деревянного мальчика Буратино и его друзей из кукольного театра. Путь к тайной двери за очагом папы Карло через козни Карабаса-Барабаса.",
        "Сказка о царе Салтане" to "Музыкальная постановка по сказке А. Пушкина. История о князе Гвидоне, прекрасной царевне Лебеди и чудесах острова Буяна.",
        "Морозко" to "Зимняя сказка о доброй Настеньке, которую мачеха отправила в лес, и о мудром дедушке Морозко, наградившем ее за терпение и кротость.",
        "Кошкин дом" to "Поучительная сказка о богатой Кошке, которая в беде осталась одна, и только бедные племянники-котята открыли ей дверь своего маленького дома.",
        "Белоснежка" to "Классическая история о прекрасной принцессе, семи добрых гномах и злой королеве. Сказка о том, что истинная красота и доброта всегда побеждают зависть.",
        "Дюймовочка" to "Путешествие крошечной девочки по большому миру. Встречи с жабами, жуком, кротом и, наконец, счастливое спасение и встреча с королем эльфов.",
        "Каприз императрицы" to "Увлекательная музыкальная комедия, действие которой происходит в блестящую эпоху правления Елизаветы Петровны. Вас ждут придворные интриги, юмор и, конечно, любовь.",
        "Царевна-лягушка" to "Веселая музыкальная сказка для всей семьи. История о том, как Иван-царевич нашел свое счастье в болоте, и о том, что истинная красота скрыта внутри. Яркие костюмы и любимые герои.",
        "Принцесса на горошине" to "Волшебная сказка о настоящей принцессе, которая смогла почувствовать маленькую горошину через двенадцать матрасов. Музыкальная история о благородстве и искренности."
    )

    private fun trustAllCertificates() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
            })
            val sc = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, SecureRandom())
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
        } catch (e: Exception) {}
    }

    private fun cleanTitle(title: String): String {
        return title.lowercase()
            .replace(Regex("(мюзикл|оперетта|комедия|сказка|драма|фантазия|детский|спектакль|для детей|по мотивам)"), "")
            .replace(Regex("[^а-яa-z0-9]"), "").trim()
    }

    private fun getPlotByTitle(title: String): String {
        val cleaned = cleanTitle(title)
        val entry = cleanPlots.entries.find { cleanTitle(it.key).contains(cleaned) || cleaned.contains(cleanTitle(it.key)) }
        return entry?.value ?: "Погрузитесь в атмосферу высокого искусства вместе с артистами Ивановского музыкального театра. Вас ждет захватывающий сюжет, великолепные вокальные партии и яркие декорации."
    }

    private val knownDurations = mapOf(
        "Анна Каренина" to "2 часа 20 минут",
        "Капитанская дочка" to "2 часа 20 минут",
        "Робин Гуд" to "2 часа 20 минут",
        "Сильва" to "2 часа 30 минут",
        "Не в стульях счастье" to "2 часа 30 минут",
        "Полнолуние" to "2 часа 40 минут",
        "Каприз Императрицы" to "2 часа 30 минут",
        "Царевна-Лягушка" to "1 час",
        "Принцесса на горошине" to "1 час",
        "Летучая мышь" to "2 часа 30 минут",
        "Мистер Икс" to "2 часа 30 минут",
        "Ханума" to "2 часа 20 минут",
        "Джейн Эйр" to "2 часа 30 минут",
        "Алые паруса" to "2 часа 15 минут",
        "Фантом" to "2 часа 30 минут",
        "Три мушкетера" to "2 часа 30 минут"
    )

    private fun getDurationByTitle(title: String): String? {
        val cleaned = cleanTitle(title)
        return knownDurations.entries.find { 
            val k = cleanTitle(it.key)
            k.contains(cleaned) || cleaned.contains(k) 
        }?.value
    }

    private fun extractPlot(doc: Document, title: String): String {
        // 1. Пробуем найти текст описания на странице
        val descriptionElements = doc.select(".performance_item_page p, .performance_unit_page p, .description p, .text_content p")
        val extracted = descriptionElements
            .map { it.text().trim() }
            .filter { 
                it.length > 40 && 
                !it.contains("Режиссер", ignoreCase = true) && 
                !it.contains("Музыка", ignoreCase = true) &&
                !it.contains("Продолжительность", ignoreCase = true) &&
                !it.contains("Либретто", ignoreCase = true) &&
                !it.contains("Балетмейстер", ignoreCase = true) &&
                !it.contains("Художник", ignoreCase = true)
            }
            .joinToString("\n\n")
            .trim()

        if (extracted.isNotEmpty() && extracted.length > 150) {
            return extracted.take(2000)
        }

        // 2. Если на странице пусто или текст технический — берем из нашей базы
        return getPlotByTitle(title)
    }

    suspend fun fetchPerformanceDetail(url: String): PerformanceDetail? = withContext(Dispatchers.IO) {
        try {
            if (url.isEmpty()) return@withContext null
            val fullUrl = if (url.startsWith("/")) baseUrl + url else url
            val doc: Document = Jsoup.connect(fullUrl).userAgent(userAgent).timeout(20000).get()
            
            val title = doc.select(".title.mobile").first()?.text()?.trim()
                ?: doc.select("nav.breadcrumbs span").last()?.text()?.trim()
                ?: doc.select("h1").first()?.text()?.trim()
                ?: doc.title().replace("- Ивановский музыкальный театр", "").trim()
            
            var duration: String? = null
            var author: String? = null
            var acts: String? = null

            // 1. Поиск продолжительности через элементы
            val infoElements = doc.select(".performance_item_page b, .performance_item_page strong, .performance_info b")
            for (el in infoElements) {
                val text = el.text().trim()
                if (text.contains("Продолжительность", ignoreCase = true)) {
                    val fullText = el.parent()?.text() ?: ""
                    if (!fullText.contains("антракт", ignoreCase = true)) {
                        val match = Regex("(\\d+\\s*час[а-я]*\\s*\\d*\\s*мин[а-я]*|\\d+\\s*час[а-я]*)").find(fullText)
                        duration = match?.value?.trim()
                    }
                }
            }

            // 2. Поиск через регулярку по всему тексту (самый надежный способ для этого сайта)
            if (duration.isNullOrEmpty()) {
                val pageText = doc.text()
                // Ищем конструкцию: "Продолжительность" + (не более 30 символов) + "час/мин"
                val match = Regex("(?i)Продолжительность[^\\d]{0,30}(\\d+\\s*час[а-я]*\\s*\\d*\\s*мин[а-я]*|\\d+\\s*час[а-я]*)").find(pageText)
                duration = match?.groupValues?.get(1)?.trim()
            }

            // 3. Если всё еще пусто — берем из базы
            if (duration.isNullOrEmpty()) {
                duration = getDurationByTitle(title)
            }
            
            if (duration.isNullOrEmpty()) duration = "уточняется"

            // Сбор остальных данных
            val allElements = doc.select(".performance_item_page *, .performance_unit_page *")
            for (el in allElements) {
                val text = el.text().trim()
                if (text.contains("Действие", ignoreCase = true) && text.length < 60 && acts == null) {
                    acts = text
                }
                if ((text.startsWith("Музыка") || text.startsWith("Либретто") || text.startsWith("Режиссер")) && text.length < 150 && author == null) {
                    author = text
                }
            }

            val gallery = mutableListOf<String>()
            // Приоритетно собираем ссылки на большие фото (из <a> тегов)
            val highResElements = doc.select("a.fancybox, a[href*=.jpg], a[href*=.png], .gallery a")
            for (el in highResElements) {
                var img = el.attr("href").trim()
                val isPushkin = img.contains("pushkin", ignoreCase = true) || 
                               el.attr("title").contains("пушкин", ignoreCase = true) ||
                               el.select("img").attr("alt").contains("пушкин", ignoreCase = true)

                if (img.isNotEmpty() && !isPushkin) {
                    if (img.startsWith("/")) img = baseUrl + img
                    if (!gallery.contains(img)) gallery.add(img)
                }
            }

            // Добираем картинки, которых еще нет (но фильтруем явные иконки и Пушкинскую карту)
            val imgElements = doc.select(".performance_item_page img, .performance_unit img, .main_image img, .gallery img")
            for (el in imgElements) {
                if (el.parents().any { it.hasClass("action_person") || it.hasClass("unit_actor") }) continue
                
                var img = el.attr("src").ifEmpty { el.attr("data-src") }.trim()
                val isPushkin = img.contains("pushkin", ignoreCase = true) || 
                               el.attr("alt").contains("пушкин", ignoreCase = true) ||
                               el.attr("title").contains("пушкин", ignoreCase = true)

                if (img.isNotEmpty() && !isPushkin) {
                    if (img.startsWith("/")) img = baseUrl + img
                    // Если это явно превью (содержит /thumb/ или маленькие размеры в имени), пропускаем, если уже есть что-то другое
                    val isThumb = img.contains("/thumb/") || img.contains("_s.") || img.contains("-150x150")
                    if (isThumb && gallery.isNotEmpty()) continue
                    
                    if (!gallery.contains(img)) gallery.add(img)
                }
            }

            val cast = mutableListOf<CastMember>()
            // Улучшенный поиск актёров (включая таблицы и списки)
            val personBlocks: Elements = doc.select(".action_person, .unit_actor, .performance_cast tr, .actors_list li")
            for (pb in personBlocks) {
                if (pb.hasClass("action_person")) {
                    val role = pb.select(".personage").first()?.text()?.trim() ?: ""
                    val actors: Elements = pb.select(".unit_actor")
                    for (ab in actors) {
                        val name = ab.select(".name").first()?.text()?.trim() ?: ""
                        var img = ab.select("img").first()?.attr("src") ?: ""
                        if (img.isNotEmpty() && img.startsWith("/")) img = baseUrl + img
                        if (role.isNotEmpty() && name.isNotEmpty()) cast.add(CastMember(role, name, img.ifEmpty { null }))
                    }
                } else if (pb.tagName() == "tr") {
                    // Обработка табличной верстки (бывает на некоторых страницах)
                    val tds = pb.select("td")
                    if (tds.size >= 2) {
                        val role = tds[0].text().trim()
                        val name = tds[1].text().trim()
                        if (role.isNotEmpty() && name.isNotEmpty() && !role.contains("Действующее лицо")) {
                            cast.add(CastMember(role, name, null))
                        }
                    }
                }
            }

            var buyTicketUrl: String? = null
            val buyButtons = doc.select("a[href*=ivanovokoncert.ru], a:contains(Купить билет)")
            for (btn in buyButtons) {
                val href = btn.attr("href")
                if (href.contains("ivanovokoncert.ru")) {
                    buyTicketUrl = href
                    break
                }
            }

            return@withContext PerformanceDetail(
                title = title,
                imageUrl = gallery.firstOrNull() ?: "",
                description = extractPlot(doc, title),
                cast = cast.distinctBy { "${it.role}:${it.name}" },
                detailUrl = url,
                buyTicketUrl = buyTicketUrl,
                galleryImages = gallery,
                author = author,
                acts = acts,
                duration = duration
            )
        } catch (e: Exception) { null }
    }

    suspend fun fetchPosters(): List<AppItem> = withContext(Dispatchers.IO) {
        try {
            trustAllCertificates()
            val doc: Document = Jsoup.connect("$baseUrl/ticket_online/").userAgent(userAgent).timeout(30000).get()
            val items = mutableListOf<AppItem>()
            val elements: Elements = doc.select(".cell.active")
            for (element in elements) {
                val title = element.select(".name").text().trim()
                if (title.isNotEmpty()) {
                    val day = element.select(".day").text().trim()
                    val month = element.select(".month").text().trim()
                    val time = element.select(".time").text().trim()
                    val dateStr = if (time.isNotEmpty()) "$day $month, $time" else "$day $month"
                    val style = element.select(".performance_unit").first()?.attr("style") ?: ""
                    var img = if (style.contains("url(")) style.substringAfter("url(").substringBefore(")").trim('\'', '"') else ""
                    if (img.startsWith("/")) img = baseUrl + img
                    var url = element.select("a").first()?.attr("href") ?: ""
                    if (url.startsWith("/")) url = baseUrl + url
                    items.add(AppItem(title, "", dateStr, img, url))
                }
            }
            return@withContext items.distinctBy { cleanTitle(it.title) }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun fetchNews(): List<AppItem> = withContext(Dispatchers.IO) {
        try {
            val doc: Document = Jsoup.connect("$baseUrl/news/").userAgent(userAgent).timeout(20000).get()
            val items = mutableListOf<AppItem>()
            val cells: Elements = doc.select(".cell")
            for (element in cells) {
                val title = element.select(".text").text().ifEmpty { element.select("a").text() }.trim()
                val date = element.select(".date").text().trim()
                if (title.isNotEmpty() && date.isNotEmpty()) {
                    var url = element.select("a").first()?.attr("href") ?: ""
                    if (url.startsWith("/")) url = baseUrl + url
                    
                    var img = element.select("img").first()?.attr("src") 
                        ?: element.select(".image img").attr("src")
                        ?: ""
                    
                    if (img.startsWith("/")) img = baseUrl + img
                    items.add(AppItem(title, "", date, img, url))
                }
            }
            return@withContext items
        } catch (e: Exception) { emptyList() }
    }

    suspend fun fetchNewsDetail(url: String): String = withContext(Dispatchers.IO) {
        try {
            if (url.isEmpty()) return@withContext ""
            val fullUrl = if (url.startsWith("/")) baseUrl + url else url
            val doc: Document = Jsoup.connect(fullUrl).userAgent(userAgent).timeout(20000).get()
            
            // 1. Поиск основного контейнера с текстом (расширенный список селекторов)
            val contentSelectors = arrayOf(
                ".news-item-detail",
                ".news_item_page",
                ".text_content",
                ".news-detail",
                ".detail_text",
                ".content-text",
                ".article-content",
                "#content",
                ".content"
            )
            
            var contentElement: Element? = null
            for (selector in contentSelectors) {
                val found = doc.select(selector).first()
                if (found != null && found.text().length > 20) {
                    contentElement = found
                    break
                }
            }

            val text = if (contentElement != null) {
                // Пытаемся достать параграфы
                val paragraphs = contentElement.select("p")
                if (paragraphs.isNotEmpty() && paragraphs.text().length > 20) {
                    paragraphs.map { it.text().trim() }
                        .filter { it.length > 2 }
                        .joinToString("\n\n")
                } else {
                    // Если параграфов нет, берем текст целиком и разбиваем по смыслу
                    contentElement.text().replace(". ", ".\n\n")
                }
            } else {
                // План В: Ищем самый длинный текстовый блок на странице
                doc.select("div")
                    .map { it }
                    .filter { it.text().length > 100 && it.children().isEmpty() }
                    .maxByOrNull { it.text().length }
                    ?.text() ?: ""
            }

            return@withContext text
                .replace("Назад к списку", "")
                .replace("Вернуться в раздел", "")
                .replace("Все новости", "")
                .trim()
        } catch (e: Exception) { "" }
    }

    suspend fun getImageBytes(url: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            if (url.isEmpty()) return@withContext null
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.doInput = true
            connection.connect()
            if (connection.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                return@withContext connection.inputStream.readBytes()
            }
        } catch (e: Exception) {
            Log.e("IvMuzScraper", "Error fetching image from $url: ${e.message}")
        }
        null
    }
}
