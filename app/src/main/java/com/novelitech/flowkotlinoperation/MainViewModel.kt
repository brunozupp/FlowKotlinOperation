package com.novelitech.flowkotlinoperation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
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

        // FLATTENING LISTS

        val flow1 = flow {
            emit(1)
            delay(500L)
            emit(2)
        }

        viewModelScope.launch {
            // concat waits each item to finish it's block to run the next.
            // merge doesn't wait, it runs all the items in the same time (.flatMapMerge)
            flow1.flatMapConcat { value ->
                flow {
                    emit(value + 1) // The value 1 will enter here first and execute value(1) + 1 and then value(1) + 2
                    delay(500L)
                    emit(value + 2)
                }
            }.collect { value ->
                println("The value is $value")
            }
        }

        // One use case to the .flatMapConcat
//        val flowIdsRecipes = (1..5).asFlow()
//
//        viewModelScope.launch {
//            flowIdsRecipes.flatMapConcat { id ->
//
//                // It would be a function that access my local database to get the recipe by Id and
//                // returns a flow from the type of the RecipeModel
//                // getRecipeById(id)
//            }.collect { value ->
//                println("The value is $value")
//            }
//        }

        // .flatMapLatest does the same as concatLatest.

        // Now will work with executions in parallel. There are some ways I can go to do so:

        val flow2 = flow {
            delay(250L)
            emit("Appetizer")
            delay(1000L)
            emit("Main Dish")
            delay(100L)
            emit("Dessert")
        }

        // using buffer()
        viewModelScope.launch {
            flow2.onEach {
                println("FLOW: $it is delivered")
            }
                .buffer() // it makes sure that the execution in flow2 will happens in a different corountine than .collect execution
                .collect {
                    println("FLOW: Now eating $it")
                    delay(1500L)
                    println("FLOW: FINISHED eating $it")
                }
        }

        // using .conflate()
        viewModelScope.launch {
            flow2.onEach {
                println("FLOW: $it is delivered")
            }
                .conflate() // it goes to the latest emittion as the .latest methods does. So if Main Dish and Dessert is delivered in the same time and
                // the .collect is still executing the first emittion, the Main Dish flow is erased and it continues directly to the Dessert
                .collect {
                    println("FLOW: Now eating $it")
                    delay(1500L)
                    println("FLOW: FINISHED eating $it")
                }
        }

        // A third option could be use the .collectLatest. It will not finish the execution if the block
        // inside .collectLatest if another emittion is set during the period in delay. It doesn't have
        // the separated corountine.
        viewModelScope.launch {
            flow2.onEach {
                println("FLOW .collectLatest: $it is delivered")
            }
                .collectLatest {
                    println("FLOW .collectLatest: Now eating $it")
                    delay(1500L)
                    println("FLOW .collectLatest: FINISHED eating $it")
                }
        }
    }
}