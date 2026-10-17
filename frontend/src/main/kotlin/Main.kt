import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.scorekeeper.generated.resources.Res
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import kotlin.system.exitProcess
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight

// ============ ЭКРАНЫ ============
enum class Screen { menu, tennis, valeball }

// ============ ТОЧКА ВХОДА ============
fun main() = application {
    // Текущий экран
    var currentScreen by remember { mutableStateOf(Screen.menu) }
    val windowState = rememberWindowState(placement = WindowPlacement.Fullscreen)
    Window(
        onCloseRequest = ::exitApplication,
        title = "ScoreKeeper",
        state= windowState,
        undecorated = true,
    ) {
        // Главный контейнер (восстановлен)
        Box(modifier = Modifier.fillMaxSize()) {

            // Переключение экранов
            when (currentScreen) {
                Screen.menu -> МЕНЮ(
                    btenis = { currentScreen = Screen.tennis },
                    bvoleiball = { currentScreen = Screen.valeball }
                )
                Screen.tennis -> GameScreen(title = "🎾 Теннис", onBack = { currentScreen = Screen.menu })
                Screen.valeball -> GameScreen(title = "🏐 Волейбол", onBack = { currentScreen = Screen.menu })
            }

            // Кнопка закрытия в правом верхнем углу (поверх всего)
            Button(
                onClick = { exitProcess(0) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Text("✕", fontSize = 20.sp)
            }
        } // Закрывающая скобка для Box
    } // Закрывающая скобка для Window
} // Закрывающая скобка для application

// ============ ЭКРАН МЕНЮ ============
@Composable
fun МЕНЮ(
    btenis: () -> Unit,
    bvoleiball: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Фон
        Image(
            painter = painterResource("background.png"),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Кнопки-картинки
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 200.dp),
            horizontalArrangement = Arrangement.spacedBy(150.dp)
        ) {
            // Волейбол
            AnimatedClickableImage(
                painter = painterResource("valeiball.png"),
                onClick = bvoleiball
            )

            // Теннис
            AnimatedClickableImage(
                painter = painterResource("tennis.png"),
                onClick = btenis
            )
        }
    }
}

// ============ УНИВЕРСАЛЬНЫЙ ИГРОВОЙ ЭКРАН временный для игр ============
@Composable
fun GameScreen(title: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05050A)), // Темный фон
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
@Composable
fun AnimatedClickableImage(
    painter: androidx.compose.ui.graphics.painter.Painter,
    onClick: () -> Unit
) {
    // Источник взаимодействия — отслеживает нажатие
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Анимация масштаба: 0.92 при нажатии, 1.0 в покое
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, // пружинка
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
            .scale(scale) // применяем анимацию
            .clickable(
                interactionSource = interactionSource,
                indication = null, // убираем тёмную ripple-тень
                onClick = onClick
            )
    )
}