package com.mlnet.builder.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import java.io.File
import java.io.Reader

/**
 * Configuration for the training data source inside an .mbconfig file.
 *
 * @property type      Source type identifier (e.g. "file", "database").
 * @property filePath  Path to the data file relative to the .mbconfig location.
 * @property delimiter Column delimiter (e.g. "," or "\t").
 */
data class DataSourceConfig(
    @SerializedName("Type")
    val type: String = "file",

    @SerializedName("FilePath")
    val filePath: String,

    @SerializedName("Delimiter")
    val delimiter: String = ",",
)

/**
 * Environment settings stored in an .mbconfig file.
 *
 * @property type   Runtime type identifier (e.g. "local").
 * @property useGpu Whether GPU acceleration is enabled.
 */
data class EnvironmentConfig(
    @SerializedName("Type")
    val type: String = "local",

    @SerializedName("UseGpu")
    val useGpu: Boolean = false,
)

/**
 * A single entry in the run-history array of an .mbconfig file.
 *
 * @property startTime     ISO-8601 formatted start timestamp.
 * @property endTime       ISO-8601 formatted end timestamp.
 * @property bestAlgorithm Name of the winning algorithm in this run.
 * @property metrics       Evaluation metrics for the best model.
 * @property modelPath     Relative path to the serialised model file.
 */
data class RunHistoryEntry(
    @SerializedName("StartTime")
    val startTime: String,

    @SerializedName("EndTime")
    val endTime: String,

    @SerializedName("BestAlgorithm")
    val bestAlgorithm: String,

    @SerializedName("Metrics")
    val metrics: Map<String, Double>,

    @SerializedName("ModelPath")
    val modelPath: String,
)

/**
 * Kotlin representation of a **.mbconfig** JSON manifest.
 *
 * This format is compatible with Visual Studio's ML.NET Model Builder so that
 * projects can be opened interchangeably between Rider and Visual Studio.
 *
 * @property trainedModelFilePath Path to the most recently trained model (.zip), or `null`.
 * @property scenario             CLI command name of the training scenario.
 * @property dataSource           Data source configuration.
 * @property environment          Environment / runtime configuration.
 * @property labelColumn          Name of the label (target) column.
 * @property featureColumns       Names of the feature columns.
 * @property trainingTime         Training time budget in seconds.
 * @property runHistory           Chronological list of past training runs.
 * @property version              Schema version of the .mbconfig format.
 */
data class MbConfig(
    @SerializedName("TrainedModelFilePath")
    val trainedModelFilePath: String? = null,

    @SerializedName("Scenario")
    val scenario: String,

    @SerializedName("DataSource")
    val dataSource: DataSourceConfig,

    @SerializedName("Environment")
    val environment: EnvironmentConfig,

    @SerializedName("LabelColumn")
    val labelColumn: String,

    @SerializedName("FeatureColumns")
    val featureColumns: List<String>,

    @SerializedName("TrainingTime")
    val trainingTime: Int,

    @SerializedName("RunHistory")
    val runHistory: List<RunHistoryEntry> = emptyList(),

    @SerializedName("Version")
    val version: String = "1.0",
) {
    /**
     * Resolves the [MLScenario] enum entry that matches [scenario],
     * or `null` when the value is unknown.
     */
    fun resolveScenario(): MLScenario? = MLScenario.fromCliCommand(scenario)

    /**
     * Returns a copy with the new [entry] appended to the run history and
     * [trainedModelFilePath] updated to the entry's model path.
     */
    fun withRunHistoryEntry(entry: RunHistoryEntry): MbConfig = copy(
        trainedModelFilePath = entry.modelPath,
        runHistory = runHistory + entry,
    )

    /**
     * Serialises this config to a pretty-printed JSON string.
     */
    fun toJson(): String = GSON.toJson(this)

    /**
     * Writes this config as pretty-printed JSON to [file].
     */
    fun saveTo(file: File) {
        file.parentFile?.mkdirs()
        file.writeText(toJson())
    }

    companion object {
        /** Shared, thread-safe Gson instance configured for .mbconfig I/O. */
        val GSON: Gson = GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create()

        /**
         * Deserialises an [MbConfig] from a JSON string.
         */
        fun fromJson(json: String): MbConfig = GSON.fromJson(json, MbConfig::class.java)

        /**
         * Deserialises an [MbConfig] from a [Reader].
         */
        fun fromReader(reader: Reader): MbConfig = GSON.fromJson(reader, MbConfig::class.java)

        /**
         * Loads an [MbConfig] from a file on disk.
         */
        fun loadFrom(file: File): MbConfig = file.reader().use { fromReader(it) }

        /**
         * Creates an initial [MbConfig] from a [TrainingConfig].
         * The resulting config has an empty run history and no trained model yet.
         */
        fun fromTrainingConfig(config: TrainingConfig): MbConfig = MbConfig(
            trainedModelFilePath = null,
            scenario = config.scenario.cliCommand,
            dataSource = DataSourceConfig(
                type = "file",
                filePath = config.dataFilePath,
                delimiter = ",",
            ),
            environment = EnvironmentConfig(
                type = "local",
                useGpu = config.useGpu,
            ),
            labelColumn = config.labelColumn,
            featureColumns = config.featureColumns,
            trainingTime = config.trainingTimeSeconds,
        )
    }
}
