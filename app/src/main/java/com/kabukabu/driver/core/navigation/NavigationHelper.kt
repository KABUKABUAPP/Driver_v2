package com.kabukabu.driver.core.navigation

import androidx.navigation.NavHostController


class Navigator(private val navController: NavHostController) {

    fun navigateUp() {
        navController.navigateUp()
    }


    fun navToKabuRideCarDetails() {
        navController.navigate(Screen.KabuRideCarDetails.route)
    }

    fun navToKabuRideCarDocsUpload() {
        navController.navigate(Screen.KabuRideCarDocsUpload.route)
    }

    fun navToKabuRideGuarantorDetailsScreen() {
        navController.navigate(Screen.KabuRideGuarantorDetails.route)
    }

    fun navToKabuRidePendingAccountApprovalScreen() {
        navController.navigate(Screen.KabuRidePendingApproval.route)
    }

    fun navToKabuRideAccountDeclinedScreen() {
        navController.navigate(Screen.KabuRideAccountDeclined.route){
            popUpTo(Screen.KabuRideAccountDeclined.route){ inclusive = true}
        }
    }

    fun navToKabuRideDocumentsReuploadScreen(id: String, title: String) {
        // Make sure to encode the title if it contains spaces or special chars
        val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
        navController.navigate("${Screen.KabuRideDocReUpload.route}/$id/$encodedTitle")
    }

    fun navToKabuRideInspection() {
        navController.navigate(Screen.KabuRideInspection.route)
    }


}
