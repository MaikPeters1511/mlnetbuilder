package com.mlnet.builder.ui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Factory that creates the ML.NET Builder tool window content.
 * Registered in plugin.xml as an extension for com.intellij.toolWindow.
 */
class MLNetToolWindowFactory : ToolWindowFactory, DumbAware {

    private val logger = Logger.getInstance(MLNetToolWindowFactory::class.java)

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        logger.info("Creating ML.NET Builder tool window")

        val wizardPanel = WizardPanel(project)
        val content = ContentFactory.getInstance().createContent(
            wizardPanel,
            "ML.NET Builder",
            false
        )

        toolWindow.contentManager.addContent(content)
        toolWindow.setToHideOnEmptyContent(false)
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        // Show the tool window in all projects — user might want to create
        // a new ML.NET model in any .NET project
        return true
    }
}
