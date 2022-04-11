package com.plcoding.currencyconverter.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.currencyconverter.data.models.Rates
import com.plcoding.currencyconverter.util.DispatcherProvider
import com.plcoding.currencyconverter.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.round

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    private val dispatchers: DispatcherProvider
): ViewModel() {

    // Events
    sealed class CurrencyEvent {
        class Success(val resultText: String): CurrencyEvent()
        class Failure(val errorText: String): CurrencyEvent()
        object Loading : CurrencyEvent()
        object Empty : CurrencyEvent()
    }

    // stateFlow needs initial value (CurrencyEvent.Empty).
    private val _conversion = MutableStateFlow<CurrencyEvent>(CurrencyEvent.Empty)
    val conversion : StateFlow<CurrencyEvent> = _conversion

    fun convert(
        amountStr : String,
        toCurrency : String
    ){
        // amount in double
        val amountDouble = amountStr.toDoubleOrNull()

        // check nullability
        if (amountDouble == null){
            _conversion.value = CurrencyEvent.Failure("Not a valid amount")
            return
        }

        viewModelScope.launch(dispatchers.io) {

            _conversion.value = CurrencyEvent.Loading

            when(val ratesResponse = repository.getRates()) {

                is Resource.Error -> _conversion.value = CurrencyEvent.Failure(ratesResponse.message!!)

                is Resource.Success -> {

                    // getting rates from repo
                    val rates = ratesResponse.data!!.rates

                    val rate = getRateForCurrency(toCurrency, rates)

                    if(rate == null) {
                        _conversion.value = CurrencyEvent.Failure("Unexpected error")

                    } else {

                        val convertedCurrency = round(amountDouble * rate * 100) / 100
                        _conversion.value = CurrencyEvent.Success(
                            "$amountDouble EUR = $convertedCurrency $toCurrency"
                        )
                    }
                }
            }
        }
    }

    private fun getRateForCurrency(toCurrency: String, rates: Rates) =

        when (toCurrency) {
            "BTC" -> rates.BTC
            "CAD" -> rates.CAD
            "EGP" -> rates.EGP
            "USD" -> rates.USD
            else -> null
        }




}