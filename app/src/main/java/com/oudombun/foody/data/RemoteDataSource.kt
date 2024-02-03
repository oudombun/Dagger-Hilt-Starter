package com.oudombun.foody.data

import com.oudombun.foody.data.network.FoodRecipesApi
import com.oudombun.foody.models.FoodRecipe
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val foodRecipesApi: FoodRecipesApi
) {


    suspend fun getRecipe(queries:Map<String,String>):Response<FoodRecipe>{
        return foodRecipesApi.getRecipes(queries)
    }
}