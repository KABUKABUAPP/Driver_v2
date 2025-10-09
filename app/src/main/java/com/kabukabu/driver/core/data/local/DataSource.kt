package com.kabukabu.driver.core.data.local

class LocalDataSource {

    fun loadInspectionHubItems(): List<InspectionHubData> {
        return listOf(
            InspectionHubData(
                name = "Emeka Anyawu",
                address = "3 Ajasin Crescent, Idumota Lagos",
                closingHour = "Closes 5pm",
                phoneNumber = "09098887655"
            ),
            InspectionHubData(
                name = "Itoro Ibiono",
                address = "5 Adekunle Crescent, Idumota Lagos",
                closingHour = "Closes 6pm",
                phoneNumber = "09098122655"
            ),
        )
    }

}