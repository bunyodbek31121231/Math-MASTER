package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.di.AppContainer
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.admin.AdminEditorScreen
import com.example.ui.screens.admin.AdminHistoryScreen
import com.example.ui.screens.admin.AdminImportScreen
import com.example.ui.screens.admin.AdminLoginScreen
import com.example.ui.screens.admin.AdminPreviewScreen
import com.example.ui.screens.admin.AdminSearchScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.maxsus.MaxsusGateScreen
import com.example.ui.screens.maxsus.MaxsusHomeScreen
import com.example.ui.screens.maxsus.MaxsusTopicDetailScreen
import com.example.ui.screens.practice.PracticeQuestionScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.progress.ProgressScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.test.ActiveTestScreen
import com.example.ui.screens.test.TestHistoryScreen
import com.example.ui.screens.test.TestResultScreen
import com.example.ui.screens.test.TestSetupScreen
import com.example.ui.screens.topics.TopicsScreen
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.MaxsusViewModel
import com.example.ui.viewmodel.PracticeViewModel
import com.example.ui.viewmodel.ProfileViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.TestViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    container: AppContainer
) {
    val activeUserId by container.userRepository.activeUserId.collectAsState(initial = null)

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        // 1. Splash Screen
        composable(Routes.SPLASH) {
            SplashScreen(
                activeUserId = activeUserId,
                onNavigateNext = { isLoggedIn ->
                    navController.navigate(if (isLoggedIn) Routes.HOME else Routes.AUTH) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // 2. Auth Screen
        composable(Routes.AUTH) {
            val authVm: AuthViewModel = viewModel(
                factory = AuthViewModel.provideFactory(container.userRepository)
            )
            AuthScreen(
                viewModel = authVm,
                onAuthSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // 3. Home Screen
        composable(Routes.HOME) {
            val homeVm: HomeViewModel = viewModel(
                factory = HomeViewModel.provideFactory(container.userRepository, container.mathRepository)
            )
            HomeScreen(
                viewModel = homeVm,
                onContinuePractice = {
                    navController.navigate(Routes.practiceRoute())
                },
                onTopicsClick = {
                    navController.navigate(Routes.TOPICS)
                },
                onTestClick = {
                    navController.navigate(Routes.TEST_SETUP)
                },
                onProgressClick = {
                    navController.navigate(Routes.PROGRESS)
                },
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
                },
                onMaxsusClick = {
                    navController.navigate(Routes.MAXSUS_GATE)
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        // 4. Practice Question Screen
        composable(
            route = Routes.PRACTICE,
            arguments = listOf(
                navArgument("topicId") { type = NavType.StringType; defaultValue = "" },
                navArgument("difficulty") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId")
            val difficulty = backStackEntry.arguments?.getString("difficulty")

            val practiceVm: PracticeViewModel = viewModel(
                factory = PracticeViewModel.provideFactory(
                    container.mathRepository,
                    container.userRepository,
                    topicId,
                    difficulty
                )
            )
            PracticeQuestionScreen(
                viewModel = practiceVm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5. Topics Screen
        composable(Routes.TOPICS) {
            val topics by container.mathRepository.allTopics.collectAsState(initial = emptyList())
            TopicsScreen(
                topics = topics,
                onTopicSelected = { topic ->
                    navController.navigate(Routes.practiceRoute(topicId = topic.id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 6. Test Setup Screen
        composable(Routes.TEST_SETUP) {
            val topics by container.mathRepository.allTopics.collectAsState(initial = emptyList())
            TestSetupScreen(
                topics = topics,
                onStartTest = { topicId, difficulty, count ->
                    navController.navigate(
                        Routes.activeTestRoute(
                            topicId = topicId,
                            difficulty = difficulty,
                            count = count,
                            isMaxsus = false
                        )
                    )
                },
                onViewHistory = {
                    navController.navigate(Routes.TEST_HISTORY)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 7. Active Test Screen (with back interception and FLAG_SECURE)
        composable(
            route = Routes.ACTIVE_TEST,
            arguments = listOf(
                navArgument("topicId") { type = NavType.StringType; defaultValue = "" },
                navArgument("difficulty") { type = NavType.StringType; defaultValue = "MEDIUM" },
                navArgument("count") { type = NavType.IntType; defaultValue = 10 },
                navArgument("isMaxsus") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId")
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "MEDIUM"
            val count = backStackEntry.arguments?.getInt("count") ?: 10
            val isMaxsus = backStackEntry.arguments?.getBoolean("isMaxsus") ?: false

            val testVm: TestViewModel = viewModel(
                factory = TestViewModel.provideFactory(
                    container.testRepository,
                    container.userRepository,
                    topicId,
                    difficulty,
                    count,
                    isMaxsus
                )
            )
            ActiveTestScreen(
                viewModel = testVm,
                onTestFinished = { testId ->
                    navController.navigate(Routes.testResultRoute(testId)) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                },
                onExitWithoutSaving = {
                    navController.popBackStack()
                }
            )
        }

        // 8. Test Result Screen
        composable(
            route = Routes.TEST_RESULT,
            arguments = listOf(navArgument("testId") { type = NavType.StringType })
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getString("testId") ?: ""
            TestResultScreen(
                testId = testId,
                testRepository = container.testRepository,
                onReturnHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onRetakeTest = {
                    navController.navigate(Routes.TEST_SETUP) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }

        // 9. Test History Screen
        composable(Routes.TEST_HISTORY) {
            TestHistoryScreen(
                userId = activeUserId ?: "guest",
                testRepository = container.testRepository,
                onTestClick = { testId ->
                    navController.navigate(Routes.testResultRoute(testId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 10. Progress Screen
        composable(Routes.PROGRESS) {
            val profileVm: ProfileViewModel = viewModel(
                factory = ProfileViewModel.provideFactory(
                    container.userRepository,
                    container.mathRepository,
                    container.testRepository
                )
            )
            ProgressScreen(
                viewModel = profileVm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 11. Profile Screen
        composable(Routes.PROFILE) {
            val profileVm: ProfileViewModel = viewModel(
                factory = ProfileViewModel.provideFactory(
                    container.userRepository,
                    container.mathRepository,
                    container.testRepository
                )
            )
            ProfileScreen(
                viewModel = profileVm,
                onViewTestHistory = {
                    navController.navigate(Routes.TEST_HISTORY)
                },
                onOpenSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 12. Settings Screen
        composable(Routes.SETTINGS) {
            val settingsVm: SettingsViewModel = viewModel(
                factory = SettingsViewModel.provideFactory(
                    container.userPreferences,
                    container.userRepository
                )
            )
            SettingsScreen(
                viewModel = settingsVm,
                onLoggedOut = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                onOpenDevGenerator = {
                    navController.navigate(Routes.DEV_GENERATOR)
                },
                onOpenAdmin = {
                    navController.navigate(Routes.ADMIN_LOGIN)
                }
            )
        }

        // 13. MAXSUS Passcode Gate Screen
        composable(Routes.MAXSUS_GATE) {
            val maxsusVm: MaxsusViewModel = viewModel(
                factory = MaxsusViewModel.provideFactory(container.maxsusRepository)
            )
            MaxsusGateScreen(
                viewModel = maxsusVm,
                onUnlocked = {
                    navController.navigate(Routes.MAXSUS_HOME) {
                        popUpTo(Routes.MAXSUS_GATE) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 14. MAXSUS Home Screen
        composable(Routes.MAXSUS_HOME) {
            val maxsusVm: MaxsusViewModel = viewModel(
                factory = MaxsusViewModel.provideFactory(container.maxsusRepository)
            )
            MaxsusHomeScreen(
                viewModel = maxsusVm,
                onTopicSelected = { topicId ->
                    navController.navigate(Routes.maxsusTopicRoute(topicId))
                },
                onStartSpecialTest = { topicId ->
                    navController.navigate(
                        Routes.activeTestRoute(
                            topicId = topicId,
                            difficulty = "HARD",
                            count = 10,
                            isMaxsus = true
                        )
                    )
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 15. MAXSUS Topic Detail Screen
        composable(
            route = Routes.MAXSUS_TOPIC,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId") ?: ""
            val maxsusVm: MaxsusViewModel = viewModel(
                factory = MaxsusViewModel.provideFactory(container.maxsusRepository)
            )
            MaxsusTopicDetailScreen(
                topicId = topicId,
                viewModel = maxsusVm,
                onPracticeExamples = { tId ->
                    navController.navigate(Routes.practiceRoute(topicId = tId, difficulty = "HARD"))
                },
                onStartTest = { tId ->
                    navController.navigate(
                        Routes.activeTestRoute(
                            topicId = tId,
                            difficulty = "HARD",
                            count = 10,
                            isMaxsus = true
                        )
                    )
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 16. Developer Generator Screen (Admin engine)
        composable(Routes.DEV_GENERATOR) {
            val devVm: com.example.ui.screens.dev.DevGeneratorViewModel = viewModel(
                factory = com.example.ui.screens.dev.DevGeneratorViewModel.provideFactory(
                    generationService = container.problemGenerationService,
                    problemDao = container.database.problemDao(),
                    jobDao = container.database.generationJobDao()
                )
            )
            com.example.ui.screens.dev.DevGeneratorScreen(
                viewModel = devVm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Admin Panel ---
        composable(Routes.ADMIN_LOGIN) {
            val adminVm: com.example.ui.admin.AdminViewModel = viewModel(
                factory = com.example.ui.admin.AdminViewModel.provideFactory(
                    container.adminAuthRepository,
                    container.adminRepository,
                    container.database
                )
            )
            AdminLoginScreen(
                viewModel = adminVm,
                onLoginSuccess = {
                    navController.navigate(Routes.ADMIN_DASHBOARD) {
                        popUpTo(Routes.ADMIN_LOGIN) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_DASHBOARD) {
            val adminVm: com.example.ui.admin.AdminViewModel = viewModel(
                factory = com.example.ui.admin.AdminViewModel.provideFactory(
                    container.adminAuthRepository,
                    container.adminRepository,
                    container.database
                )
            )
            if (!container.adminAuthRepository.isLoggedIn()) {
                navController.navigate(Routes.ADMIN_LOGIN)
            } else {
                AdminDashboardScreen(
                    viewModel = adminVm,
                    onNavigateToImport = { navController.navigate(Routes.ADMIN_IMPORT) },
                    onNavigateToHistory = { navController.navigate(Routes.ADMIN_HISTORY) },
                    onNavigateToSearch = { navController.navigate(Routes.ADMIN_SEARCH) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.ADMIN_SEARCH) {
            val adminVm: com.example.ui.admin.AdminViewModel = viewModel(
                factory = com.example.ui.admin.AdminViewModel.provideFactory(
                    container.adminAuthRepository,
                    container.adminRepository,
                    container.database
                )
            )
            if (!container.adminAuthRepository.isLoggedIn()) {
                navController.navigate(Routes.ADMIN_LOGIN)
            } else {
                AdminSearchScreen(
                    viewModel = adminVm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.ADMIN_IMPORT) {
            val adminVm: com.example.ui.admin.AdminViewModel = viewModel(
                factory = com.example.ui.admin.AdminViewModel.provideFactory(
                    container.adminAuthRepository,
                    container.adminRepository,
                    container.database
                )
            )
            if (!container.adminAuthRepository.isLoggedIn()) {
                navController.navigate(Routes.ADMIN_LOGIN)
            } else {
                AdminImportScreen(
                    viewModel = adminVm,
                    onNavigateToPreview = { navController.navigate(Routes.ADMIN_PREVIEW) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.ADMIN_PREVIEW) {
            val adminVm: com.example.ui.admin.AdminViewModel = viewModel(
                factory = com.example.ui.admin.AdminViewModel.provideFactory(
                    container.adminAuthRepository,
                    container.adminRepository,
                    container.database
                )
            )
            if (!container.adminAuthRepository.isLoggedIn()) {
                navController.navigate(Routes.ADMIN_LOGIN)
            } else {
                AdminPreviewScreen(
                    viewModel = adminVm,
                    onNavigateToEditor = { index -> 
                        navController.navigate("admin_editor/$index")
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onImportFinished = {
                        navController.navigate(Routes.ADMIN_DASHBOARD) {
                            popUpTo(Routes.ADMIN_IMPORT) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(
            route = "admin_editor/{index}",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            val adminVm: com.example.ui.admin.AdminViewModel = viewModel(
                factory = com.example.ui.admin.AdminViewModel.provideFactory(
                    container.adminAuthRepository,
                    container.adminRepository,
                    container.database
                )
            )
            if (!container.adminAuthRepository.isLoggedIn()) {
                navController.navigate(Routes.ADMIN_LOGIN)
            } else {
                AdminEditorScreen(
                    index = index,
                    viewModel = adminVm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.ADMIN_HISTORY) {
            val adminVm: com.example.ui.admin.AdminViewModel = viewModel(
                factory = com.example.ui.admin.AdminViewModel.provideFactory(
                    container.adminAuthRepository,
                    container.adminRepository,
                    container.database
                )
            )
            if (!container.adminAuthRepository.isLoggedIn()) {
                navController.navigate(Routes.ADMIN_LOGIN)
            } else {
                AdminHistoryScreen(
                    viewModel = adminVm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
