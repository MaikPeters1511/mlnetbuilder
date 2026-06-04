package com.mlnet.builder.model

/**
 * Represents the supported ML.NET training scenarios.
 *
 * Each scenario maps to an ML.NET CLI command and carries UI metadata
 * (display name, description, icon) along with domain-specific examples
 * and the evaluation metrics that apply to trained models of that type.
 */
enum class MLScenario(
    val displayName: String,
    val description: String,
    val cliCommand: String,
    val iconPath: String,
    val exampleUseCases: List<String>,
    val relevantMetrics: List<String>,
) {
    CLASSIFICATION(
        displayName = "Data Classification",
        description = "Categorize text or data into groups",
        cliCommand = "classification",
        iconPath = "/icons/scenario-classification.svg",
        exampleUseCases = listOf(
            "Sentiment analysis",
            "Spam detection",
            "Customer support ticket routing",
            "Document categorisation",
        ),
        relevantMetrics = listOf(
            "Accuracy",
            "AUC",
            "F1Score",
            "LogLoss",
        ),
    ),

    REGRESSION(
        displayName = "Value Prediction",
        description = "Predict a numeric value",
        cliCommand = "regression",
        iconPath = "/icons/scenario-regression.svg",
        exampleUseCases = listOf(
            "Price prediction",
            "Demand forecasting",
            "Sales estimation",
            "Risk scoring",
        ),
        relevantMetrics = listOf(
            "RMSE",
            "MAE",
            "RSquared",
            "LossFunction",
        ),
    ),

    RECOMMENDATION(
        displayName = "Recommendation",
        description = "Suggest items based on user history",
        cliCommand = "recommendation",
        iconPath = "/icons/scenario-recommendation.svg",
        exampleUseCases = listOf(
            "Product recommendations",
            "Movie suggestions",
            "Content personalisation",
            "Related item discovery",
        ),
        relevantMetrics = listOf(
            "RMSE",
            "MAE",
            "RSquared",
        ),
    ),

    IMAGE_CLASSIFICATION(
        displayName = "Image Classification",
        description = "Categorize images using deep learning",
        cliCommand = "image-classification",
        iconPath = "/icons/scenario-image.svg",
        exampleUseCases = listOf(
            "Product defect detection",
            "Medical image diagnosis",
            "Object recognition",
            "Scene classification",
        ),
        relevantMetrics = listOf(
            "Accuracy",
            "PerClassAccuracy",
            "LogLoss",
            "LogLossReduction",
        ),
    ),

    FORECASTING(
        displayName = "Forecasting",
        description = "Predict future trends in time-series data",
        cliCommand = "forecasting",
        iconPath = "/icons/scenario-forecasting.svg",
        exampleUseCases = listOf(
            "Revenue forecasting",
            "Inventory planning",
            "Energy demand prediction",
            "Traffic volume estimation",
        ),
        relevantMetrics = listOf(
            "RMSE",
            "MAE",
            "MeanForecastError",
            "HorizonRMSE",
        ),
    ),

    ANOMALY_DETECTION(
        displayName = "Anomaly Detection",
        description = "Identify unusual patterns in data",
        cliCommand = "anomaly-detection",
        iconPath = "/icons/scenario-anomaly.svg",
        exampleUseCases = listOf(
            "Fraud detection",
            "Network intrusion detection",
            "Equipment failure prediction",
            "Quality assurance monitoring",
        ),
        relevantMetrics = listOf(
            "AUC",
            "DetectionRate",
            "FalsePositiveRate",
            "F1Score",
        ),
    );

    companion object {
        /**
         * Resolves a scenario from the CLI command string
         * (e.g. the value stored inside an .mbconfig file).
         */
        fun fromCliCommand(command: String): MLScenario? =
            entries.firstOrNull { it.cliCommand.equals(command, ignoreCase = true) }

        /**
         * Resolves a scenario from its display name.
         */
        fun fromDisplayName(name: String): MLScenario? =
            entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) }
    }
    
    /**
     * Determines whether this scenario is supported by the underlying ML.NET CLI
     * on the current host operating system and architecture.
     */
    fun isSupportedOnCurrentPlatform(): Boolean {
        // The ML.NET CLI currently omits these commands on Apple Silicon (ARM64)
        // due to missing native dependencies (like TensorFlow/ONNX).
        val isMacArm = com.intellij.openapi.util.SystemInfo.isMac && com.intellij.util.system.CpuArch.isArm64()
        if (isMacArm) {
            return this == CLASSIFICATION || this == REGRESSION || this == RECOMMENDATION
        }
        return true
    }
}
