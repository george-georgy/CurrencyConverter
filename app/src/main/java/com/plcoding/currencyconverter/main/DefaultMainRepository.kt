package com.plcoding.currencyconverter.main

import com.plcoding.currencyconverter.data.CurrencyApi
import com.plcoding.currencyconverter.data.models.CurrencyResponse
import com.plcoding.currencyconverter.util.Constants
import com.plcoding.currencyconverter.util.Resource
import java.lang.Exception
import javax.inject.Inject

class DefaultMainRepository @Inject constructor(
    private val api : CurrencyApi
) : MainRepository {


    override suspend fun getRates(): Resource<CurrencyResponse> {
        return try {

            val apiResponse = api.getRates(Constants.API_KEY)
            // we pass this result to  Resource.Success(result,null)
            val result = apiResponse.body()

            if (apiResponse.isSuccessful && result != null){
                Resource.Success(result)
            }else{
                Resource.Error(apiResponse.message())
            }

        }catch (e: Exception){
            Resource.Error(e.message ?: "An Error occurred")
        }
    }
}