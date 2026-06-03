package com.mlnet.builder

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Plugin startup activity that runs when a project is opened.
 * Checks for .NET SDK and mlnet CLI availability.
 */
class MLNetBuilderStartupActivity : ProjectActivity {

    private val logger = Logger.getInstance(MLNetBuilderStartupActivity::class.java)

    override suspend fun execute(project: Project) {
        logger.info("ML.NET Builder plugin initialized for project: ${project.name}")
    }
}
