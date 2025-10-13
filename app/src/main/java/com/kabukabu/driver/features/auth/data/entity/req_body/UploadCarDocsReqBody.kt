package com.kabukabu.driver.features.auth.data.entity.req_body

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.File

@JsonClass(generateAdapter = true)
data class UploadCarDocsReqBody(
    @Json(name = "driver_licence_number") val driverLicenceNumber: String?=null,
    @Json(name = "car_insurance_number") val carInsuranceNumber: String?=null,
    @Json(name = "vehicle_licence_number") val vehicleLicenceNumber: String?=null,
    @Json(name = "proof_of_ownership_number") val proofOfOwnershipNumber: String?=null,
    @Json(name = "road_worthiness_certification_number") val roadWorthinessCertificationNumber: String?=null,
    @Json(name = "hackney_permit_number") val hackneyPermitNumber: String?=null,

    @Json(name = "driver_licence") val driverLicence: File?,
    @Json(name = "vehicle_licence") val vehicleLicence: File?,
    @Json(name = "insurance_certificate") val insuranceCertificate: File?,
    @Json(name = "proof_of_ownership") val proofOfOwnership: File?,
    @Json(name = "road_worthiness_certification") val roadWorthinessCertification: File?,
    @Json(name = "hackney_permit") val hackneyPermit: File?
)
