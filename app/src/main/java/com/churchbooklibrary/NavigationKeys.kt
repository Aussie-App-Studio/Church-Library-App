package com.churchbooklibrary

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object SelectBooks : Screen("select_books")
    object BorrowerInfo : Screen("borrower_info")
    object BorrowSuccess : Screen("borrow_success")
    object ReturnBooks : Screen("return_books")
    object AdminPin : Screen("admin_pin")
    object AdminDashboard : Screen("admin_dashboard")
}

