package com.mlnet.builder.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.mlnet.builder.services.MbConfigService
import com.mlnet.builder.model.MLScenario
import java.io.File

class AddMLModelAction : AnAction() {

    private val logger = Logger.getInstance(AddMLModelAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val directory = if (virtualFile.isDirectory) {
            virtualFile.path
        } else {
            virtualFile.parent?.path ?: return
        }

        val modelName = Messages.showInputDialog(
            project,
            "Enter a name for your ML.NET model:",
            "Add Machine Learning Model",
            null,
            "MLModel",
            null
        ) ?: return

        if (modelName.isBlank()) {
            Messages.showErrorDialog(project, "Model name cannot be empty.", "Invalid Name")
            return
        }

        try {
            val mbConfigService = MbConfigService.getInstance(project)
            val config = mbConfigService.create(directory, modelName, MLScenario.CLASSIFICATION)
            val filePath = "$directory/$modelName.mbconfig"
            mbConfigService.save(filePath, config)

            // Refresh VFS so the file appears in the project tree
            LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath)

            // Open the ML.NET Builder tool window
            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("ML.NET Builder")
            toolWindow?.show()

            logger.info("Created new ML.NET model: $filePath")
        } catch (ex: Exception) {
            logger.error("Failed to create ML.NET model", ex)
            Messages.showErrorDialog(
                project,
                "Failed to create ML.NET model: ${ex.message}",
                "Error"
            )
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = project != null && file != null
    }
}
