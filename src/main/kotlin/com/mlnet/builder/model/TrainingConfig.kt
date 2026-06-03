package com.mlnet.builder.model

/**
 * Immutable configuration for a single ML.NET AutoML training run.
 *
 * An instance of this class is assembled by the wizard and then handed
 * to the training process runner.
 *
 * @property scenario             The ML scenario to train (classification, regression, …).
 * @property dataFilePath         Absolute path to the training data file (CSV / TSV).
 * @property labelColumn          Name of the column used as the label (target).
 * @property featureColumns       Names of columns used as features.
 * @property trainingTimeSeconds  Maximum wall-clock time budget for AutoML exploration.
 * @property useGpu               Whether to enable GPU acceleration (requires CUDA).
 * @property outputDirectory      Directory where generated artefacts are written.
 * @property modelName            Base name for the output model and generated code files.
 */
data class TrainingConfig(
    val scenario: MLScenario,
    val dataFilePath: String,
    val labelColumn: String,
    val featureColumns: List<String>,
    val trainingTimeSeconds: Int = 30,
    val useGpu: Boolean = false,
    val outputDirectory: String,
    val modelName: String,
) {
    /** Path where the serialised model (.zip) will be written. */
    val modelOutputPath: String
        get() = "$outputDirectory/$modelName.zip"

    /** Path where the mbconfig manifest will be written. */
    val mbconfigOutputPath: String
        get() = "$outputDirectory/$modelName.mbconfig"

    /**
     * Builds the CLI argument list for `mlnet` matching this configuration.
     *
     * Example output:
     * ```
     * ["classification", "--dataset", "/data/train.csv",
     *  "--label-col", "Sentiment", "--train-time", "30",
     *  "--output", "/out", "--name", "SentimentModel"]
     * ```
     */
    fun toCliArguments(): List<String> = buildList {
        add(scenario.cliCommand)
        add("--dataset"); add(dataFilePath)
        add("--label-col"); add(labelColumn)
        add("--train-time"); add(trainingTimeSeconds.toString())
        add("--output"); add(outputDirectory)
        add("--name"); add(modelName)
        if (useGpu) {
            add("--gpu")
        }
    }
}
