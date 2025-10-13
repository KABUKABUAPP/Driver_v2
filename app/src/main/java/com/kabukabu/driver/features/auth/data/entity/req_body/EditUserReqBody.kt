package com.kabukabu.driver.features.auth.data.entity.req_body

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.File


/*
this endpoint is used as a makeshift for image upload
during onboarding
 */
@JsonClass(generateAdapter = true)
data class EditUserProfileReqBody(
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "phone_number") val phoneNumber: String?,
    @Json(name = "email") val email: String?,
    @Json(name = "profile_image") val profileImage: File?, // If sending multipart file, handle separately as MultipartBody.Part

    @Json(name = "next_of_kin_full_name") val nextOfKinFullName: String?,
    @Json(name = "next_of_kin_relationship") val nextOfKinRelationship: String?,
    @Json(name = "next_of_kin_phone_number") val nextOfKinPhoneNumber: String?,

    @Json(name = "guarantor_name") val guarantorName: String?,
    @Json(name = "guarantor_address") val guarantorAddress: String?,
    @Json(name = "guarantor_phone_number") val guarantorPhoneNumber: String?,
    @Json(name = "guarantor_image") val guarantorImage: File?, // Also handle as MultipartBody.Part if file

//    @Json(name = "preferred_destination") val preferredDestination: PreferredDestination?,
    @Json(name = "gender") val gender: String?
)

//@JsonClass(generateAdapter = true)
//data class PreferredDestination(
//    @Json(name = "is_active") val isActive: Boolean?,
//    @Json(name = "coordinate") val coordinate: List<Double>?, // [latitude, longitude]
//    @Json(name = "address") val address: String?
//)
