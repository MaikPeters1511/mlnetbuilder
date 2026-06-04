package com.mlnet.builder.ui.steps

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.mlnet.builder.ui.WizardState
import com.mlnet.builder.ui.WizardStepPanel
import java.awt.BorderLayout
import java.awt.Font
import java.awt.GridLayout
import javax.swing.*

class EnvironmentStep(private val project: Project, private val wizardState: WizardState) : JPanel(BorderLayout()), WizardStepPanel {

    private val localCpuRadio = JRadioButton("Local (CPU)")
    private val localGpuRadio = JRadioButton("Local (GPU)")
    private val azureRadio = JRadioButton("Azure")

    init {
        border = JBUI.Borders.empty(16)
        val panel = JPanel(GridLayout(3, 1, 0, JBUI.scale(12)))
        
        val buttonGroup = ButtonGroup()
        buttonGroup.add(localCpuRadio)
        buttonGroup.add(localGpuRadio)
        buttonGroup.add(azureRadio)

        // Add descriptions
        val cpuPanel = createOptionPanel(localCpuRadio, "Train on your local machine using CPU.")
        val gpuPanel = createOptionPanel(localGpuRadio, "Train on your local machine using GPU. Requires compatible hardware and drivers.")
        val azurePanel = createOptionPanel(azureRadio, "Train in the cloud using Azure Machine Learning.")
        
        // Default select
        localCpuRadio.isSelected = true

        // Listeners
        val actionListener = java.awt.event.ActionListener {
            when {
                localCpuRadio.isSelected -> wizardState.environment = "Local (CPU)"
                localGpuRadio.isSelected -> wizardState.environment = "Local (GPU)"
                azureRadio.isSelected -> wizardState.environment = "Azure"
            }
            wizardState.useGpu = localGpuRadio.isSelected
        }
        
        localCpuRadio.addActionListener(actionListener)
        localGpuRadio.addActionListener(actionListener)
        azureRadio.addActionListener(actionListener)

        panel.add(cpuPanel)
        panel.add(gpuPanel)
        panel.add(azurePanel)
        
        val wrapper = JPanel(BorderLayout())
        wrapper.add(panel, BorderLayout.NORTH)
        add(wrapper, BorderLayout.CENTER)
    }

    private fun createOptionPanel(radio: JRadioButton, description: String): JPanel {
        val panel = JPanel(BorderLayout())
        radio.font = radio.font.deriveFont(Font.BOLD, JBUI.scale(14).toFloat())
        val descLabel = JBLabel(description).apply {
            font = font.deriveFont(Font.PLAIN, JBUI.scale(12).toFloat())
            foreground = com.intellij.ui.JBColor.GRAY
            border = JBUI.Borders.emptyLeft(24) // Indent to align with radio text
        }
        panel.add(radio, BorderLayout.NORTH)
        panel.add(descLabel, BorderLayout.CENTER)
        return panel
    }

    override fun getStepTitle(): String = "Environment"
    override fun getStepDescription(): String = "Select the compute environment to train your model."
    override fun isStepValid(): Boolean = true
    
    override fun onEnter() {
        when (wizardState.environment) {
            "Local (CPU)" -> localCpuRadio.isSelected = true
            "Local (GPU)" -> localGpuRadio.isSelected = true
            "Azure" -> azureRadio.isSelected = true
            else -> {
                localCpuRadio.isSelected = true
                wizardState.environment = "Local (CPU)"
            }
        }
    }
    
    override fun onLeave() {}
}
