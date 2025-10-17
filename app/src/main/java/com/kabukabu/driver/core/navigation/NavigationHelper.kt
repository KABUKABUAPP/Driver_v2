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
        navController.navigate(Screen.KabuRideAccountDeclined.route)
    }

    fun navToKabuRideDocumentsReuploadScreen(id: String) {
        navController.navigate(Screen.KabuRideDocReUpload.route+"/$id")
    }

    fun navToKabuRideInspection() {
        navController.navigate(Screen.KabuRideInspection.route)
    }


}
