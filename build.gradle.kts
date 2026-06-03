import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(providers.gradleProperty("javaVersion").get().toInt())
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        rider(providers.gradleProperty("platformVersion"))

        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }

    implementation("com.google.code.gson:gson:2.11.0")

    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    instrumentCode = false
    pluginConfiguration {
        id = providers.gradleProperty("pluginGroup")
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }

        description = """
            <h2>ML.NET Builder for JetBrains Rider</h2>
            <p>Build, train, and deploy custom ML.NET machine learning models directly within JetBrains Rider.</p>
            <ul>
                <li><b>Step-by-step wizard</b> — guided workflow for model creation</li>
                <li><b>AutoML training</b> — automatically finds the best algorithm</li>
                <li><b>Code generation</b> — generates ready-to-use C# code</li>
                <li><b>Scenario support</b> — Classification, Regression, Recommendation, and more</li>
                <li><b>.mbconfig compatibility</b> — compatible with Visual Studio's Model Builder</li>
            </ul>
        """.trimIndent()
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks {
    wrapper {
        gradleVersion = "8.11.1"
    }
}
