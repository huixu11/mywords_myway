package com.mywordsmyway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.mywordsmyway.ui.screen.NotesScreen
import com.mywordsmyway.ui.screen.PrivacyScreen
import com.mywordsmyway.ui.screen.ReviewScreen
import com.mywordsmyway.ui.screen.WordsScreen
import com.mywordsmyway.ui.screen.WriteNoteScreen
import com.mywordsmyway.ui.theme.MyWordsTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory((application as MyWordsApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyWordsTheme {
                MyWordsApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun MyWordsApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route.orEmpty()

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { item ->
                    NavigationBarItem(
                        selected = item.isSelected(currentRoute),
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo("notes") {
                                    saveState = item.route != "notes"
                                }
                                launchSingleTop = true
                                restoreState = item.route != "notes"
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "notes",
            modifier = Modifier,
        ) {
            composable(
                route = "note/{conversationId}?source={source}",
                arguments = listOf(
                    navArgument("conversationId") { type = NavType.StringType },
                    navArgument("source") {
                        type = NavType.StringType
                        defaultValue = "notes"
                    },
                ),
            ) { entry ->
                val conversationId = requireNotNull(entry.arguments?.getString("conversationId"))
                val returnRoute = if (entry.arguments?.getString("source") == "words") "words" else "notes"
                WriteNoteScreen(
                    viewModel = viewModel,
                    conversationId = conversationId,
                    contentPadding = padding,
                    reviewAfterSave = false,
                    onBack = {
                        if (!navController.popBackStack(returnRoute, inclusive = false)) {
                            navController.navigate(returnRoute) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onSaved = {
                        if (!navController.popBackStack(returnRoute, inclusive = false)) {
                            navController.navigate(returnRoute) {
                                launchSingleTop = true
                            }
                        }
                    },
                )
            }
            composable(
                route = "review/{conversationId}",
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType }),
            ) { entry ->
                val conversationId = requireNotNull(entry.arguments?.getString("conversationId"))
                ReviewScreen(
                    viewModel = viewModel,
                    conversationId = conversationId,
                    contentPadding = padding,
                    onDone = { navController.navigate("words") },
                )
            }
            composable("notes") {
                NotesScreen(
                    viewModel = viewModel,
                    contentPadding = padding,
                    onNewNote = { conversationId -> navController.navigate("note/$conversationId?source=notes") },
                    onEditNote = { conversationId -> navController.navigate("note/$conversationId?source=notes") },
                )
            }
            composable("words") {
                WordsScreen(
                    viewModel = viewModel,
                    contentPadding = padding,
                    onOpenNote = { conversationId -> navController.navigate("note/$conversationId?source=words") },
                    onCreateLinkedNote = { wordId ->
                        scope.launch {
                            viewModel.startNote()
                                .onSuccess { conversationId ->
                                    viewModel.linkBorromeanWordToConversation(wordId, conversationId)
                                    navController.navigate("note/$conversationId?source=words")
                                }
                        }
                    },
                )
            }
            composable("privacy") {
                PrivacyScreen(viewModel = viewModel, contentPadding = padding)
            }
        }
    }
}

private data class BottomItem(
    val label: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isSelected: (String) -> Boolean,
)

private val bottomItems = listOf(
    BottomItem(
        label = "Notes",
        route = "notes",
        icon = Icons.AutoMirrored.Filled.List,
        isSelected = { route -> route == "notes" || route.startsWith("note") || route.startsWith("review") },
    ),
    BottomItem(
        label = "Words",
        route = "words",
        icon = Icons.AutoMirrored.Filled.Label,
        isSelected = { it == "words" },
    ),
    BottomItem(
        label = "Privacy",
        route = "privacy",
        icon = Icons.Default.Lock,
        isSelected = { it == "privacy" },
    ),
)
