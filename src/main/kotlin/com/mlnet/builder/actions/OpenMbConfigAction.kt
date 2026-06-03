package com.mlnet.builder.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.wm.ToolWindowManager

class OpenMbConfigAction : AnAction() {

    private val logger = Logger.getInstance(OpenMbConfigAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        if (file.extension != "mbconfig") return

        logger.info("Opening .mbconfig in ML.NET Builder: ${file.path}")

        // Open the ML.NET Builder tool window
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("ML.NET Builder")
        toolWindow?.show()
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = file != null && file.extension == "mbconfig"
    }
}
