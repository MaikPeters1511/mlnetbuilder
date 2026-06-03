package com.mlnet.builder.ui.steps

import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBUI
import com.mlnet.builder.model.TrainingConfig
import com.mlnet.builder.services.CodeGenerationService
import com.mlnet.builder.ui.WizardState
import com.mlnet.builder.ui.WizardStepPanel
import java.awt.BorderLayout
import java.awt.datatransfer.StringSelection
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextArea

class ConsumeStep(private val project: Project, private val wizardState: WizardState) : JPanel(BorderLayout()), WizardStepPanel {

    private val codeGenService = CodeGenerationService.getInstance(project)
    private val tabbedPane = JBTabbedPane()
    
    private val consumptionArea = JTextArea().apply { isEditable = false; font = JBUI.Fonts.create("Monospaced", 12) }
    private val trainingArea = JTextArea().apply { isEditable = false; font = JBUI.Fonts.create("Monospaced", 12) }
    
    private var configSnapshot: TrainingConfig? = null

    init {
        tabbedPane.addTab("Consumption Code", JBScrollPane(consumptionArea))
        tabbedPane.addTab("Training Code", JBScrollPane(trainingArea))
        
        val btnPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(8)
            val btn = JButton("Add to Project").apply {
                putClientProperty("JButton.buttonType", "default")
                addActionListener { addToProject() }
            }
            val copyBtn = JButton("Copy Code").apply {
                addActionListener { copyCode() }
            }
            add(btn, BorderLayout.WEST)
            add(copyBtn, BorderLayout.EAST)
        }
        
        add(tabbedPane, BorderLayout.CENTER)
        add(btnPanel, BorderLayout.SOUTH)
    }
    
    private fun addToProject() {
        val config = configSnapshot ?: return
        val result = wizardState.trainingResult ?: return
        codeGenService.writeGeneratedFiles(project.basePath ?: "", config, result)
        // Optionally show notification
    }
    
    private fun copyCode() {
        val idx = tabbedPane.selectedIndex
        val text = if (idx == 0) consumptionArea.text else trainingArea.text
        CopyPasteManager.getInstance().setContents(StringSelection(text))
    }

    override fun getStepTitle(): String = "Consume"
    override fun getStepDescription(): String = "Generate code to use your model."
    override fun isStepValid(): Boolean = true
    
    override fun onEnter() {
        val result = wizardState.trainingResult
        if (result != null) {
            val config = TrainingConfig(
                scenario = wizardState.scenario!!,
                dataFilePath = wizardState.dataFilePath!!,
                labelColumn = wizardState.labelColumn!!,
                featureColumns = wizardState.featureColumns,
                trainingTimeSeconds = wizardState.trainingTimeSeconds,
                useGpu = wizardState.useGpu,
                outputDirectory = project.basePath ?: "",
                modelName = wizardState.modelName
            )
            configSnapshot = config
            consumptionArea.text = codeGenService.generateConsumptionCode(config, result)
            trainingArea.text = codeGenService.generateTrainingCode(config, result)
        } else {
            consumptionArea.text = "// No model trained yet"
            trainingArea.text = "// No model trained yet"
        }
        consumptionArea.caretPosition = 0
        trainingArea.caretPosition = 0
    }
    
    override fun onLeave() {}
}
