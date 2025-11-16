package com.example.somashare.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.somashare.ui.auth.LoginScreen
import com.example.somashare.ui.auth.RegisterScreen
import com.example.somashare.ui.auth.OnboardingScreen
import com.example.somashare.ui.home.HomeScreen
import com.example.somashare.ui.search.SearchScreen
import com.example.somashare.ui.recommendations.RecommendationsScreen
import com.example.somashare.ui.recentlyopened.RecentlyOpenedScreen
import com.example.somashare.ui.unitdetails.UnitDetailsScreen
import com.example.somashare.ui.pdfviewer.PdfViewerScreen
import com.example.somashare.ui.profile.ProfileScreen
import com.example.somashare.ui.profile.EditProfileScreen
import com.example.somashare.ui.profile.SettingsScreen
import com.example.somashare.ui.favorites.FavoritesScreen
import com.example.somashare.ui.upload.UploadScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = determineStartDestination()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ============================================
        // AUTHENTICATION FLOW
        // ============================================

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Check if user needs onboarding
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    // New user needs onboarding
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    // Onboarding done, go to home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // ============================================
        // MAIN APP FLOW
        // ============================================

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToRecommendations = {
                    navController.navigate(Screen.Recommendations.route)
                },
                onNavigateToRecentlyOpened = {
                    navController.navigate(Screen.RecentlyOpened.route)
                },
                onNavigateToUnitDetails = { unitId ->
                    navController.navigate(Screen.UnitDetails.createRoute(unitId))
                },
                onNavigateToPdfViewer = { paperId ->
                    navController.navigate(Screen.PdfViewer.createRoute(paperId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToUpload = {
                    navController.navigate(Screen.Upload.route)
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                navController = navController,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToUnitDetails = { unitId ->
                    navController.navigate(Screen.UnitDetails.createRoute(unitId))
                },
                onNavigateToPdfViewer = { paperId ->
                    navController.navigate(Screen.PdfViewer.createRoute(paperId))
                }
            )
        }

        composable(Screen.Recommendations.route) {
            RecommendationsScreen(
                navController = navController,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToUnitDetails = { unitId ->
                    navController.navigate(Screen.UnitDetails.createRoute(unitId))
                }
            )
        }

        composable(Screen.RecentlyOpened.route) {
            RecentlyOpenedScreen(
                navController = navController,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToPdfViewer = { paperId ->
                    navController.navigate(Screen.PdfViewer.createRoute(paperId))
                }
            )
        }

        // ============================================
        // DETAIL SCREENS (with parameters)
        // ============================================

        composable(
            route = Screen.UnitDetails.route,
            arguments = listOf(
                navArgument("unitId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val unitId = backStackEntry.arguments?.getString("unitId") ?: ""
            UnitDetailsScreen(
                unitId = unitId,
                navController = navController,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToPdfViewer = { paperId ->
                    navController.navigate(Screen.PdfViewer.createRoute(paperId))
                }
            )
        }

        composable(
            route = Screen.PdfViewer.route,
            arguments = listOf(
                navArgument("paperId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val paperId = backStackEntry.arguments?.getString("paperId") ?: ""
            PdfViewerScreen(
                paperId = paperId,
                navController = navController,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // ============================================
        // PROFILE & SETTINGS
        // ============================================

        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route)
                },
                onLogout = {
                    // Clear back stack and go to login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                navController = navController,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onSaveSuccess = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                navController = navController,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToUnitDetails = { unitId ->
                    navController.navigate(Screen.UnitDetails.createRoute(unitId))
                }
            )
        }

        // ============================================
        // UPLOAD FLOW
        // ============================================

        composable(Screen.Upload.route) {
            UploadScreen(
                navController = navController,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onUploadSuccess = { paperId ->
                    // Navigate to the uploaded paper
                    navController.navigate(Screen.PdfViewer.createRoute(paperId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
    }
}

// Helper function to determine start destination based on auth state
private fun determineStartDestination(): String {
    val currentUser = FirebaseAuth.getInstance().currentUser
    return if (currentUser != null) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }
}