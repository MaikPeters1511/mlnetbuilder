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
    
    // Tabular Data UI
    private val fileField = TextFieldWithBrowseButton()
    private val labelColumnCombo = JComboBox<String>()
    
    // Image Data UI
    private val folderField = TextFieldWithBrowseButton()
    
    private val dataPreviewTable = DataPreviewTable()
    
    private val topCards = JPanel(java.awt.CardLayout())
    private val bottomCards = JPanel(java.awt.CardLayout())
    
    init {
        // --- Tabular Mode Setup ---
        val tabularTopPanel = JPanel(BorderLayout(JBUI.scale(8), 0)).apply {
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
        
        val tabularBottomPanel = JPanel(BorderLayout(JBUI.scale(8), 0)).apply {
            border = JBUI.Borders.empty(8)
            add(JBLabel("Label column (to predict):"), BorderLayout.WEST)
            add(labelColumnCombo, BorderLayout.CENTER)
            labelColumnCombo.addActionListener {
                wizardState.labelColumn = labelColumnCombo.selectedItem as? String
                dataPreviewTable.setLabelColumn(wizardState.labelColumn)
            }
        }

        // --- Image Mode Setup ---
        val imageTopPanel = JPanel(BorderLayout(JBUI.scale(8), 0)).apply {
            border = JBUI.Borders.empty(8)
            add(JBLabel("Image folder:"), BorderLayout.WEST)
            val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                .withTitle("Select Image Folder")
                .withDescription("Choose a folder containing subfolders for each category")
            folderField.addBrowseFolderListener(project, descriptor)
            folderField.textField.document.addDocumentListener(object : javax.swing.event.DocumentListener {
                override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = loadImageData()
                override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = loadImageData()
                override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = loadImageData()
            })
            add(folderField, BorderLayout.CENTER)
        }
        
        val imageBottomPanel = JPanel(BorderLayout(JBUI.scale(8), 0)).apply {
            border = JBUI.Borders.empty(8)
            add(JBLabel("Labels are determined from subfolder names."), BorderLayout.WEST)
        }
        
        topCards.add(tabularTopPanel, "TABULAR")
        topCards.add(imageTopPanel, "IMAGE")
        
        bottomCards.add(tabularBottomPanel, "TABULAR")
        bottomCards.add(imageBottomPanel, "IMAGE")
        
        add(topCards, BorderLayout.NORTH)
        add(dataPreviewTable, BorderLayout.CENTER)
        add(bottomCards, BorderLayout.SOUTH)
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

    private fun loadImageData() {
        val path = folderField.text
        if (path.isNotBlank()) {
            wizardState.dataFilePath = path
            // For image classification, labels are the folders
            wizardState.labelColumn = "Label"
            wizardState.featureColumns = mutableListOf("ImageSource")
            
            // Show a simple preview indicating folder is selected
            val folder = java.io.File(path)
            if (folder.exists() && folder.isDirectory) {
                val subdirs = folder.listFiles { it -> it.isDirectory } ?: emptyArray()
                val labels = subdirs.map { it.name }
                val rows = subdirs.take(10).map { dir ->
                    val imageCount = dir.listFiles { it -> it.isFile && (it.name.endsWith(".jpg", true) || it.name.endsWith(".png", true)) }?.size ?: 0
                    listOf(dir.name, "$imageCount images")
                }
                dataPreviewTable.setData(listOf("Label (Folder Name)", "Contents"), rows, null)
                dataPreviewTable.setTotalRowCount(labels.size)
            }
        }
    }

    override fun getStepTitle(): String = "Data"
    override fun getStepDescription(): String = "Select the dataset to use for training."
    override fun isStepValid(): Boolean = !wizardState.dataFilePath.isNullOrBlank() && !wizardState.labelColumn.isNullOrBlank()
    
    override fun onEnter() {
        val isImageClass = wizardState.scenario == com.mlnet.builder.model.MLScenario.IMAGE_CLASSIFICATION
        val topLayout = topCards.layout as java.awt.CardLayout
        val bottomLayout = bottomCards.layout as java.awt.CardLayout
        
        if (isImageClass) {
            topLayout.show(topCards, "IMAGE")
            bottomLayout.show(bottomCards, "IMAGE")
            if (folderField.text != wizardState.dataFilePath) {
                folderField.text = wizardState.dataFilePath ?: ""
            }
            wizardState.labelColumn = "Label" // Default for image classification
        } else {
            topLayout.show(topCards, "TABULAR")
            bottomLayout.show(bottomCards, "TABULAR")
            if (fileField.text != wizardState.dataFilePath) {
                fileField.text = wizardState.dataFilePath ?: ""
            }
        }
    }
    
    override fun onLeave() {}
}
