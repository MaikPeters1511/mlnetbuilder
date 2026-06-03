package com.mlnet.builder.ui.steps

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.mlnet.builder.ui.WizardState
import com.mlnet.builder.ui.WizardStepPanel
import com.mlnet.builder.ui.components.MetricsPanel
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.BoxLayout
import javax.swing.JPanel

class EvaluateStep(private val project: Project, private val wizardState: WizardState) : JPanel(BorderLayout()), WizardStepPanel {

    private val contentPanel = JPanel()
    private val metricsPanel = MetricsPanel()
    private val summaryLabel = JBLabel()

    init {
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)
        contentPanel.border = JBUI.Borders.empty(16)
        
        summaryLabel.font = summaryLabel.font.deriveFont(Font.BOLD, JBUI.scale(16).toFloat())
        
        contentPanel.add(summaryLabel)
        contentPanel.add(JBUI.Borders.emptyTop(16).let { JPanel().apply { isOpaque = false; border = it } })
        contentPanel.add(metricsPanel)
        
        add(contentPanel, BorderLayout.NORTH)
    }

    override fun getStepTitle(): String = "Evaluate"
    override fun getStepDescription(): String = "Review the metrics of the best model found."
    override fun isStepValid(): Boolean = true
    
    override fun onEnter() {
        val result = wizardState.trainingResult
        if (result != null) {
            summaryLabel.text = "Best Algorithm: ${result.bestAlgorithm}"
            metricsPanel.setMetrics(result.metrics, wizardState.scenario?.cliCommand ?: "classification")
        } else {
            summaryLabel.text = "No training result available yet. Please complete the Training step."
            metricsPanel.setMetrics(emptyMap(), "classification")
        }
    }
    
    override fun onLeave() {}
}
