import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
            implementation("io.github.vinceglb:filekit-dialogs:0.12.0")
            implementation("io.github.vinceglb:filekit-dialogs-compose:0.12.0")

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.poi.ooxml)
        }
        jvmTest.dependencies {
            implementation(libs.mockito.core)
        }
    }
}


val appPackageName = "CertDecoder"
val appVersion = "1.0.1"

compose.desktop {
    application {
        mainClass = "com.user.certdecoder.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = appPackageName
            packageVersion = appVersion
            outputBaseDir.set(project.layout.projectDirectory.dir("output"))

            windows {
                // Fixed for the lifetime of this app — do not regenerate. Windows Installer uses
                // this UpgradeCode to recognize a new build as an upgrade of a previous install
                // (rather than an unrelated product) and uninstalls the old version automatically.
                // Bump packageVersion above on every release for this to actually trigger.
                upgradeUuid = "0034ebe0-6708-4cce-a3b5-d18949651329"
                iconFile.set(project.file("icons/cert_decoder.ico"))
                installationPath = "Cert Decoder"
            }
            linux {
                iconFile.set(project.file("icons/cert_decoder_icon.png"))
            }
        }
    }
}
