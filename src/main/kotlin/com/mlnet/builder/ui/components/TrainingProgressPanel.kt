package com.mlnet.builder.ui.components

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JTextArea
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.Timer

/**
 * Panel that visualises ML model training progress.
 *
 * Contains a progress bar (indeterminate until a total duration is known),
 * a status label, an elapsed-time label, and a read-only, monospaced,
 * auto-scrolling log text area.
 */
class TrainingProgressPanel : JPanel(BorderLayout()) {

    private val progressBar = JProgressBar(0, 100).apply {
        isIndeterminate = true
        isStringPainted = true
        string = "Preparing..."
        preferredSize = Dimension(preferredSize.width, JBUI.scale(24))
        border = JBUI.Borders.empty()
    }

    private val statusLabel = JBLabel("Ready to train").apply {
        font = UIUtil.getLabelFont().deriveFont(Font.BOLD, JBUI.scale(13).toFloat())
        border = JBUI.Borders.emptyBottom(4)
    }

    private val elapsedLabel = JBLabel("Elapsed: 0:00").apply {
        font = UIUtil.getLabelFont().deriveFont(Font.PLAIN, JBUI.scale(12).toFloat())
        foreground = JBColor(Color(0x888888), Color(0x999999))
        horizontalAlignment = SwingConstants.RIGHT
    }

    private val logArea = JTextArea().apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        font = Font(Font.MONOSPACED, Font.PLAIN, JBUI.scale(12))
        background = JBColor(Color(0xFAFAFA), Color(0x2B2B2B))
        foreground = JBColor(Color(0x333333), Color(0xCCCCCC))
        border = JBUI.Borders.empty(8)
        rows = 10
    }

    private val logScrollPane = JBScrollPane(logArea).apply {
        border = BorderFactory.createCompoundBorder(
            JBUI.Borders.customLine(JBColor(Color(0xE0E0E0), Color(0x515151)), 1),
            JBUI.Borders.empty()
        )
        preferredSize = Dimension(preferredSize.width, JBUI.scale(200))
    }

    private var elapsedSeconds: Int = 0
    private var totalSeconds: Int = 0
    private var isRunning: Boolean = false

    private val timer = Timer(1000) {
        if (isRunning) {
            elapsedSeconds++
            updateTimerDisplay()
            if (totalSeconds > 0) {
                val progress = ((elapsedSeconds.toDouble() / totalSeconds) * 100).toInt().coerceAtMost(100)
                progressBar.value = progress
                progressBar.string = "$progress%"
            }
        }
    }

    private val successColor = JBColor(Color(0x2E7D32), Color(0x66BB6A))
    private val failureColor = JBColor(Color(0xC62828), Color(0xEF5350))

    init {
        border = JBUI.Borders.empty(12)
        background = UIUtil.getPanelBackground()

        // Top section: status + elapsed time
        val topRow = JPanel(BorderLayout()).apply {
            isOpaque = false
            border = JBUI.Borders.emptyBottom(8)
            add(statusLabel, BorderLayout.WEST)
            add(elapsedLabel, BorderLayout.EAST)
        }

        // Progress bar section
        val progressPanel = JPanel(BorderLayout()).apply {
            isOpaque = false
            border = JBUI.Borders.emptyBottom(12)
            add(progressBar, BorderLayout.CENTER)
        }

        // Header panel combining top row and progress bar
        val headerPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
            add(topRow)
            add(progressPanel)
        }

        // Log section with title
        val logTitle = JBLabel("Training Log").apply {
            font = UIUtil.getLabelFont().deriveFont(Font.BOLD, JBUI.scale(12).toFloat())
            foreground = JBColor(Color(0x666666), Color(0xAAAAAA))
            border = JBUI.Borders.emptyBottom(6)
        }

        val logPanel = JPanel(BorderLayout()).apply {
            isOpaque = false
            add(logTitle, BorderLayout.NORTH)
            add(logScrollPane, BorderLayout.CENTER)
        }

        add(headerPanel, BorderLayout.NORTH)
        add(logPanel, BorderLayout.CENTER)
    }

    /**
     * Starts the progress tracking with an estimated total duration.
     *
     * @param totalSeconds  estimated total training duration in seconds;
     *                      pass 0 for indeterminate mode
     */
    fun startProgress(totalSeconds: Int) {
        this.totalSeconds = totalSeconds
        elapsedSeconds = 0
        isRunning = true

        if (totalSeconds > 0) {
            progressBar.isIndeterminate = false
            progressBar.value = 0
            progressBar.string = "0%"
        } else {
            progressBar.isIndeterminate = true
            progressBar.string = "Training..."
        }

        statusLabel.text = "Training..."
        statusLabel.foreground = UIUtil.getLabelForeground()
        elapsedLabel.text = "Elapsed: 0:00"

        appendLog("[INFO] Training started" + if (totalSeconds > 0) " (estimated: ${formatTime(totalSeconds)})" else "")

        timer.start()
    }

    /**
     * Updates the status label and appends the message to the log.
     */
    fun updateStatus(message: String) {
        SwingUtilities.invokeLater {
            statusLabel.text = message
            appendLog("[STATUS] $message")
        }
    }

    /**
     * Appends a line to the log area and auto-scrolls to the bottom.
     */
    fun appendLog(line: String) {
        SwingUtilities.invokeLater {
            if (logArea.text.isNotEmpty()) {
                logArea.append("\n")
            }
            logArea.append(line)
            // Auto-scroll to the bottom
            logArea.caretPosition = logArea.document.length
        }
    }

    /**
     * Stops the progress, showing a success or failure final state.
     *
     * @param success  true if training completed successfully
     */
    fun stopProgress(success: Boolean) {
        isRunning = false
        timer.stop()

        SwingUtilities.invokeLater {
            progressBar.isIndeterminate = false
            if (success) {
                progressBar.value = 100
                progressBar.string = "Complete"
                statusLabel.text = "✓ Training completed successfully"
                statusLabel.foreground = successColor
                appendLog("[INFO] Training completed successfully in ${formatTime(elapsedSeconds)}")
            } else {
                progressBar.string = "Failed"
                statusLabel.text = "✗ Training failed"
                statusLabel.foreground = failureColor
                appendLog("[ERROR] Training failed after ${formatTime(elapsedSeconds)}")
            }
            elapsedLabel.text = "Total: ${formatTime(elapsedSeconds)}"
        }
    }

    /**
     * Resets all state for a new training run.
     */
    fun reset() {
        isRunning = false
        timer.stop()
        elapsedSeconds = 0
        totalSeconds = 0

        SwingUtilities.invokeLater {
            progressBar.isIndeterminate = true
            progressBar.value = 0
            progressBar.string = "Preparing..."
            statusLabel.text = "Ready to train"
            statusLabel.foreground = UIUtil.getLabelForeground()
            elapsedLabel.text = "Elapsed: 0:00"
            logArea.text = ""
        }
    }

    private fun updateTimerDisplay() {
        SwingUtilities.invokeLater {
            val elapsedFormatted = formatTime(elapsedSeconds)
            if (totalSeconds > 0) {
                val remainingFormatted = formatTime((totalSeconds - elapsedSeconds).coerceAtLeast(0))
                elapsedLabel.text = "Elapsed: $elapsedFormatted / ${formatTime(totalSeconds)}"
                statusLabel.text = "Training... ($remainingFormatted remaining)"
            } else {
                elapsedLabel.text = "Elapsed: $elapsedFormatted"
            }
        }
    }

    private fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "$mins:${secs.toString().padStart(2, '0')}"
    }
}
