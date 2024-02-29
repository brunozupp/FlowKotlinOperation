package com.novelitech.flowkotlinoperation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val countDownFlow = flow<Int> {

        val startingValue = 10
        var currentValue = startingValue

        emit(currentValue)

        while(currentValue > 0) {
            delay(1000L)

            currentValue--

            emit(currentValue)
        }
    }

    init {
        collectFlow()
    }

    private fun collectFlow() {

        // I could use this too if the only thing I wanted is to iterate on each value
//        countDownFlow.onEach {
//            println(it)
//        }.launchIn(viewModelScope)

        // To each execution of countDownFlow, it will wait the seconds it needs to be solved
        // And just one execution runs at a time
        viewModelScope.launch {
            countDownFlow
                .filter { time -> // One example of operator
                    time % 2 == 0
                }
                .map { time ->
                    time * time
                }
                .onEach { time ->
                    // Do something in here. Return nothing, it just iterates on each item
                }
                .collect { time ->
                    println("The current time is $time")
                }

            // TERMINAL OPERATORS - it terminates the flow. It basically takes the result of the flow and does something with this
            val count = countDownFlow
                .filter { time -> // One example of operator
                    time % 2 == 0
                }
                .map { time ->
                    time * time
                }
                .count { time -> // It will return the length of items that match the previous condition
                    time % 2 == 0
                }

            println("The count is $count")

            val total = countDownFlow.reduce { accumulator, value ->
                accumulator + value
            }

            println("The total of reduce is $total")

            // The difference with .reduce is that .fold needs a initialValue
            val totalFold = countDownFlow.fold(100) { acc, value ->
                acc + value
            }

            println("The totalFold is = $totalFold")
        }



    }
}