package com.example.group17_gonogo

import java.io.Serializable

class GNGResult: Serializable {
    private var time: Long = 0
    private var mode: GNGMode = GNGMode.NONE
    private var status: TestStatus = TestStatus.TBD

    constructor(time: Long, mode: GNGMode, status: TestStatus) {
        this.time = time
        this.mode = mode
        this.status = status
    }

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