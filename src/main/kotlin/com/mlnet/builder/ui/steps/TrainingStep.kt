package com.mlnet.builder.ui.steps

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.mlnet.builder.model.TrainingConfig
import com.mlnet.builder.services.MLNetCliService
import com.mlnet.builder.ui.WizardState
import com.mlnet.builder.ui.WizardStepPanel
import com.mlnet.builder.ui.components.TrainingProgressPanel
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.FlowLayout
import javax.swing.*

class TrainingStep(private val project: Project, private val wizardState: WizardState) : JPanel(BorderLayout()), WizardStepPanel {

    private val cliService = MLNetCliService.getInstance(project)
    
    private val configPanel = JPanel()
    private val progressPanelComponent = TrainingProgressPanel()
    
    private val timeSpinner = JSpinner(SpinnerNumberModel(30, 10, 3600, 10))
    private val gpuCheckbox = JCheckBox("Use GPU")
    private val startBtn = JButton("Start Training")
    
    private val cardLayout = CardLayout()
    private val container = JPanel(cardLayout)

    init {
        configPanel.layout = BoxLayout(configPanel, BoxLayout.Y_AXIS)
        configPanel.border = JBUI.Borders.empty(16)
        
        val timePanel = JPanel(FlowLayout(FlowLayout.LEFT, JBUI.scale(8), 0)).apply {
            maximumSize = java.awt.Dimension(Int.MAX_VALUE, JBUI.scale(30))
            add(JBLabel("Time to train (seconds):"))
            add(timeSpinner)
        }
        
        val gpuPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            maximumSize = java.awt.Dimension(Int.MAX_VALUE, JBUI.scale(30))
            add(gpuCheckbox)
        }
        
        val btnPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            border = JBUI.Borders.emptyTop(16)
            maximumSize = java.awt.Dimension(Int.MAX_VALUE, JBUI.scale(50))
            startBtn.putClientProperty("JButton.buttonType", "default")
            startBtn.addActionListener { startTraining() }
            add(startBtn)
        }
        
        configPanel.add(timePanel)
        configPanel.add(gpuPanel)
        configPanel.add(btnPanel)
        configPanel.add(Box.createVerticalGlue())
        
        container.add(configPanel, "CONFIG")
        container.add(progressPanelComponent, "PROGRESS")
        
        add(container, BorderLayout.CENTER)
    }

    private fun startTraining() {
        val config = TrainingConfig(
            scenario = wizardState.scenario!!,
            dataFilePath = wizardState.dataFilePath!!,
            labelColumn = wizardState.labelColumn!!,
            featureColumns = wizardState.featureColumns,
            trainingTimeSeconds = timeSpinner.value as Int,
            useGpu = gpuCheckbox.isSelected,
            outputDirectory = project.basePath ?: "",
            modelName = wizardState.modelName
        )
        
        wizardState.trainingTimeSeconds = config.trainingTimeSeconds
        wizardState.useGpu = config.useGpu
        
        cardLayout.show(container, "PROGRESS")
        progressPanelComponent.reset()
        progressPanelComponent.startProgress(config.trainingTimeSeconds)
        
        cliService.startTraining(config,
            onProgress = { line ->
                javax.swing.SwingUtilities.invokeLater {
                    progressPanelComponent.appendLog(line)
                    progressPanelComponent.updateStatus("Training...")
                }
            },
            onComplete = { result ->
                javax.swing.SwingUtilities.invokeLater {
                    progressPanelComponent.stopProgress(true)
                    progressPanelComponent.appendLog("\nTraining completed successfully! Best algorithm: ${result.bestAlgorithm}")
                    wizardState.trainingResult = result
                }
            },
            onError = { ex ->
                javax.swing.SwingUtilities.invokeLater {
                    progressPanelComponent.stopProgress(false)
                    progressPanelComponent.appendLog("\nTraining failed: ${ex.message}")
                }
            }
        )
    }

    override fun getStepTitle(): String = "Train"
    override fun getStepDescription(): String = "Configure and run AutoML to find the best model."
    override fun isStepValid(): Boolean = wizardState.trainingResult != null
    override fun onEnter() {
        if (wizardState.trainingResult != null) {
            cardLayout.show(container, "PROGRESS")
        } else {
            cardLayout.show(container, "CONFIG")
        }
    }
    override fun onLeave() {}
}
