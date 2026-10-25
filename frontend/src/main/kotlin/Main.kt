import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import com.example.ApiClient
import org.jetbrains.skia.Image
import java.io.File
import kotlin.system.exitProcess

fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

// ============ ЭКРАНЫ ============
enum class Screen { menu, tennis, valeball }

data class MatchState(
    val team1: String = "КОМАНДА 1",
    val team2: String = "КОМАНДА 2",
    val score1: Int = 0,
    val score2: Int = 0,
    val sets1: Int = 0,
    val sets2: Int = 0,
    val currentSet: Int = 1,
    val finished: Boolean = false,
    val status: String = "IN_PROGRESS"
)

// ============ ТОЧКА ВХОДА ============
fun main() = application {
    var currentScreen by remember { mutableStateOf(Screen.menu) }
    var showSetupDialog by remember { mutableStateOf(false) }
    var selectedGame by remember { mutableStateOf(Screen.tennis) }

    var gameLogo1 by remember { mutableStateOf<ImageBitmap?>(null) }
    var gameName1 by remember { mutableStateOf("") }
    var gameLogo2 by remember { mutableStateOf<ImageBitmap?>(null) }
    var gameName2 by remember { mutableStateOf("") }

    // ГЛОБАЛЬНОЕ СОСТОЯНИЕ МАТЧА (управляется Python-бэкендом)
    var match by remember { mutableStateOf(MatchState()) }
    var showAudience by remember { mutableStateOf(false) }

    val windowState = rememberWindowState(placement = WindowPlacement.Fullscreen)

    // ============ ГЛАВНОЕ ОКНО (АДМИНКА) ============
    Window(
        onCloseRequest = ::exitApplication,
        title = "ScoreKeeper - Админка",
        state = windowState
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                Screen.menu -> МЕНЮ(
                    btenis = {
                        selectedGame = Screen.tennis
                        showSetupDialog = true
                    },
                    bvoleiball = {
                        selectedGame = Screen.valeball
                        showSetupDialog = true
                    },
                    bsetting = { },
                    bexit = { exitProcess(0) }
                )

                Screen.tennis, Screen.valeball -> VoleyballScreen( // Используем один экран для обоих
                    onBack = { currentScreen = Screen.menu },
                    name1 = gameName1,
                    name2 = gameName2,
                    logo1 = gameLogo1,
                    logo2 = gameLogo2,
                    match = match,
                    onMatchUpdate = { match = it },
                    onToggleAudience = { showAudience = !showAudience }
                )
            }
        }

        if (showSetupDialog) {
            setteam_window(
                exit_setwind = { showSetupDialog = false },
                startgame_inready = { logo1, name1, logo2, name2 ->
                    gameLogo1 = logo1
                    gameName1 = name1
                    gameLogo2 = logo2
                    gameName2 = name2
                    showSetupDialog = false
                    currentScreen = selectedGame
                }
            )
        }
    }

    // ============ ВТОРОЕ ОКНО (ТАБЛО ДЛЯ ЗРИТЕЛЕЙ) ============
    if (showAudience) {
        Window(
            onCloseRequest = { showAudience = false },
            title = "ScoreKeeper - Табло",
            state = rememberWindowState(placement = WindowPlacement.Fullscreen)
        ) {
            AudienceScreen(match = match)
        }
    }
}

// ============ ЭКРАН МЕНЮ ============
@Composable
fun МЕНЮ(btenis: () -> Unit, bvoleiball: () -> Unit, bsetting: () -> Unit, bexit: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource("background.png"),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 200.dp),
            horizontalArrangement = Arrangement.spacedBy(150.dp)
        ) {
            AnimatedClickableImage(painter = painterResource("valeiball.png"), onClick = bvoleiball)
            AnimatedClickableImage(painter = painterResource("tennis.png"), onClick = btenis)
        }

        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource("seting.png"),
                contentDescription = "Настройки",
                modifier = Modifier.size(40.dp).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { bsetting() }
            )
            Image(
                painter = painterResource("exit.png"),
                contentDescription = "Закрыть",
                modifier = Modifier.size(40.dp).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { bexit() }
            )
        }
    }
}

// ============ АНИМАЦИЯ КНОПОК ============
@Composable
fun AnimatedClickableImage(painter: Painter, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(width = 370.dp, height = 140.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    )
}

// ============ УТИЛИТА ДЛЯ ЗАГРУЗКИ КАРТИНКИ ============
fun loadImageFromFile(): ImageBitmap? {
    val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Выберите логотип", java.awt.FileDialog.LOAD)
    dialog.setFilenameFilter { _, name ->
        name.endsWith(".png", ignoreCase = true) ||
                name.endsWith(".jpg", ignoreCase = true) ||
                name.endsWith(".jpeg", ignoreCase = true)
    }
    dialog.isVisible = true

    val fileName = dialog.file
    val directory = dialog.directory

    if (fileName != null && directory != null) {
        return try {
            val file = File(directory, fileName)
            Image.makeFromEncoded(file.readBytes()).toComposeImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    return null
}

// ============ ОКНО НАСТРОЙКИ КОМАНД ============
@Composable
fun setteam_window(
    exit_setwind: () -> Unit,
    startgame_inready: (ImageBitmap?, String, ImageBitmap?, String) -> Unit
) {
    var logo1 by remember { mutableStateOf<ImageBitmap?>(null) }
    var logo2 by remember { mutableStateOf<ImageBitmap?>(null) }
    var name1 by remember { mutableStateOf("") }
    var name2 by remember { mutableStateOf("") }

    Dialog(onDismissRequest = exit_setwind) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.width(500.dp).height(500.dp)
        ) {
            Box(modifier = Modifier.padding(all = 24.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ВВЕДИТЕ ИМЕНА ИГРОКОВ \n И ЗАГРУЗИТЕ ИХ ЛОГО.",
                        color = Color(32, 112, 245),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TeamColumn(
                            title = "Игрок 1",
                            image = logo1,
                            onImageClick = { logo1 = loadImageFromFile() },
                            name = name1,
                            onNameChange = { name1 = it }
                        )

                        Box(
                            modifier = Modifier.width(1.dp).height(280.dp).background(Color(0xFFE0E0E0))
                                .align(Alignment.CenterVertically)
                        )

                        TeamColumn(
                            title = "Игрок 2",
                            image = logo2,
                            onImageClick = { logo2 = loadImageFromFile() },
                            name = name2,
                            onNameChange = { name2 = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { startgame_inready(logo1, name1, logo2, name2) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.width(200.dp).height(50.dp)
                    ) {
                        Text("ГОТОВО", fontSize = 20.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(
                    onClick = exit_setwind,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 16.dp, y = (-16).dp)
                ) {
                    Text("←", fontSize = 32.sp, color = Color(0xFF1976D2))
                }
            }
        }
    }
}

// ============ КОЛОНКА КОМАНДЫ ============
@Composable
fun TeamColumn(
    title: String,
    image: ImageBitmap?,
    onImageClick: () -> Unit,
    name: String,
    onNameChange: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(180.dp)) {
        Text(title, color = Color(0xFF1976D2), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.size(140.dp).clip(CircleShape).background(Color(0xFFE0E0E0))
                .clickable { onImageClick() },
            contentAlignment = Alignment.Center
        ) {
            if (image != null) {
                Image(
                    bitmap = image,
                    contentDescription = "Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource("vsavphoto.png"),
                    contentDescription = "Добавить фото",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(40.dp).background(Color(0xFF455A64), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = name,
                onValueChange = onNameChange,
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                        if (name.isEmpty()) {
                            Text("...", color = Color.LightGray, fontSize = 14.sp)
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ============ ЭКРАН АДМИНКИ ИГРЫ (управляется Python) ============
@Composable
fun VoleyballScreen(
    onBack: () -> Unit,
    name1: String,
    name2: String,
    logo1: ImageBitmap?,
    logo2: ImageBitmap?,
    match: MatchState,
    onMatchUpdate: (MatchState) -> Unit,
    onToggleAudience: () -> Unit
) {
    LaunchedEffect(Unit) {
        ApiClient.startMatch("volleyball", name1, name2, 0) { response ->
            if (response != null) {
                onMatchUpdate(
                    match.copy(
                        team1 = response.player1, team2 = response.player2,
                        score1 = response.score[0], score2 = response.score[1],
                        sets1 = response.sets[0], sets2 = response.sets[1],
                        currentSet = response.current_set,
                        status = response.status
                    )
                )
            }
        }
    }

    var secondsElapsed by remember { mutableStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (true) {
                kotlinx.coroutines.delay(1000L)
                secondsElapsed++
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        2
        Image(
            painter = painterResource("ВОЛЕЙБОЛЗАДНИЙФОН.png"),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(60.dp),
            modifier = Modifier.offset(x = 1400.dp, y = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Иконка просто отображается (без кликов)
                Icon(
                    painter = painterResource("timer.png"),
                    contentDescription = null,
                    modifier = Modifier.size(width = 30.dp, height = 35.dp)
                )

                // Цифры таймера
                Text(
                    text = formatTime(secondsElapsed),
                    color = Color.Gray,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Image(
                painter = painterResource("setingingame.png"),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 35.dp, height = 35.dp)
                    .clickable {
                        // Сюда потом добавишь логику открытия окна настроек
                        println("Нажата шестеренка настроек")
                    }
            )
            Box(modifier = Modifier.size(width = 185.dp, height = 55.dp)) {
                AnimatedClickableImage(
                    painter = painterResource("back.png"),
                    onClick = { onBack() }
                )
            }

        }
        Image(
            painter = painterResource("Group.png"),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(width = 490.dp, height = 120.dp)
                .offset(x = 1320.dp, y = 160.dp))

        MatchCard(
            logo1 = logo1,
            name1 = name1,
            logo2 = logo2,
            name2 = name2,
            // Проверяем статус, который прислал Python
            statusText = if (match.status == "FINISHED") "Завершено" else "В игре",
            isFinished = match.status == "FINISHED",
            score1 = match.score1,
            score2 = match.score2,
            modifier = Modifier.align(Alignment.Center).offset(y = 50.dp)
        )
    }

}

//маленькая карточка матча
@Composable
fun MatchCard(
    logo1: ImageBitmap?,
    name1: String,
    logo2: ImageBitmap?,
    name2: String,
    statusText: String,
    isFinished: Boolean,
    score1: Int, // 👈 добавили
    score2: Int, // 👈 добавили
    modifier: Modifier = Modifier // 👈 добавили для позиционирования
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = modifier.width(350.dp) // 👈 используем переданный modifier
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- ИГРОК 1 ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (logo1 != null) Color.Transparent else Color.LightGray)
                ) {
                    if (logo1 != null) {
                        Image(bitmap = logo1, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = name1,
                    color = Color(0xFF2979FF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // 👇 Реальный счет
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("$score1", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            // Разделитель
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEEEEE)))
            Spacer(modifier = Modifier.height(16.dp))

            // --- ИГРОК 2 ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (logo2 != null) Color.Transparent else Color.LightGray)
                ) {
                    if (logo2 != null) {
                        Image(bitmap = logo2, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = name2,
                    color = Color(0xFF2979FF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // 👇 Реальный счет
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("$score2", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- СТАТУС ---
            Box(
                modifier = Modifier
                    .background(
                        color = if (isFinished) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isFinished) Color.Red else Color.Green)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        color = if (isFinished) Color.Red else Color.Green,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
// ============ ЭКРАН ТАБЛО (ВТОРОЕ ОКНО) ============
@Composable
fun AudienceScreen(match: MatchState) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource("backgroundaudencscreen.png"),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}