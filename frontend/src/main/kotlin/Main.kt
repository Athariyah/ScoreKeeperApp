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
import org.jetbrains.skia.Image
import java.io.File
import kotlin.system.exitProcess



// ============ ЭКРАНЫ ============
enum class Screen { menu, tennis, valeball }

data class MatchState(
    val team1: String = "КОМАНДА 1",        // имя 1-й команды, показывается в админке и на табло
    val team2: String = "КОМАНДА 2",        // имя 2-й команды
    val score1: Int = 0,                    // очки 1-й команды в текущей партии
    val score2: Int = 0,                    // очки 2-й команды в текущей партии
    val sets1: Int = 0,                     // выигранные партии 1-й командой
    val sets2: Int = 0,                     // выигранные партии 2-й командой
    val currentSet: Int = 1,                // номер текущей партии (1, 2, 3...)
    val history1: List<Int> = emptyList(),  // стек прошлых score1 для кнопки "Откат"
    val history2: List<Int> = emptyList(),  // стек прошлых score2 для отката обоих счётов сразу
    val finished: Boolean = false           // true = матч окончен, кнопки блокируются
)

// ============ ТОЧКА ВХОДА ============
fun main() = application {
    // Текущий экран
    var currentScreen by remember { mutableStateOf(Screen.menu) }

    // Состояния для окна настройки команд
    var showSetupDialog by remember { mutableStateOf(false) }
    var selectedGame by remember { mutableStateOf(Screen.tennis) }

    var gameLogo1 by remember { mutableStateOf<ImageBitmap?>(null) }
    var gameName1 by remember { mutableStateOf("") }
    var gameLogo2 by remember { mutableStateOf<ImageBitmap?>(null) }
    var gameName2 by remember { mutableStateOf("") }
    var match by remember { mutableStateOf(MatchState()) }    // состояние матча, общее для обоих окон
    var showAudience by remember { mutableStateOf(false) }    // открыто ли второе окно (табло)

    val windowState = rememberWindowState(placement = WindowPlacement.Fullscreen)

    Window(
        onCloseRequest = ::exitApplication,
        title = "ScoreKeeper",
        state = windowState,
        undecorated = true,
    ) {
        // Главный контейнер
        Box(modifier = Modifier.fillMaxSize()) {

            // Переключение экранов
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
                    bsetting = { /* Здесь будет логика общих настроек */ },
                    bexit = { exitProcess(0) }
                )

                Screen.tennis -> VoleyballScreen(  // экран тениса (админка)
                    onBack = { currentScreen = Screen.menu },
                    name1 = gameName1,
                    name2 = gameName2,
                    logo1 = gameLogo1,
                    logo2 = gameLogo2
                )
                Screen.valeball -> VoleyballScreen(  // экран волейбола (админка)
                    onBack = { currentScreen = Screen.menu },
                    name1 = gameName1,
                    name2 = gameName2,
                    logo1 = gameLogo1,
                    logo2 = gameLogo2
                )
            }

        }

        if (showSetupDialog) { // Окно настройки команд (появляется поверх всего)
            setteam_window(
                exit_setwind = { showSetupDialog = false },
                startgame_inready = { logo1, name1, logo2, name2 ->
                    // 👇 ВОТ ЗДЕСЬ Я ВСТАВИЛ СОХРАНЕНИЕ ДАННЫХ
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
}


// ============ ЭКРАН МЕНЮ ============
@Composable
fun МЕНЮ(
    btenis: () -> Unit,
    bvoleiball: () -> Unit,
    bsetting: () -> Unit,
    bexit: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Фон
        Image(
            painter = painterResource("background.png"),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Кнопки-картинки (Волейбол и Теннис)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 200.dp),
            horizontalArrangement = Arrangement.spacedBy(150.dp)
        ) {
            AnimatedClickableImage(
                painter = painterResource("valeiball.png"),
                onClick = bvoleiball
            )
            AnimatedClickableImage(
                painter = painterResource("tennis.png"),
                onClick = btenis
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource("seting.png"),
                contentDescription = "Настройки",
                modifier = Modifier
                    .size(40.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { bsetting() }
            )
            Image(
                painter = painterResource("exit.png"),
                contentDescription = "Закрыть",
                modifier = Modifier
                    .size(40.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { bexit() }
            )
        }
    }
}

// ============ АНИМАЦИЯ КНОПОК ============
@Composable
fun AnimatedClickableImage(
    painter: Painter,
    onClick: () -> Unit
) {
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
        modifier = Modifier
            .size(width = 370.dp, height = 140.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    )
}

// ============ УНИВЕРСАЛЬНЫЙ ИГРОВОЙ ЭКРАН ============
@Composable
fun GameScreen(title: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05050A)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.White, fontSize = 32.sp)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBack) {
                Text("В меню")
            }
        }
    }
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
            modifier = Modifier
                .width(500.dp)
                .height(500.dp)
        ) {
            Box(modifier = Modifier.padding(all = 24.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ВВЕДИТЕ ИМЕНА ИГРОКОВ \n И ЗАГРУЗИТЕ ИХ ЛОГО.",
                        color = Color(32,112,245),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Две колонки команд + разделитель
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

                        // Вертикальная линия-разделитель
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(280.dp)
                                .background(Color(0xFFE0E0E0))
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
                        modifier = Modifier
                            .width(200.dp)
                            .height(50.dp)
                    ) {
                        Text("ГОТОВО", fontSize = 20.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                // Кнопка «Назад» (стрелочка)
                IconButton(
                    onClick = exit_setwind,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 16.dp, y = (-16).dp)
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(180.dp)
    ) {
        Text(title, color = Color(0xFF1976D2), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Круглая кнопка-аватар
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0))
                .clickable { onImageClick() },
            contentAlignment = Alignment.Center
        ) {
            if (image != null) {
                // Если логотип выбран — рисуем его
                Image(
                    bitmap = image,
                    contentDescription = "Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // 👇 А ВОТ ЗДЕСЬ ТЕПЕРЬ ТВОЯ КАРТИНКА
                Image(
                    painter = painterResource("vsavphoto.png"), // Имя твоего файла
                    contentDescription = "Добавить фото",
                    // Вписываем картинку так, чтобы она заполнила кружок
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Поле ввода имени команды
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp) // Задай нужную высоту
                .background(Color(0xFF455A64), RoundedCornerShape(12.dp)),
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

@Composable
fun VoleyballScreen(
    onBack: () -> Unit,
    name1: String,
    name2: String,
    logo1: ImageBitmap?,
    logo2: ImageBitmap?
) {
    // Логика счета
    var score1 by remember { mutableStateOf(0) }//очки
    var score2 by remember { mutableStateOf(0) }//очки
    var sets1 by remember { mutableStateOf(0) }//партия
    var sets2 by remember { mutableStateOf(0) }//партия
}