package com.mlnet.builder.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*

class MLNetSettingsConfigurable : BoundConfigurable("ML.NET Builder") {

    private val settings = MLNetSettings.getInstance()

    override fun createPanel(): DialogPanel = panel {
        group("Training Defaults") {
            row("Default training time (seconds):") {
                spinner(10..3600, 10)
                    .bindIntValue(settings::defaultTrainingTimeSeconds)
                    .comment("How long AutoML will explore algorithms (10-3600 seconds)")
            }
            row("Use GPU by default:") {
                checkBox("")
                    .bindSelected(settings::defaultUseGpu)
                    .comment("Enable GPU acceleration for training (requires compatible GPU)")
            }
            row("Show training logs:") {
                checkBox("")
                    .bindSelected(settings::showTrainingLogs)
                    .comment("Show detailed console output during training")
            }
        }

        group("Tool Paths") {
            row("dotnet SDK path:") {
                textFieldWithBrowseButton(
                    FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle("Select dotnet SDK Directory")
                )
                    .bindText(settings::dotnetSdkPath)
                    .comment("Leave empty to use system PATH")
                    .columns(COLUMNS_LARGE)
            }
            row("mlnet CLI path:") {
                textFieldWithBrowseButton(
                    FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor().withTitle("Select mlnet CLI Executable")
                )
                    .bindText(settings::mlnetCliPath)
                    .comment("Leave empty to use globally installed mlnet tool")
                    .columns(COLUMNS_LARGE)
            }
        }

        group("Code Generation") {
            row("Auto-add NuGet packages:") {
                checkBox("")
                    .bindSelected(settings::autoAddNuGetPackages)
                    .comment("Automatically add Microsoft.ML NuGet packages to the project")
            }
            row("Default output directory:") {
                textFieldWithBrowseButton(
                    FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle("Select Default Output Directory")
                )
                    .bindText(settings::defaultOutputDirectory)
                    .comment("Leave empty to use the project directory")
                    .columns(COLUMNS_LARGE)
            }
        }

        group("Data Preview") {
            row("Max preview rows:") {
                spinner(10..10000, 10)
                    .bindIntValue(settings::maxPreviewRows)
                    .comment("Maximum number of rows shown in data preview (10-10000)")
            }
        }
    }
}
