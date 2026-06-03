package com.mlnet.builder.model

import com.google.gson.annotations.SerializedName

/**
 * The primitive data type of a dataset column as inferred during data loading.
 */
enum class ColumnDataType(val displayName: String) {
    @SerializedName("string")
    STRING("String"),

    @SerializedName("float")
    FLOAT("Float"),

    @SerializedName("integer")
    INTEGER("Integer"),

    @SerializedName("boolean")
    BOOLEAN("Boolean"),

    @SerializedName("datetime")
    DATETIME("DateTime");

    companion object {
        /**
         * Attempts to infer a [ColumnDataType] from the raw string values of a column.
         * Falls back to [STRING] when no more specific type can be determined.
         */
        fun infer(values: List<String>): ColumnDataType {
            if (values.isEmpty()) return STRING
            val nonBlank = values.filter { it.isNotBlank() }
            if (nonBlank.isEmpty()) return STRING

            if (nonBlank.all { it.equals("true", ignoreCase = true) || it.equals("false", ignoreCase = true) }) {
                return BOOLEAN
            }
            if (nonBlank.all { it.toIntOrNull() != null }) return INTEGER
            if (nonBlank.all { it.toDoubleOrNull() != null }) return FLOAT
            // Simple ISO-date heuristic (yyyy-MM-dd...)
            if (nonBlank.all { it.length >= 10 && it[4] == '-' && it[7] == '-' }) return DATETIME
            return STRING
        }
    }
}

/**
 * The intended role of a column within the ML pipeline.
 */
enum class ColumnPurpose(val displayName: String) {
    @SerializedName("label")
    LABEL("Label"),

    @SerializedName("feature")
    FEATURE("Feature"),

    @SerializedName("ignore")
    IGNORE("Ignore"),

    @SerializedName("weight")
    WEIGHT("Weight");
}

/**
 * Describes a single column in a training dataset.
 *
 * @property name       The header / column name.
 * @property dataType   Inferred or user-specified data type.
 * @property purpose    The role this column plays during training.
 * @property sampleValues The first few values from the dataset, kept for UI preview.
 */
data class ColumnInfo(
    val name: String,
    val dataType: ColumnDataType,
    val purpose: ColumnPurpose,
    val sampleValues: List<String> = emptyList(),
) {
    /** `true` when this column is the label (target) column. */
    val isLabel: Boolean get() = purpose == ColumnPurpose.LABEL

    /** `true` when this column is used as a feature. */
    val isFeature: Boolean get() = purpose == ColumnPurpose.FEATURE
}
