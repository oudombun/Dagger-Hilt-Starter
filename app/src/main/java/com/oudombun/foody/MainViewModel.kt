package com.oudombun.foody

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.oudombun.foody.data.Repository
import com.oudombun.foody.models.FoodRecipe
import com.oudombun.foody.util.NetworkResult
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel @ViewModelInject constructor(
    application: Application,
    private val repository: Repository
):AndroidViewModel(application) {

    var recipesResponse :MutableLiveData<NetworkResult<FoodRecipe>> = MutableLiveData()

    fun getRecipe(queries:Map<String,String>) = viewModelScope.launch {
        getRecipeSafeCall(queries)
    }

    private suspend fun getRecipeSafeCall(queries:Map<String,String>) {
        recipesResponse.value = NetworkResult.Loading()
        if(hasInternetConnection()){
            try {
                val response = repository.remote.getRecipe(queries)
                recipesResponse.value = handleFoodRecipeResponse(response)
            }catch (e:Exception){
                recipesResponse.value = NetworkResult.Error(e.message.toString())
            }
        }else{
            recipesResponse.value = NetworkResult.Error("No Internet Connections.")
        }
    }

    private fun handleFoodRecipeResponse(response: Response<FoodRecipe>): NetworkResult<FoodRecipe>? {
        when{
            response.message().toString().contains("timeout") -> {
                return  NetworkResult.Error("Timeout")
            }
            response.code()==402 ->{
                return  NetworkResult.Error("Api Key Limited")
            }
            response.body()!!.results.isEmpty()->{
                return  NetworkResult.Error("Recipe Not Found")
            }
            response.isSuccessful ->{
                val foodRecipe = response.body()
                return NetworkResult.Success(foodRecipe!!)
            }
            else ->{
                return  NetworkResult.Error(response.message().toString())
            }
        }
    }

    private fun hasInternetConnection():Boolean{
        val connectivityManager = getApplication<Application>()
            .getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager
        val activeNetwork= connectivityManager.activeNetwork?:return false
        val capabilities= connectivityManager.getNetworkCapabilities(activeNetwork)?:return false
        return when{
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}