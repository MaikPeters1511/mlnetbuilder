package com.mlnet.builder.services

import com.google.gson.GsonBuilder
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.mlnet.builder.model.*
import java.io.File

@Service(Service.Level.PROJECT)
class MbConfigService(private val project: Project) {
    
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun load(filePath: String): MbConfig {
        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalArgumentException("File not found: $filePath")
        }
        return gson.fromJson(file.readText(), MbConfig::class.java)
    }

    fun save(filePath: String, config: MbConfig) {
        val file = File(filePath)
        file.writeText(gson.toJson(config))
    }

    fun create(directory: String, modelName: String, scenario: MLScenario): MbConfig {
        return MbConfig(
            scenario = scenario.displayName,
            dataSource = DataSourceConfig("LocalFile", "", ","),
            environment = EnvironmentConfig("LocalCPU", false),
            labelColumn = "",
            featureColumns = emptyList(),
            trainingTime = 30,
            runHistory = emptyList(),
            version = "1.0"
        )
    }

    fun updateWithResult(config: MbConfig, result: TrainingResult): MbConfig {
        val historyEntry = RunHistoryEntry(
            startTime = "now",
            endTime = "now",
            bestAlgorithm = result.bestAlgorithm,
            metrics = result.metrics,
            modelPath = result.modelFilePath
        )
        val mutableHistory = config.runHistory.toMutableList()
        mutableHistory.add(historyEntry)
        return config.copy(runHistory = mutableHistory)
    }

    companion object {
        fun getInstance(project: Project): MbConfigService {
            return project.getService(MbConfigService::class.java)
        }
    }
}
