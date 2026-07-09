package com.atlas.agent.device

object CpuProvider {

    fun getCpuCores(): String {
        return Runtime.getRuntime().availableProcessors().toString() + " Cores"
    }

}