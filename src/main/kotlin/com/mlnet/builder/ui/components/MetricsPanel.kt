package com.mlnet.builder.ui.components

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.RenderingHints
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.SwingConstants

/**
 * Panel for displaying ML model evaluation metrics as styled cards.
 *
 * Metrics are rendered as individual cards with color-coded values
 * based on quality thresholds: green (>0.9), orange (0.7–0.9), red (<0.7).
 * The primary metric for the selected scenario is displayed with a
 * larger card and a special accent border.
 */
class MetricsPanel : JPanel() {

    private val cardsPanel = JPanel().apply {
        layout = FlowLayout(FlowLayout.LEFT, JBUI.scale(12), JBUI.scale(12))
        isOpaque = false
    }

    private val titleLabel = JBLabel("Model Evaluation Metrics").apply {
        font = UIUtil.getLabelFont().deriveFont(Font.BOLD, JBUI.scale(16).toFloat())
        border = JBUI.Borders.emptyBottom(8)
    }

    private val scenarioLabel = JBLabel().apply {
        font = UIUtil.getLabelFont().deriveFont(Font.ITALIC, JBUI.scale(12).toFloat())
        foreground = JBColor(Color(0x888888), Color(0x999999))
        border = JBUI.Borders.emptyBottom(12)
    }

    init {
        layout = BorderLayout()
        isOpaque = false
        border = JBUI.Borders.empty(12)

        val headerPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
            add(titleLabel)
            add(scenarioLabel)
        }
        add(headerPanel, BorderLayout.NORTH)
        add(cardsPanel, BorderLayout.CENTER)
    }

    /**
     * Displays the given metrics as styled metric cards.
     *
     * @param metrics   map of metric name to its double value (0.0–1.0 range expected)
     * @param scenario  the ML scenario name (e.g. "Classification", "Regression")
     */
    fun setMetrics(metrics: Map<String, Double>, scenario: String) {
        cardsPanel.removeAll()
        scenarioLabel.text = "Scenario: $scenario"

        val primaryMetric = determinePrimaryMetric(scenario)

        // Render primary metric card first (if present)
        val primaryEntry = metrics.entries.find { it.key.equals(primaryMetric, ignoreCase = true) }
        if (primaryEntry != null) {
            cardsPanel.add(createMetricCard(primaryEntry.key, primaryEntry.value, isPrimary = true))
        }

        // Render remaining metric cards
        for ((name, value) in metrics) {
            if (name.equals(primaryMetric, ignoreCase = true)) continue
            cardsPanel.add(createMetricCard(name, value, isPrimary = false))
        }

        cardsPanel.revalidate()
        cardsPanel.repaint()
    }

    private fun determinePrimaryMetric(scenario: String): String {
        return when (scenario.lowercase()) {
            "classification", "binary classification", "multiclass classification",
            "image classification", "text classification" -> "Accuracy"
            "regression", "value prediction" -> "RSquared"
            "recommendation" -> "RSquared"
            "ranking" -> "NDCG"
            "anomaly detection" -> "AUC"
            "forecasting" -> "MAE"
            "object detection" -> "MAP"
            else -> "Accuracy"
        }
    }

    private fun qualityColor(value: Double): JBColor {
        return when {
            value >= 0.9 -> JBColor(Color(0x2E7D32), Color(0x66BB6A))
            value >= 0.7 -> JBColor(Color(0xE65100), Color(0xFFA726))
            else -> JBColor(Color(0xC62828), Color(0xEF5350))
        }
    }

    private fun qualityBorderColor(value: Double): JBColor {
        return when {
            value >= 0.9 -> JBColor(Color(0xA5D6A7), Color(0x2E7D32))
            value >= 0.7 -> JBColor(Color(0xFFCC80), Color(0xE65100))
            else -> JBColor(Color(0xEF9A9A), Color(0xC62828))
        }
    }

    private fun qualityLabel(value: Double): String {
        return when {
            value >= 0.9 -> "Excellent"
            value >= 0.7 -> "Good"
            value >= 0.5 -> "Fair"
            else -> "Poor"
        }
    }

    private fun metricTooltip(metricName: String): String {
        return when (metricName.lowercase()) {
            "accuracy" -> "Proportion of correct predictions among total predictions."
            "auc", "area under curve" -> "Area Under the ROC Curve. Measures the model's ability to distinguish between classes."
            "f1score", "f1 score" -> "Harmonic mean of Precision and Recall. Balances both metrics."
            "precision" -> "Proportion of true positive predictions among all positive predictions."
            "recall", "sensitivity" -> "Proportion of actual positives correctly identified."
            "rsquared", "r-squared", "r²" -> "Coefficient of determination. Indicates how well the model explains variance in the data."
            "mae", "mean absolute error" -> "Average absolute difference between predicted and actual values. Lower is better."
            "rmse", "root mean squared error" -> "Square root of the average squared differences. Lower is better."
            "logloss", "log loss", "log-loss" -> "Logarithmic loss penalizing confident wrong predictions. Lower is better."
            "ndcg" -> "Normalized Discounted Cumulative Gain. Measures ranking quality."
            "map", "mean average precision" -> "Mean Average Precision across detection thresholds."
            else -> "Evaluation metric: $metricName"
        }
    }

    private fun createMetricCard(name: String, value: Double, isPrimary: Boolean): JPanel {
        val cardWidth = if (isPrimary) JBUI.scale(200) else JBUI.scale(160)
        val cardHeight = if (isPrimary) JBUI.scale(120) else JBUI.scale(100)

        val color = qualityColor(value)
        val borderColor = qualityBorderColor(value)
        val quality = qualityLabel(value)

        val card = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                val g2 = g as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2.color = UIUtil.getPanelBackground()
                g2.fillRoundRect(0, 0, width, height, JBUI.scale(8), JBUI.scale(8))
                super.paintComponent(g)
            }
        }.apply {
            layout = BorderLayout()
            isOpaque = false
            preferredSize = Dimension(cardWidth, cardHeight)
            minimumSize = Dimension(cardWidth, cardHeight)
            toolTipText = metricTooltip(name)

            val accentThickness = if (isPrimary) JBUI.scale(3) else JBUI.scale(2)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, accentThickness),
                JBUI.Borders.empty(10, 12)
            )
        }

        // Metric name label
        val nameLabel = JBLabel(name).apply {
            font = UIUtil.getLabelFont().deriveFont(
                if (isPrimary) Font.BOLD else Font.PLAIN,
                JBUI.scale(if (isPrimary) 12 else 11).toFloat()
            )
            foreground = JBColor(Color(0x888888), Color(0x999999))
            horizontalAlignment = SwingConstants.LEFT
        }

        // Metric value label
        val formattedValue = if (value < 10.0) {
            String.format("%.4f", value)
        } else {
            String.format("%.2f", value)
        }
        val valueLabel = JBLabel(formattedValue).apply {
            font = UIUtil.getLabelFont().deriveFont(Font.BOLD, JBUI.scale(if (isPrimary) 24 else 20).toFloat())
            foreground = color
            horizontalAlignment = SwingConstants.LEFT
        }

        // Quality badge
        val qualityBadge = JBLabel(quality).apply {
            font = UIUtil.getLabelFont().deriveFont(Font.ITALIC, JBUI.scale(10).toFloat())
            foreground = color
            horizontalAlignment = SwingConstants.LEFT
        }

        val topSection = JPanel(BorderLayout()).apply {
            isOpaque = false
            add(nameLabel, BorderLayout.NORTH)
            if (isPrimary) {
                val primaryBadge = JBLabel("★ Primary").apply {
                    font = UIUtil.getLabelFont().deriveFont(Font.BOLD, JBUI.scale(9).toFloat())
                    foreground = JBColor(Color(0x2979FF), Color(0x448AFF))
                }
                add(primaryBadge, BorderLayout.SOUTH)
            }
        }

        val bottomSection = JPanel(BorderLayout()).apply {
            isOpaque = false
            add(valueLabel, BorderLayout.CENTER)
            add(qualityBadge, BorderLayout.SOUTH)
        }

        card.add(topSection, BorderLayout.NORTH)
        card.add(bottomSection, BorderLayout.CENTER)

        return card
    }
}
