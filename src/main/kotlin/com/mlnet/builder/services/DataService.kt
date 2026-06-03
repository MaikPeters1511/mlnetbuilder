package com.mlnet.builder.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.mlnet.builder.model.ColumnInfo
import com.mlnet.builder.model.ColumnDataType
import com.mlnet.builder.model.ColumnPurpose
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

data class DataPreview(val columns: List<ColumnInfo>, val rows: List<List<String>>, val totalRowCount: Int)

@Service(Service.Level.PROJECT)
class DataService(private val project: Project) {

    fun parseFile(filePath: String, delimiter: Char = ',', maxRows: Int = 100): DataPreview {
        val file = File(filePath)
        if (!file.exists()) return DataPreview(emptyList(), emptyList(), 0)

        val rows = mutableListOf<List<String>>()
        var totalRows = 0
        var header: List<String>? = null

        BufferedReader(FileReader(file)).use { reader ->
            var line: String? = reader.readLine()
            if (line != null) {
                header = line.split(delimiter)
                line = reader.readLine()
                while (line != null) {
                    if (totalRows < maxRows) {
                        rows.add(line.split(delimiter))
                    }
                    totalRows++
                    line = reader.readLine()
                }
            }
        }

        val columns = header?.mapIndexed { index, name ->
            val sampleValues = rows.mapNotNull { it.getOrNull(index) }
            ColumnInfo(
                name = name,
                dataType = inferDataType(sampleValues),
                purpose = ColumnPurpose.FEATURE,
                sampleValues = sampleValues.take(5)
            )
        } ?: emptyList()

        return DataPreview(columns, rows, totalRows)
    }

    fun inferDelimiter(filePath: String): Char {
        val file = File(filePath)
        if (!file.exists()) return ','
        
        val line = BufferedReader(FileReader(file)).use { it.readLine() } ?: return ','
        if (line.contains("\t")) return '\t'
        if (line.contains(";")) return ';'
        return ','
    }

    fun inferColumnTypes(filePath: String, delimiter: Char): List<ColumnInfo> {
        return parseFile(filePath, delimiter).columns
    }
    
    private fun inferDataType(values: List<String>): ColumnDataType {
        // very basic inference
        if (values.isEmpty()) return ColumnDataType.STRING
        val first = values.first()
        if (first.toIntOrNull() != null) return ColumnDataType.INTEGER
        if (first.toFloatOrNull() != null) return ColumnDataType.FLOAT
        if (first.equals("true", true) || first.equals("false", true)) return ColumnDataType.BOOLEAN
        return ColumnDataType.STRING
    }

    companion object {
        fun getInstance(project: Project): DataService {
            return project.getService(DataService::class.java)
        }
    }
}
