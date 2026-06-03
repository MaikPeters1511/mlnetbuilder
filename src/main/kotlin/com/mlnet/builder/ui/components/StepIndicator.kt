package com.mlnet.builder.ui.components

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JPanel
import javax.swing.border.MatteBorder

/**
 * A vertical sidebar panel that visually displays wizard progress.
 *
 * Each step is rendered as a circle connected by vertical lines.
 * Completed steps show a green circle with a checkmark, the current step
 * shows a blue filled circle with its number, and upcoming steps show
 * a gray outlined circle with their number.
 */
class StepIndicator(private val stepNames: List<String>) : JPanel() {

    var currentStep: Int = 0
        set(value) {
            field = value.coerceIn(0, stepNames.size - 1)
            repaint()
        }

    var completedSteps: Set<Int> = emptySet()
        set(value) {
            field = value
            repaint()
        }

    private val circleDiameter: Int
        get() = JBUI.scale(32)

    private val circleRadius: Int
        get() = circleDiameter / 2

    private val verticalSpacing: Int
        get() = JBUI.scale(56)

    private val topPadding: Int
        get() = JBUI.scale(24)

    private val leftPadding: Int
        get() = JBUI.scale(24)

    private val textLeftMargin: Int
        get() = JBUI.scale(16)

    private val lineStrokeWidth: Float
        get() = JBUI.scale(2).toFloat()

    private val completedColor: JBColor = JBColor(Color(0x5CB85C), Color(0x4CAF50))
    private val activeColor: JBColor = JBColor(Color(0x2979FF), Color(0x448AFF))
    private val upcomingColor: JBColor = JBColor(Color(0xBDBDBD), Color(0x757575))
    private val upcomingFillColor: JBColor = JBColor(Color(0xF5F5F5), Color(0x3C3F41))
    private val borderLineColor: JBColor = JBColor(Color(0xE0E0E0), Color(0x515151))

    init {
        isOpaque = true
        background = UIUtil.getPanelBackground()
        preferredSize = JBUI.size(140, 400)
        border = MatteBorder(0, 0, 0, JBUI.scale(1), borderLineColor)
    }

    override fun getPreferredSize(): Dimension {
        val height = topPadding * 2 + (stepNames.size - 1) * verticalSpacing + circleDiameter
        val width = JBUI.scale(140)
        return Dimension(width, height.coerceAtLeast(JBUI.scale(400)))
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)

            for (i in stepNames.indices) {
                val centerX = leftPadding + circleRadius
                val centerY = topPadding + i * verticalSpacing + circleRadius

                // Draw connecting line to the next step
                if (i < stepNames.size - 1) {
                    val nextCenterY = topPadding + (i + 1) * verticalSpacing + circleRadius
                    val lineColor = when {
                        i in completedSteps -> completedColor
                        else -> upcomingColor
                    }
                    g2.color = lineColor
                    g2.stroke = BasicStroke(lineStrokeWidth)
                    g2.drawLine(centerX, centerY + circleRadius + JBUI.scale(2), centerX, nextCenterY - circleRadius - JBUI.scale(2))
                }

                val isCompleted = i in completedSteps
                val isCurrent = i == currentStep

                when {
                    isCompleted -> drawCompletedStep(g2, centerX, centerY)
                    isCurrent -> drawCurrentStep(g2, centerX, centerY, i)
                    else -> drawUpcomingStep(g2, centerX, centerY, i)
                }

                // Draw step name text
                val textX = centerX + circleRadius + textLeftMargin
                val textY = centerY + JBUI.scale(5)
                val fontSize = JBUI.scale(13).toFloat()
                g2.font = if (isCurrent) {
                    UIUtil.getLabelFont().deriveFont(Font.BOLD, fontSize)
                } else {
                    UIUtil.getLabelFont().deriveFont(Font.PLAIN, fontSize)
                }
                g2.color = if (isCurrent || isCompleted) {
                    UIUtil.getLabelForeground()
                } else {
                    JBColor(Color(0x999999), Color(0x888888))
                }
                g2.drawString(stepNames[i], textX, textY)
            }
        } finally {
            g2.dispose()
        }
    }

    private fun drawCompletedStep(g2: Graphics2D, cx: Int, cy: Int) {
        // Filled green circle
        g2.color = completedColor
        g2.fillOval(cx - circleRadius, cy - circleRadius, circleDiameter, circleDiameter)

        // White checkmark
        g2.color = JBColor.WHITE
        g2.stroke = BasicStroke(JBUI.scale(2).toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        val checkSize = JBUI.scale(8)
        val startX = cx - checkSize / 2
        val startY = cy
        val midX = cx - JBUI.scale(1)
        val midY = cy + checkSize / 2 - JBUI.scale(1)
        val endX = cx + checkSize / 2 + JBUI.scale(1)
        val endY = cy - checkSize / 2 + JBUI.scale(1)
        g2.drawLine(startX, startY, midX, midY)
        g2.drawLine(midX, midY, endX, endY)
    }

    private fun drawCurrentStep(g2: Graphics2D, cx: Int, cy: Int, stepIndex: Int) {
        // Filled blue circle
        g2.color = activeColor
        g2.fillOval(cx - circleRadius, cy - circleRadius, circleDiameter, circleDiameter)

        // White step number
        g2.color = JBColor.WHITE
        val fontSize = JBUI.scale(14).toFloat()
        g2.font = UIUtil.getLabelFont().deriveFont(Font.BOLD, fontSize)
        val text = (stepIndex + 1).toString()
        val fm = g2.fontMetrics
        val textWidth = fm.stringWidth(text)
        val textHeight = fm.ascent
        g2.drawString(text, cx - textWidth / 2, cy + textHeight / 2 - JBUI.scale(1))
    }

    private fun drawUpcomingStep(g2: Graphics2D, cx: Int, cy: Int, stepIndex: Int) {
        // Light fill
        g2.color = upcomingFillColor
        g2.fillOval(cx - circleRadius, cy - circleRadius, circleDiameter, circleDiameter)

        // Gray outline
        g2.color = upcomingColor
        g2.stroke = BasicStroke(JBUI.scale(2).toFloat())
        g2.drawOval(cx - circleRadius, cy - circleRadius, circleDiameter, circleDiameter)

        // Gray step number
        g2.color = upcomingColor
        val fontSize = JBUI.scale(13).toFloat()
        g2.font = UIUtil.getLabelFont().deriveFont(Font.PLAIN, fontSize)
        val text = (stepIndex + 1).toString()
        val fm = g2.fontMetrics
        val textWidth = fm.stringWidth(text)
        val textHeight = fm.ascent
        g2.drawString(text, cx - textWidth / 2, cy + textHeight / 2 - JBUI.scale(1))
    }
}
