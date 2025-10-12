package com.kabukabu.driver.features.auth.data.entity.req_body
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.File

@JsonClass(generateAdapter = true)
data class DriverGuarantorDetailsRequest(
    @Json(name = "guarantor_image")
    val guarantorImage: File,
    @Json(name = "guarantor_full_name")
    val guarantorFullName: String,
    @Json(name = "guarantor_relationship")
    val guarantorRelationship: String,
    @Json(name = "guarantor_house_address")
    val guarantorHouseAddress: String,
    @Json(name = "guarantor_city")
    val guarantorCity: String,
    @Json(name = "guarantor_state")
    val guarantorState: String,
    @Json(name = "guarantor_phone_number")
    val guarantorPhoneNumber: String,
    @Json(name = "guarantor_email")
    val guarantorEmail: String,
    @Json(name = "referral_code")
    val referralCode: String,
    @Json(name = "sharp_program_type")
    val sharpProgramType: String
)
