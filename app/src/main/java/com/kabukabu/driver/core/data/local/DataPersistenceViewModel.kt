package com.kabukabu.driver.core.data.local

import androidx.lifecycle.ViewModel
import com.kabukabu.driver.features.auth.data.entity.req_body.DriverDetailsReqBody
import com.kabukabu.driver.features.auth.data.entity.response.InspectionHubsResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DataPersistenceViewModel: ViewModel() {


    private val _carBrands = MutableStateFlow<List<String>>(emptyList())
    val carBrands: StateFlow<List<String>> = _carBrands.asStateFlow()

    private val _inspectionsHubs = MutableStateFlow<InspectionHubsResponse?>(null)
    val inspectionsHubs: StateFlow<InspectionHubsResponse?> = _inspectionsHubs.asStateFlow()

    private val _driverDetailsReqBody = MutableStateFlow<DriverDetailsReqBody?>(null)
    val driverDetailsReqBody: StateFlow<DriverDetailsReqBody?> = _driverDetailsReqBody.asStateFlow()


    fun setCarBrands(carBrands: List<String>) {
        _carBrands.value = carBrands
    }

    fun setInspectionHubs(inspectionHubsResponse: InspectionHubsResponse?){
        _inspectionsHubs.value = inspectionHubsResponse
    }

    fun setDriverDetailsReqBody(driverDetailsReqBody: DriverDetailsReqBody?){
        _driverDetailsReqBody.value = driverDetailsReqBody
    }


}