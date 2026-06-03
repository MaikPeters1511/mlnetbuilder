package com.mlnet.builder.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(
    name = "MLNetBuilderSettings",
    storages = [Storage("MLNetBuilderSettings.xml")]
)
class MLNetSettings : PersistentStateComponent<MLNetSettings.State> {

    data class State(
        var defaultTrainingTimeSeconds: Int = 30,
        var defaultUseGpu: Boolean = false,
        var mlnetCliPath: String = "",
        var dotnetSdkPath: String = "",
        var autoAddNuGetPackages: Boolean = true,
        var defaultOutputDirectory: String = "",
        var showTrainingLogs: Boolean = true,
        var maxPreviewRows: Int = 100
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    var defaultTrainingTimeSeconds: Int
        get() = myState.defaultTrainingTimeSeconds
        set(value) { myState.defaultTrainingTimeSeconds = value }

    var defaultUseGpu: Boolean
        get() = myState.defaultUseGpu
        set(value) { myState.defaultUseGpu = value }

    var mlnetCliPath: String
        get() = myState.mlnetCliPath
        set(value) { myState.mlnetCliPath = value }

    var dotnetSdkPath: String
        get() = myState.dotnetSdkPath
        set(value) { myState.dotnetSdkPath = value }

    var autoAddNuGetPackages: Boolean
        get() = myState.autoAddNuGetPackages
        set(value) { myState.autoAddNuGetPackages = value }

    var defaultOutputDirectory: String
        get() = myState.defaultOutputDirectory
        set(value) { myState.defaultOutputDirectory = value }

    var showTrainingLogs: Boolean
        get() = myState.showTrainingLogs
        set(value) { myState.showTrainingLogs = value }

    var maxPreviewRows: Int
        get() = myState.maxPreviewRows
        set(value) { myState.maxPreviewRows = value }

    companion object {
        fun getInstance(): MLNetSettings {
            return ApplicationManager.getApplication().getService(MLNetSettings::class.java)
        }
    }
}
