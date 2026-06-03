package com.mlnet.builder.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.mlnet.builder.model.TrainingConfig
import com.mlnet.builder.model.TrainingResult

@Service(Service.Level.PROJECT)
class MLNetCliService(private val project: Project) {

    private val logger = Logger.getInstance(MLNetCliService::class.java)
    private var currentProcess: OSProcessHandler? = null

    fun startTraining(
        config: TrainingConfig,
        onProgress: (String) -> Unit,
        onComplete: (TrainingResult) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (isTraining()) {
            onError(IllegalStateException("Training is already in progress."))
            return
        }

        try {
            val customPath = com.mlnet.builder.settings.MLNetSettings.getInstance().mlnetCliPath
            val executable = if (customPath.isNotBlank()) customPath else "mlnet"
            val commandLine = GeneralCommandLine(executable, config.scenario.cliCommand)
            
            // Ensure ~/.dotnet/tools is in PATH since IDEs often miss it on macOS/Linux
            val home = System.getProperty("user.home")
            val dotnetTools = "$home/.dotnet/tools"
            val currentPath = commandLine.parentEnvironment["PATH"] ?: System.getenv("PATH") ?: ""
            if (!currentPath.contains(".dotnet/tools")) {
                commandLine.environment["PATH"] = "$dotnetTools${java.io.File.pathSeparator}$currentPath"
            }
            commandLine.addParameter("--dataset")
            commandLine.addParameter(config.dataFilePath)
            commandLine.addParameter("--label-col")
            commandLine.addParameter(config.labelColumn)
            commandLine.addParameter("--train-time")
            commandLine.addParameter(config.trainingTimeSeconds.toString())
            commandLine.addParameter("--output")
            commandLine.addParameter(config.outputDirectory)
            commandLine.addParameter("--name")
            commandLine.addParameter(config.modelName)

            val handler = OSProcessHandler(commandLine)
            currentProcess = handler
            val logs = mutableListOf<String>()

            handler.addProcessListener(object : ProcessAdapter() {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    val text = event.text
                    logs.add(text)
                    onProgress(text)
                }

                override fun processTerminated(event: ProcessEvent) {
                    currentProcess = null
                    if (event.exitCode == 0) {
                        val result = parseTrainingResult(logs, config)
                        onComplete(result)
                    } else {
                        onError(RuntimeException("Training failed with exit code ${event.exitCode}"))
                    }
                }
            })

            handler.startNotify()
        } catch (e: Exception) {
            logger.error("Failed to start training", e)
            currentProcess = null
            onError(e)
        }
    }

    fun cancelTraining() {
        currentProcess?.destroyProcess()
        currentProcess = null
    }

    fun isTraining(): Boolean {
        return currentProcess != null && !currentProcess!!.isProcessTerminated
    }

    private fun parseTrainingResult(logs: List<String>, config: TrainingConfig): TrainingResult {
        // Simple dummy parser for now, in reality parse output
        return TrainingResult(
            bestAlgorithm = "LightGbm",
            metrics = mapOf("Accuracy" to 0.95),
            modelFilePath = "${config.outputDirectory}/${config.modelName}.zip",
            trainingDurationSeconds = config.trainingTimeSeconds.toLong(),
            exploredModels = emptyList(),
            logs = logs
        )
    }

    companion object {
        fun getInstance(project: Project): MLNetCliService {
            return project.getService(MLNetCliService::class.java)
        }
    }
}
