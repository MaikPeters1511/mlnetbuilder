package com.mlnet.builder.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.mlnet.builder.model.TrainingConfig
import com.mlnet.builder.model.TrainingResult
import java.io.File

@Service(Service.Level.PROJECT)
class CodeGenerationService(private val project: Project) {

    fun generateConsumptionCode(config: TrainingConfig, result: TrainingResult): String {
        val sb = StringBuilder()
        sb.appendLine("using Microsoft.ML;")
        sb.appendLine("using Microsoft.ML.Data;")
        sb.appendLine("using System;")
        sb.appendLine()
        sb.appendLine("namespace ${config.modelName}ConsoleApp")
        sb.appendLine("{")
        sb.appendLine("    public class ${config.modelName}Consumption")
        sb.appendLine("    {")
        sb.appendLine("        // Consumption code here")
        sb.appendLine("    }")
        sb.appendLine("}")
        return sb.toString()
    }

    fun generateTrainingCode(config: TrainingConfig, result: TrainingResult): String {
        return "// Training code here\n"
    }

    fun generateSampleCode(config: TrainingConfig): String {
        return "// Sample code here\n"
    }

    fun writeGeneratedFiles(projectPath: String, config: TrainingConfig, result: TrainingResult) {
        // The ML.NET CLI natively generates the model.zip, mbconfig, and the C# consumption/training code.
        // We just need to instruct Rider to refresh its virtual file system asynchronously.
        // This is safe to call from the UI thread without holding write locks.
        val outputDir = File(config.outputDirectory ?: projectPath)
        com.intellij.openapi.vfs.LocalFileSystem.getInstance().refreshIoFiles(listOf(outputDir), true, true, null)
    }

    fun addNuGetReference(csprojPath: String, packageName: String, version: String) {
        // Dummy implementation to update csproj XML
    }

    companion object {
        fun getInstance(project: Project): CodeGenerationService {
            return project.getService(CodeGenerationService::class.java)
        }
    }
}
