package com.mlnet.builder.filetype

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

class MbConfigFileType private constructor() : LanguageFileType(PlainTextLanguage.INSTANCE) {

    override fun getName(): String = "MbConfig"

    override fun getDescription(): String = "ML.NET Model Builder Configuration"

    override fun getDefaultExtension(): String = "mbconfig"

    override fun getIcon(): Icon = IconLoader.getIcon("/icons/mlnet-logo.svg", MbConfigFileType::class.java)

    companion object {
        @JvmField
        val INSTANCE = MbConfigFileType()
    }
}
