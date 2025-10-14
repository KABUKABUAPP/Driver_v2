package com.kabukabu.driver.core.data.remote.hub

import com.kabukabu.driver.features.auth.data.entity.response.InspectionHubsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface HubApiService {

    @GET("hub/fetch-hubs")
    suspend fun fetchHubs(
        @Query("state") state: String
    ): InspectionHubsResponse

}
