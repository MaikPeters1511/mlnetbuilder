package com.mlnet.builder.ui.steps

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.mlnet.builder.model.MLScenario
import com.mlnet.builder.ui.WizardState
import com.mlnet.builder.ui.WizardStepPanel
import java.awt.BorderLayout
import java.awt.Font
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.border.Border

class ScenarioStep(private val project: Project, private val wizardState: WizardState) : JPanel(BorderLayout()), WizardStepPanel {

    private val gridPanel = JPanel(GridLayout(3, 2, JBUI.scale(16), JBUI.scale(16)))
    private val cards = mutableListOf<ScenarioCard>()

    init {
        border = JBUI.Borders.empty(8)
        
        for (scenario in MLScenario.values()) {
            val card = ScenarioCard(scenario) { selectedScenario ->
                wizardState.scenario = selectedScenario
                updateSelection()
            }
            cards.add(card)
            gridPanel.add(card)
        }

        val scrollPane = JBScrollPane(gridPanel).apply {
            border = JBUI.Borders.empty()
            viewportBorder = JBUI.Borders.empty()
        }
        
        add(scrollPane, BorderLayout.CENTER)
    }

    private fun updateSelection() {
        for (card in cards) {
            card.setSelected(card.scenario == wizardState.scenario)
        }
    }

    override fun getStepTitle(): String = "Scenario"
    override fun getStepDescription(): String = "Choose the machine learning scenario that best describes your task."
    override fun isStepValid(): Boolean = wizardState.scenario != null
    override fun onEnter() { updateSelection() }
    override fun onLeave() {}

    private inner class ScenarioCard(val scenario: MLScenario, val onClick: (MLScenario) -> Unit) : JPanel() {
        private val defaultBorder: Border = BorderFactory.createLineBorder(JBColor.border(), 1, true)
        private val selectedBorder: Border = BorderFactory.createLineBorder(JBColor.BLUE, 2, true)

        init {
            val isSupported = scenario.isSupportedOnCurrentPlatform()
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = defaultBorder
            background = JBColor.PanelBackground
            
            val iconLabel = JBLabel(IconLoader.getIcon(scenario.iconPath, ScenarioCard::class.java))
            iconLabel.alignmentX = CENTER_ALIGNMENT
            if (!isSupported) {
                iconLabel.isEnabled = false // Grays out the icon if supported by look and feel
            }
            
            val titleLabel = JBLabel(scenario.displayName).apply {
                font = font.deriveFont(Font.BOLD, JBUI.scale(14).toFloat())
                alignmentX = CENTER_ALIGNMENT
                if (!isSupported) {
                    foreground = JBColor.GRAY
                }
            }
            
            val descLabel = JBLabel("<html><div style='text-align: center;'>${scenario.description}</div></html>").apply {
                font = font.deriveFont(Font.PLAIN, JBUI.scale(12).toFloat())
                foreground = JBColor.GRAY
                alignmentX = CENTER_ALIGNMENT
            }

            add(JBUI.Borders.emptyTop(16).let { JPanel().apply { isOpaque = false; border = it } })
            add(iconLabel)
            add(JBUI.Borders.emptyTop(8).let { JPanel().apply { isOpaque = false; border = it } })
            add(titleLabel)
            add(JBUI.Borders.emptyTop(4).let { JPanel().apply { isOpaque = false; border = it } })
            add(descLabel)
            
            if (isSupported) {
                add(JBUI.Borders.emptyTop(16).let { JPanel().apply { isOpaque = false; border = it } })
                addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        onClick(scenario)
                    }
                })
                cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
            } else {
                val warningLabel = JBLabel("Not supported on this platform").apply {
                    font = font.deriveFont(Font.BOLD, JBUI.scale(10).toFloat())
                    foreground = com.intellij.ui.JBColor.RED
                    alignmentX = CENTER_ALIGNMENT
                }
                add(JBUI.Borders.emptyTop(8).let { JPanel().apply { isOpaque = false; border = it } })
                add(warningLabel)
                add(JBUI.Borders.emptyTop(8).let { JPanel().apply { isOpaque = false; border = it } })
                
                toolTipText = "This scenario is not supported by the underlying ML.NET CLI on your operating system architecture."
            }
        }

        fun setSelected(selected: Boolean) {
            border = if (selected) selectedBorder else defaultBorder
            background = if (selected) JBColor(0xEDF5FC, 0x2B3D50) else JBColor.PanelBackground
        }
    }
}
