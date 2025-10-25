package com.kabukabu.driver.core.data.local

class LocalDataSource {


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

    val carColourModal = listOf<CarColourModal>(
        CarColourModal(0xFF151515, "Black"),
        CarColourModal(0xFF2A2AB5, "Navy Blue"),
        CarColourModal(0xFF33699F, "Deep Blue"),
        CarColourModal(0xFF4169E1, "Blue"),
        CarColourModal(0xFF7AC5E4, "Sky Blue"),
        CarColourModal(0xFFEFB512, "Yellow"),
        CarColourModal(0xFFFEA80C, "Orange"),
        CarColourModal(0xFF51E751, "Green"),
        CarColourModal(0xFF029B02, "Deep Green"),
        CarColourModal(0xFF52CEA1, "Mint Green"),
        CarColourModal(0xFFD44F44, "Red"),
        CarColourModal(0xFFB80E22, "Wine"),
        CarColourModal(0xFF9A5001, "Brown"),
        CarColourModal(0xFF6D3801, "Chocolate"),
        CarColourModal(0xFF6F4E37, "Coffee"),
        CarColourModal(0xFF8B6C03, "Gold"),
        CarColourModal(0xFFD7B708, "Light Gold"),
        CarColourModal(0xFF920592, "Purple"),
        CarColourModal(0xFFFDB7C3, "Pink"),
        CarColourModal(0xFFFCED2A, "Lemon"),
        CarColourModal(0xFFF9F9CF, "Biege"),
        CarColourModal(0xFFFFFDD0, "Cream"),
        CarColourModal(0xFFBFBEBE, "Ash"),
        CarColourModal(0xFFADADAD, "Grey"),
        CarColourModal(0xFFCECECE, "Silver"),
        CarColourModal(0xFFF1F1F1, "White"),
        )

}