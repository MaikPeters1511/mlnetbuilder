package com.mlnet.builder.ui.steps

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.mlnet.builder.services.DataService
import com.mlnet.builder.ui.WizardState
import com.mlnet.builder.ui.WizardStepPanel
import com.mlnet.builder.ui.components.DataPreviewTable
import java.awt.BorderLayout
import javax.swing.*

class DataStep(private val project: Project, private val wizardState: WizardState) : JPanel(BorderLayout()), WizardStepPanel {

    private val dataService = DataService.getInstance(project)
    private val fileField = TextFieldWithBrowseButton()
    private val dataPreviewTable = DataPreviewTable()
    private val labelColumnCombo = JComboBox<String>()
    
    init {
        val topPanel = JPanel(BorderLayout(JBUI.scale(8), 0)).apply {
            border = JBUI.Borders.empty(8)
            add(JBLabel("Data file:"), BorderLayout.WEST)
            
            val descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
                .withTitle("Select Data File")
                .withDescription("Choose a CSV or TSV file for training")
                .withFileFilter { 
                    it.extension == "csv" || it.extension == "tsv" || it.extension == "txt" 
                }
            fileField.addBrowseFolderListener(project, descriptor)
            fileField.textField.document.addDocumentListener(object : javax.swing.event.DocumentListener {
                override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = loadData()
                override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = loadData()
                override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = loadData()
            })
            add(fileField, BorderLayout.CENTER)
        }
        
        val bottomPanel = JPanel(BorderLayout(JBUI.scale(8), 0)).apply {
            border = JBUI.Borders.empty(8)
            add(JBLabel("Label column (to predict):"), BorderLayout.WEST)
            add(labelColumnCombo, BorderLayout.CENTER)
            
            labelColumnCombo.addActionListener {
                wizardState.labelColumn = labelColumnCombo.selectedItem as? String
                dataPreviewTable.setLabelColumn(wizardState.labelColumn)
            }
        }
        
        add(topPanel, BorderLayout.NORTH)
        add(dataPreviewTable, BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)
    }

    private fun loadData() {
        val path = fileField.text
        if (path.isNotBlank()) {
            wizardState.dataFilePath = path
            val delimiter = dataService.inferDelimiter(path)
            wizardState.delimiter = delimiter
            val preview = dataService.parseFile(path, delimiter)
            
            labelColumnCombo.removeAllItems()
            preview.columns.forEach { col -> 
                labelColumnCombo.addItem(col.name)
            }
            
            if (preview.columns.isNotEmpty()) {
                val defaultLabel = preview.columns.last().name
                labelColumnCombo.selectedItem = defaultLabel
                wizardState.labelColumn = defaultLabel
            }
            
            dataPreviewTable.setData(preview.columns.map { it.name }, preview.rows, wizardState.labelColumn)
            dataPreviewTable.setTotalRowCount(preview.totalRowCount)
            
            wizardState.featureColumns = preview.columns.map { it.name }.toMutableList()
        }
    }

    override fun getStepTitle(): String = "Data"
    override fun getStepDescription(): String = "Select the dataset to use for training."
    override fun isStepValid(): Boolean = !wizardState.dataFilePath.isNullOrBlank() && !wizardState.labelColumn.isNullOrBlank()
    override fun onEnter() {
        if (fileField.text != wizardState.dataFilePath) {
            fileField.text = wizardState.dataFilePath ?: ""
        }
    }
    override fun onLeave() {}
}
