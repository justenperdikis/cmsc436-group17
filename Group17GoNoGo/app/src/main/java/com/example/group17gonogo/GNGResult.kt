package com.example.group17gonogo

import java.io.Serializable

class GNGResult(private var time: Long, private var mode: GNGMode, private var status: TestStatus) : Serializable {

    fun getReactTime() : Long {
        return time
    }

    fun getGNGMode(): GNGMode {
        return mode
    }

    fun getTestStatus(): TestStatus {
        return status
    }
}