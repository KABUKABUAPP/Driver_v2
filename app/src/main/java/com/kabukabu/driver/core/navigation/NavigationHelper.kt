package com.kabukabu.driver.core.navigation

import androidx.navigation.NavHostController


class Navigator(private val navController: NavHostController) {

    fun navToKabuRideCarDetails() {
        navController.navigate(Screen.KabuRideCarDetails.route)
    }

    fun navToKabuDocumentsUpload() {
        navController.navigate(Screen.KabuRideDocumentUpload.route)
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

    fun navToKabuRideDocumentsReuploadScreen() {
        navController.navigate(Screen.KabuRideDocumentsReUpload.route)
    }

    fun navToKabuRideInspection() {
        navController.navigate(Screen.KabuRideInspection.route)
    }


}
