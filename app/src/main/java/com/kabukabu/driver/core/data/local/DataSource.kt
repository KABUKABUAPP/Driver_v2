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

    val nigeriaStates = listOf(
        "Abia",
        "Adamawa",
        "Akwa Ibom",
        "Anambra",
        "Bauchi",
        "Bayelsa",
        "Benue",
        "Borno",
        "Cross River",
        "Delta",
        "Ebonyi",
        "Edo",
        "Ekiti",
        "Enugu",
        "Gombe",
        "Imo",
        "Jigawa",
        "Kaduna",
        "Kano",
        "Katsina",
        "Kebbi",
        "Kogi",
        "Kwara",
        "Lagos",
        "Nasarawa",
        "Niger",
        "Ogun",
        "Ondo",
        "Osun",
        "Oyo",
        "Plateau",
        "Rivers",
        "Sokoto",
        "Taraba",
        "Yobe",
        "Zamfara",
        "Federal Capital Territory"
    )

    val carCategories = listOf(
        "REGULAR", "TRICYCLE"
    )

    val guarantorRelationship = listOf(
        "FATHER",
        "MOTHER",
        "BROTHER",
        "SISTER",
        "SPOUSE",
        "FRIEND",
        "UNCLE",
        "AUNT",
        "COLLEAGUE"
    )


    val carColours = listOf(
        "Black", "Blue", "Yellow", "Orange", "Green", "Red", "Purple", "Green", "Ash", "Grey",
    )

}