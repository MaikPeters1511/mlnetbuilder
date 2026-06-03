package com.mlnet.builder.ui.components

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel

/**
 * A panel for previewing CSV/TSV data in a table format.
 *
 * Displays data in a [JBTable] with support for highlighting the label
 * (target) column, showing row counts, and auto-sizing columns.
 */
class DataPreviewTable : JPanel(BorderLayout()) {

    private val tableModel = object : DefaultTableModel() {
        override fun isCellEditable(row: Int, column: Int): Boolean = false
    }

    private val table = JBTable(tableModel).apply {
        autoResizeMode = JTable.AUTO_RESIZE_OFF
        rowHeight = JBUI.scale(24)
        setShowGrid(true)
        gridColor = JBColor(Color(0xE8E8E8), Color(0x4B4B4B))
        tableHeader.reorderingAllowed = false
    }

    private val scrollPane = JBScrollPane(table).apply {
        border = JBUI.Borders.empty()
    }

    private val rowCountLabel = JBLabel().apply {
        border = JBUI.Borders.empty(6, 10)
        font = UIUtil.getLabelFont().deriveFont(Font.PLAIN, JBUI.scale(12).toFloat())
        foreground = JBColor(Color(0x888888), Color(0x999999))
    }

    private val emptyLabel = JBLabel("No data loaded", SwingConstants.CENTER).apply {
        foreground = JBColor(Color(0xAAAAAA), Color(0x777777))
        font = UIUtil.getLabelFont().deriveFont(Font.ITALIC, JBUI.scale(14).toFloat())
    }

    private var labelColumnName: String? = null
    private var totalRowCount: Int = 0

    private val labelColumnBackground = JBColor(Color(0xE3F2FD), Color(0x1A3A5C))
    private val labelColumnHeaderBackground = JBColor(Color(0xBBDEFB), Color(0x1E4976))

    init {
        border = JBUI.Borders.empty()
        background = UIUtil.getPanelBackground()
        add(emptyLabel, BorderLayout.CENTER)
    }

    /**
     * Populates the table with the given column names and row data.
     *
     * @param columns  the column header names
     * @param rows     the row data, each inner list corresponding to one row
     * @param labelColumn  optional name of the label (target) column to highlight
     */
    fun setData(columns: List<String>, rows: List<List<String>>, labelColumn: String?) {
        labelColumnName = labelColumn

        removeAll()

        if (columns.isEmpty()) {
            add(emptyLabel, BorderLayout.CENTER)
            revalidate()
            repaint()
            return
        }

        // Update model
        tableModel.setColumnCount(0)
        tableModel.setRowCount(0)

        for (col in columns) {
            tableModel.addColumn(col)
        }

        for (row in rows) {
            val paddedRow = row.toMutableList()
            while (paddedRow.size < columns.size) {
                paddedRow.add("")
            }
            tableModel.addRow(paddedRow.toTypedArray())
        }

        totalRowCount = rows.size

        // Apply cell renderers
        applyCellRenderers(columns)

        // Auto-size columns
        autoSizeColumns()

        // Build bottom bar
        val bottomPanel = JPanel(BorderLayout()).apply {
            background = UIUtil.getPanelBackground()
            border = JBUI.Borders.customLine(JBColor(Color(0xE0E0E0), Color(0x515151)), 1, 0, 0, 0)
        }

        val displayedCount = rows.size
        rowCountLabel.text = if (totalRowCount > displayedCount) {
            "Showing $displayedCount of $totalRowCount rows"
        } else {
            "Showing $displayedCount rows  •  ${columns.size} columns"
        }
        bottomPanel.add(rowCountLabel, BorderLayout.WEST)

        if (labelColumn != null) {
            val labelInfo = JBLabel("Label column: $labelColumn").apply {
                border = JBUI.Borders.empty(6, 10)
                font = UIUtil.getLabelFont().deriveFont(Font.ITALIC, JBUI.scale(12).toFloat())
                foreground = JBColor(Color(0x2979FF), Color(0x448AFF))
            }
            bottomPanel.add(labelInfo, BorderLayout.EAST)
        }

        add(scrollPane, BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)

        revalidate()
        repaint()
    }

    /**
     * Updates the highlighted label column.
     */
    fun setLabelColumn(labelColumn: String?) {
        labelColumnName = labelColumn
        val columns = (0 until tableModel.columnCount).map { tableModel.getColumnName(it) }
        applyCellRenderers(columns)
        table.repaint()
        table.tableHeader.repaint()
    }

    /**
     * Updates the row count label to reflect a larger total (e.g. when only a
     * preview subset is loaded).
     */
    fun setTotalRowCount(total: Int) {
        totalRowCount = total
        val displayedCount = tableModel.rowCount
        rowCountLabel.text = if (total > displayedCount) {
            "Showing $displayedCount of $total rows"
        } else {
            "Showing $displayedCount rows  •  ${tableModel.columnCount} columns"
        }
    }

    private fun applyCellRenderers(columns: List<String>) {
        val labelIndex = if (labelColumnName != null) columns.indexOf(labelColumnName!!) else -1

        for (colIdx in 0 until table.columnCount) {
            val isLabelCol = colIdx == labelIndex
            table.columnModel.getColumn(colIdx).cellRenderer = DataCellRenderer(isLabelCol)
            table.columnModel.getColumn(colIdx).headerRenderer = HeaderCellRenderer(isLabelCol)
        }
    }

    private fun autoSizeColumns() {
        val maxColumnWidth = JBUI.scale(250)
        val minColumnWidth = JBUI.scale(80)
        val padding = JBUI.scale(20)

        for (colIdx in 0 until table.columnCount) {
            var preferredWidth = minColumnWidth

            // Check header width
            val headerRenderer = table.tableHeader.defaultRenderer
            val headerComp = headerRenderer.getTableCellRendererComponent(
                table, table.columnModel.getColumn(colIdx).headerValue, false, false, -1, colIdx
            )
            preferredWidth = preferredWidth.coerceAtLeast(headerComp.preferredSize.width + padding)

            // Check first N rows
            val rowsToCheck = minOf(table.rowCount, 50)
            for (rowIdx in 0 until rowsToCheck) {
                val cellRenderer = table.getCellRenderer(rowIdx, colIdx)
                val comp = table.prepareRenderer(cellRenderer, rowIdx, colIdx)
                preferredWidth = preferredWidth.coerceAtLeast(comp.preferredSize.width + padding)
            }

            table.columnModel.getColumn(colIdx).preferredWidth = preferredWidth.coerceAtMost(maxColumnWidth)
        }
    }

    /**
     * Cell renderer that applies a subtle highlight background to the label column.
     */
    private inner class DataCellRenderer(private val isLabelColumn: Boolean) : DefaultTableCellRenderer() {
        init {
            border = JBUI.Borders.empty(0, 6)
        }

        override fun getTableCellRendererComponent(
            table: JTable,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): Component {
            val comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            if (!isSelected && isLabelColumn) {
                comp.background = labelColumnBackground
            } else if (!isSelected) {
                comp.background = UIUtil.getTableBackground()
            }
            border = JBUI.Borders.empty(0, 6)
            return comp
        }
    }

    /**
     * Header renderer that highlights the label column header.
     */
    private inner class HeaderCellRenderer(private val isLabelColumn: Boolean) : DefaultTableCellRenderer() {
        init {
            horizontalAlignment = SwingConstants.LEFT
            border = JBUI.Borders.empty(4, 6)
        }

        override fun getTableCellRendererComponent(
            table: JTable,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): Component {
            val comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            font = UIUtil.getLabelFont().deriveFont(Font.BOLD, JBUI.scale(12).toFloat())
            if (isLabelColumn) {
                comp.background = labelColumnHeaderBackground
                comp.foreground = JBColor(Color(0x1565C0), Color(0x82B1FF))
                val displayText = "⬤ ${value ?: ""}"
                text = displayText
            } else {
                comp.background = UIUtil.getTableBackground()
                comp.foreground = UIUtil.getLabelForeground()
            }
            border = JBUI.Borders.compound(
                JBUI.Borders.customLine(JBColor(Color(0xE0E0E0), Color(0x515151)), 0, 0, 1, 0),
                JBUI.Borders.empty(4, 6)
            )
            return comp
        }
    }
}
