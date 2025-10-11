package com.kabukabu.driver.features.auth.data.entity.req_body

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.File

@JsonClass(generateAdapter = true)
data class UploadDriverAndCarDocsReqBody(
    @Json(name = "driver_licence_number") val driverLicenceNumber: String,
    @Json(name = "car_insurance_number") val carInsuranceNumber: String,
    @Json(name = "vehicle_licence_number") val vehicleLicenceNumber: String,
    @Json(name = "proof_of_ownership_number") val proofOfOwnershipNumber: String,
    @Json(name = "road_worthiness_certification_number") val roadWorthinessCertificationNumber: String,
    @Json(name = "hackney_permit_number") val hackneyPermitNumber: String,

    @Json(name = "driver_licence") val driverLicence: File,
    @Json(name = "vehicle_licence") val vehicleLicence: File,
    @Json(name = "insurance_certificate") val insuranceCertificate: File,
    @Json(name = "proof_of_ownership") val proofOfOwnership: File?,
    @Json(name = "road_worthiness_certification") val roadWorthinessCertification: File,
    @Json(name = "hackney_permit") val hackneyPermit: File?
)
