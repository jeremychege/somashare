package com.example.somashare.navigation

sealed class Screen(val route: String) {
    // Authentication Flow
    object Login : Screen("login")
    object Register : Screen("register")
    object Onboarding : Screen("onboarding")

    // Main App Flow
    object Home : Screen("home")
    object Search : Screen("search")
    object Recommendations : Screen("recommendations")
    object RecentlyOpened : Screen("recently_opened")

    // Details Screens
    object UnitDetails : Screen("unit_details/{unitId}") {
        fun createRoute(unitId: String) = "unit_details/$unitId"
    }

    object PaperDetails : Screen("paper_details/{paperId}") {
        fun createRoute(paperId: String) = "paper_details/$paperId"
    }

    object PdfViewer : Screen("pdf_viewer/{paperId}") {
        fun createRoute(paperId: String) = "pdf_viewer/$paperId"
    }

    // Profile & Settings
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
    object Favorites : Screen("favorites")

    // Upload
    object Upload : Screen("upload")
    object UploadSuccess : Screen("upload_success/{paperId}") {
        fun createRoute(paperId: String) = "upload_success/$paperId"
    }
}