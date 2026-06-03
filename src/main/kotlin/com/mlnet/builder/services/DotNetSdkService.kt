package com.mlnet.builder.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.io.File
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class DotNetSdkService(private val project: Project) {

    private val logger = Logger.getInstance(DotNetSdkService::class.java)

    fun isDotNetInstalled(): Boolean {
        return getDotNetVersion() != null
    }

    fun getDotNetVersion(): String? {
        return try {
            val cmd = GeneralCommandLine("dotnet", "--version")
            val handler = CapturingProcessHandler(cmd)
            val output = handler.runProcess(5000)
            if (output.exitCode == 0) {
                output.stdout.trim()
            } else {
                null
            }
        } catch (e: Exception) {
            logger.warn("Failed to check dotnet version", e)
            null
        }
    }

    fun isMlNetCliInstalled(): Boolean {
        return try {
            val cmd = GeneralCommandLine("dotnet", "tool", "list", "-g")
            val handler = CapturingProcessHandler(cmd)
            val output = handler.runProcess(5000)
            output.exitCode == 0 && output.stdout.contains("mlnet")
        } catch (e: Exception) {
            logger.warn("Failed to check if mlnet is installed", e)
            false
        }
    }

    fun getMlNetCliVersion(): String? {
        return try {
            val cmd = GeneralCommandLine("mlnet", "--version")
            val handler = CapturingProcessHandler(cmd)
            val output = handler.runProcess(5000)
            if (output.exitCode == 0) {
                output.stdout.trim()
            } else {
                null
            }
        } catch (e: Exception) {
            logger.warn("Failed to get mlnet version", e)
            null
        }
    }

    fun installMlNetCli(): Boolean {
        return try {
            val cmd = GeneralCommandLine("dotnet", "tool", "install", "-g", "mlnet-linux-x64") // Actually should just be mlnet, but maybe need arch specific or just mlnet
            // Let's use general mlnet
            val cmd2 = GeneralCommandLine("dotnet", "tool", "install", "-g", "mlnet")
            val handler = CapturingProcessHandler(cmd2)
            val output = handler.runProcess(30000)
            output.exitCode == 0
        } catch (e: Exception) {
            logger.error("Failed to install mlnet cli", e)
            false
        }
    }

    fun getDotNetPath(): String {
        return "dotnet"
    }

    companion object {
        fun getInstance(project: Project): DotNetSdkService {
            return project.getService(DotNetSdkService::class.java)
        }
    }
}
