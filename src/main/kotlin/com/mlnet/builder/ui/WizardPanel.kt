package com.mlnet.builder.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.mlnet.builder.model.MLScenario
import com.mlnet.builder.model.TrainingResult
import com.mlnet.builder.ui.components.StepIndicator
import com.mlnet.builder.ui.steps.*
import java.awt.*
import javax.swing.*

/**
 * Shared state across all wizard steps.
 */
data class WizardState(
    var scenario: MLScenario? = null,
    var dataFilePath: String? = null,
    var delimiter: Char = ',',
    var labelColumn: String? = null,
    var featureColumns: MutableList<String> = mutableListOf(),
    var trainingTimeSeconds: Int = 30,
    var useGpu: Boolean = false,
    var modelName: String = "MLModel",
    var outputDirectory: String? = null,
    var trainingResult: TrainingResult? = null
)

/**
 * Main wizard container panel with step navigation sidebar,
 * content area for step panels, and Next/Back/Cancel buttons.
 */
class WizardPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val logger = Logger.getInstance(WizardPanel::class.java)

    private val wizardState = WizardState()
    private var currentStepIndex = 0

    private val stepNames = listOf(
        "Scenario",
        "Data",
        "Train",
        "Evaluate",
        "Consume"
    )

    private val stepIndicator = StepIndicator(stepNames)

    private val steps: List<JPanel> by lazy {
        listOf(
            ScenarioStep(project, wizardState),
            DataStep(project, wizardState),
            TrainingStep(project, wizardState),
            EvaluateStep(project, wizardState),
            ConsumeStep(project, wizardState)
        )
    }

    private val contentPanel = JPanel(CardLayout())
    private val nextButton = JButton("Next →")
    private val backButton = JButton("← Back")
    private val cancelButton = JButton("Cancel")

    // Header components
    private val titleLabel = JLabel()
    private val descriptionLabel = JLabel()

    init {
        border = JBUI.Borders.empty()
        background = JBColor.PanelBackground

        setupStepIndicator()
        setupContentPanel()
        setupNavigationButtons()
        setupHeader()
        assembleLayout()

        goToStep(0)
    }

    private fun setupStepIndicator() {
        stepIndicator.currentStep = 0
        stepIndicator.border = JBUI.Borders.merge(
            JBUI.Borders.customLine(JBColor.border(), 0, 0, 0, 1),
            JBUI.Borders.empty(12, 8, 12, 8),
            true
        )
    }

    private fun setupContentPanel() {
        steps.forEachIndexed { index, step ->
            contentPanel.add(step, "step_$index")
        }
        contentPanel.border = JBUI.Borders.empty(16)
    }

    private fun setupHeader() {
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, JBUI.scale(18).toFloat())
        titleLabel.foreground = JBColor.foreground()

        descriptionLabel.font = descriptionLabel.font.deriveFont(Font.PLAIN, JBUI.scale(12).toFloat())
        descriptionLabel.foreground = JBColor.GRAY
    }

    private fun setupNavigationButtons() {
        nextButton.addActionListener { onNext() }
        backButton.addActionListener { onBack() }
        cancelButton.addActionListener { onCancel() }

        // Style the next button
        nextButton.putClientProperty("JButton.buttonType", "default")
    }

    private fun assembleLayout() {
        // Header panel
        val headerPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.merge(
                JBUI.Borders.empty(12, 16, 8, 16),
                JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 0),
                true
            )
            isOpaque = false
            add(titleLabel)
            add(Box.createVerticalStrut(JBUI.scale(4)))
            add(descriptionLabel)
        }

        // Navigation button panel
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, JBUI.scale(8), JBUI.scale(4))).apply {
            border = JBUI.Borders.merge(
                JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0),
                JBUI.Borders.empty(8, 16),
                true
            )
            isOpaque = false
            add(cancelButton)
            add(backButton)
            add(nextButton)
        }

        // Right content panel (header + content + buttons)
        val rightPanel = JPanel(BorderLayout()).apply {
            isOpaque = false
            add(headerPanel, BorderLayout.NORTH)
            add(JBScrollPane(contentPanel).apply {
                border = JBUI.Borders.empty()
                viewportBorder = JBUI.Borders.empty()
            }, BorderLayout.CENTER)
            add(buttonPanel, BorderLayout.SOUTH)
        }

        // Main layout
        add(stepIndicator, BorderLayout.WEST)
        add(rightPanel, BorderLayout.CENTER)
    }

    private fun goToStep(index: Int) {
        if (index < 0 || index >= steps.size) return

        // Leave current step
        if (currentStepIndex in steps.indices) {
            val currentStep = steps[currentStepIndex] as? WizardStepPanel
            currentStep?.onLeave()
        }

        currentStepIndex = index
        stepIndicator.currentStep = index

        // Show the step panel
        val cardLayout = contentPanel.layout as CardLayout
        cardLayout.show(contentPanel, "step_$index")

        // Enter new step
        val newStep = steps[index] as? WizardStepPanel
        newStep?.let {
            titleLabel.text = it.getStepTitle()
            descriptionLabel.text = it.getStepDescription()
            it.onEnter()
        }

        // Update button states
        backButton.isEnabled = index > 0
        nextButton.text = if (index == steps.size - 1) "Finish ✓" else "Next →"

        // Mark previous steps as completed
        stepIndicator.completedSteps = (0 until index).toSet()

        logger.info("Wizard navigated to step $index: ${stepNames[index]}")
    }

    private fun onNext() {
        val currentStep = steps[currentStepIndex] as? WizardStepPanel
        if (currentStep != null && !currentStep.isStepValid()) {
            // Step validation failed — the step should show its own error message
            return
        }

        if (currentStepIndex < steps.size - 1) {
            goToStep(currentStepIndex + 1)
        } else {
            onFinish()
        }
    }

    private fun onBack() {
        if (currentStepIndex > 0) {
            goToStep(currentStepIndex - 1)
        }
    }

    private fun onCancel() {
        // Reset wizard state
        goToStep(0)
    }

    private fun onFinish() {
        logger.info("Wizard completed. Model: ${wizardState.modelName}")
        stepIndicator.completedSteps = (0 until steps.size).toSet()
        
        // Ensure files are generated and added to the project
        val result = wizardState.trainingResult
        if (result != null && wizardState.scenario != null && wizardState.dataFilePath != null && wizardState.labelColumn != null) {
            val config = com.mlnet.builder.model.TrainingConfig(
                scenario = wizardState.scenario!!,
                dataFilePath = wizardState.dataFilePath!!,
                labelColumn = wizardState.labelColumn!!,
                featureColumns = wizardState.featureColumns,
                trainingTimeSeconds = wizardState.trainingTimeSeconds,
                useGpu = wizardState.useGpu,
                outputDirectory = project.basePath ?: "",
                modelName = wizardState.modelName
            )
            com.mlnet.builder.services.CodeGenerationService.getInstance(project)
                .writeGeneratedFiles(project.basePath ?: "", config, result)
        }
        
        // Hide the tool window
        com.intellij.openapi.wm.ToolWindowManager.getInstance(project).getToolWindow("ML.NET Builder")?.hide()
        
        // Reset wizard for the next run
        goToStep(0)
    }
}

/**
 * Interface for wizard step panels.
 */
interface WizardStepPanel {
    fun getStepTitle(): String
    fun getStepDescription(): String
    fun isStepValid(): Boolean
    fun onEnter()
    fun onLeave()
}
