package com.mlnet.builder.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a single algorithm trial explored during AutoML training.
 *
 * @property algorithmName  The trainer / algorithm name (e.g. "LightGbm", "FastTree").
 * @property rank           1-based rank in the leaderboard (1 = best).
 * @property metrics        Evaluation metric values for this trial.
 */
data class ExploredModel(
    @SerializedName("algorithmName")
    val algorithmName: String,

    @SerializedName("rank")
    val rank: Int,

    @SerializedName("metrics")
    val metrics: Map<String, Double>,
) : Comparable<ExploredModel> {
    /**
     * Natural ordering: ascending rank (best model first).
     */
    override fun compareTo(other: ExploredModel): Int = rank.compareTo(other.rank)

    /**
     * Returns the value of a specific metric, or `null` if not recorded.
     */
    fun metric(name: String): Double? = metrics[name]
}

/**
 * The complete outcome of an ML.NET AutoML training run.
 *
 * @property bestAlgorithm           Name of the winning algorithm.
 * @property metrics                 Evaluation metrics for the best model.
 * @property modelFilePath           Absolute path to the serialised model (.zip).
 * @property trainingDurationSeconds Wall-clock training time in seconds.
 * @property exploredModels          Leaderboard of all algorithm trials.
 * @property logs                    Raw console log lines captured during training.
 */
data class TrainingResult(
    @SerializedName("bestAlgorithm")
    val bestAlgorithm: String,

    @SerializedName("metrics")
    val metrics: Map<String, Double>,

    @SerializedName("modelFilePath")
    val modelFilePath: String,

    @SerializedName("trainingDurationSeconds")
    val trainingDurationSeconds: Long,

    @SerializedName("exploredModels")
    val exploredModels: List<ExploredModel>,

    @SerializedName("logs")
    val logs: List<String> = emptyList(),
) {
    /** `true` when at least one model was successfully trained. */
    val isSuccessful: Boolean
        get() = bestAlgorithm.isNotBlank() && modelFilePath.isNotBlank()

    /** Convenience: the top-ranked explored model, if any. */
    val bestModel: ExploredModel?
        get() = exploredModels.minByOrNull { it.rank }

    /** Returns the value of a best-model metric by name, or `null` if absent. */
    fun bestMetric(name: String): Double? = metrics[name]

    /** Returns a formatted multi-line summary suitable for log output or tooltips. */
    fun toSummaryString(): String = buildString {
        appendLine("Training Result")
        appendLine("═══════════════════════════════════")
        appendLine("Best Algorithm : $bestAlgorithm")
        appendLine("Duration       : ${trainingDurationSeconds}s")
        appendLine("Model File     : $modelFilePath")
        appendLine("Metrics:")
        metrics.forEach { (key, value) ->
            appendLine("  $key = ${"%.4f".format(value)}")
        }
        if (exploredModels.isNotEmpty()) {
            appendLine("Explored Models (${exploredModels.size}):")
            exploredModels.sortedBy { it.rank }.forEach { model ->
                appendLine("  #${model.rank} ${model.algorithmName}")
            }
        }
    }
}
