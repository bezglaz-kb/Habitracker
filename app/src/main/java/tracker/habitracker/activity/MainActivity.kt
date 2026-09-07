package tracker.habitracker.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tracker.habitracker.entity.Habit
import tracker.habitracker.ui.theme.HabitrackerTheme
import tracker.habitracker.viewmodel.HabitViewModel
import java.time.LocalDate
import androidx.compose.material.icons.filled.Delete

object Routes {
    const val MAIN_SCREEN = "main"
    const val CREATE_SCREEN = "create"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitrackerTheme {
                val viewModel: HabitViewModel = viewModel()
                val habits by viewModel.habitsState.collectAsState()

                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Routes.MAIN_SCREEN
                ) {
                    composable(Routes.MAIN_SCREEN) {
                        MainScreen(
                            habits = habits,
                            navController = navController,
                            onDayClick = { habitName, index ->
                                viewModel.toggleDay(
                                    habitName,
                                    index
                                )
                            },
                            onDeleteHabit = { habit -> viewModel.deleteHabit(habit)}
                        )
                    }
                    composable(Routes.CREATE_SCREEN) {
                        CreateHabitScreen(
                            onSaveClick = { name ->
                                viewModel.createNewHabit(name)
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    habits: List<Habit>,
    navController: NavController,
    onDayClick: (String, Int) -> Unit,
    onDeleteHabit: (Habit) -> Unit
) {
    var isReadyToShowPlaceholder by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        isReadyToShowPlaceholder = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.CREATE_SCREEN) }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Создать")
            }
        }
    ) { innerPadding ->
        if (habits.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(
                    habits,
                    key = { it.name }
                ) { habit ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart ||
                                dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                onDeleteHabit(habit)
                                true
                            } else {
                                false
                            }
                        },
                        positionalThreshold = { totalDistance -> totalDistance * 0.5f }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val backgroundColor = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                                    else -> Color(0xFFF44336)
                                }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(backgroundColor)
                                    .padding(horizontal = 24.dp),
                                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                                    Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Удалить",
                                    tint = Color.White
                                )
                            }
                        },
                        content = {
                            // Сама карточка привычки (белый/темный фон, чтобы перекрывать красный задник)
                            Surface(modifier = Modifier.fillMaxWidth()) {
                                HabitCard(
                                    habit = habit,
                                    onHabitDayClick = onDayClick
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else if (isReadyToShowPlaceholder) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "У вас пока нет привычек.\nНажмите на + чтобы создать!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding))
        }
    }
}

@Composable
fun CreateHabitScreen(
    onSaveClick: (String) -> Unit
) {
    var habitNameInput by remember { mutableStateOf("") }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Создание привычки",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = habitNameInput,
                onValueChange = { habitNameInput = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { if (habitNameInput.isNotBlank()) onSaveClick(habitNameInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = habitNameInput.isNotBlank()
            ) {
                Text("Сохранить привычку")
            }
        }
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    modifier: Modifier = Modifier,
    onHabitDayClick: (String, Int) -> Unit
) {
    val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val todayIndex = LocalDate.now().dayOfWeek.value - 1

    val currentOnDayClick by rememberUpdatedState(onHabitDayClick)

    Column(
        modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = habit.progressRow.toString())
            Icon(imageVector = Icons.Filled.Check, contentDescription = "Maked")
            Spacer(Modifier.width(16.dp))
            Text(text = habit.name)
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            habit.history.forEachIndexed { index, isDayCompleted ->
                val isToday = (index == todayIndex)
                val circleColor = when {
                    isDayCompleted -> Color(0xFF4CAF50)
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> Color.LightGray
                }
                val onCircleClick = remember(habit.name) {
                    { currentOnDayClick(habit.name, index) }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = daysOfWeek[index],
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isToday) MaterialTheme.colorScheme.primary else Color.Gray,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(circleColor)
                            .clickable(enabled = isToday) { onCircleClick() }
                    ) {
                        if (isDayCompleted) {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = "completed",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HabitCardPreview() {
    HabitrackerTheme {
        HabitCard(
            Habit(
                name = "Make it",
                progressRow = 3,
                history = listOf(false, true, false, false, false, false, false)
            ),
            onHabitDayClick = { _, _ -> }
        )
    }
}

