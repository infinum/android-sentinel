tasks.register("deploySentinel") {
    dependsOn(
        ":sentinel:clean",
        ":sentinel:publishMavenPublicationToMavenCentralRepository"
    )
    group = "Deploy"
    description = "Deploy Sentinel module to Maven Central repository"
}

tasks.register("deploySentinelNoOp") {
    dependsOn(
        ":sentinel-no-op:clean",
        ":sentinel-no-op:publishMavenPublicationToMavenCentralRepository"
    )
    group = "Deploy"
    description = "Deploy Sentinel No-Op module to Maven Central repository"
}

tasks.register("deployToolChucker") {
    dependsOn(
        ":tool-chucker:clean",
        ":tool-chucker:publishMavenPublicationToMavenCentralRepository"
    )
    group = "Deploy"
    description = "Deploy Tool Chucker module to Maven Central repository"
}

tasks.register("deployToolCollar") {
    dependsOn(
        ":tool-collar:clean",
        ":tool-collar:publishMavenPublicationToMavenCentralRepository"
    )
    group = "Deploy"
    description = "Deploy Tool Collar module to Maven Central repository"
}

tasks.register("deployToolDbInspector") {
    dependsOn(
        ":tool-dbinspector:clean",
        ":tool-dbinspector:publishMavenPublicationToMavenCentralRepository"
    )
    group = "Deploy"
    description = "Deploy Tool DbInspector module to Maven Central repository"
}

tasks.register("deployToolLeakCanary") {
    dependsOn(
        ":tool-leakcanary:clean",
        ":tool-leakcanary:publishMavenPublicationToMavenCentralRepository"
    )
    group = "Deploy"
    description = "Deploy Tool LeakCanary module to Maven Central repository"
}

tasks.register("deployToolAppGallery") {
    dependsOn(
        ":tool-appgallery:clean",
        ":tool-appgallery:publishMavenPublicationToMavenCentralRepository"
    )
    group = "Deploy"
    description = "Deploy Tool AppGallery module to Maven Central repository"
}

tasks.register("deployToolGooglePlay") {
    dependsOn(
        ":tool-googleplay:clean",
        ":tool-googleplay:publishMavenPublicationToMavenCentralRepository"
    )
    group = "Deploy"
    description = "Deploy Tool GooglePlay module to Maven Central repository"
}

tasks.register("deployToolTimber") {
    dependsOn(
        ":tool-timber:clean",
        ":tool-timber:publishMavenPublicationToMavenCentralRepository"
    )
    group = "Deploy"
    description = "Deploy Tool Timber module to Maven Central repository"
}

tasks.register("deployToolNetworkEmulatorOkhttp") {
    dependsOn(
        ":tool-networkemulator-okhttp:clean",
        ":tool-networkemulator-okhttp-no-op:clean",
        ":tool-networkemulator-okhttp:publishMavenPublicationToMavenCentralRepository",
        ":tool-networkemulator-okhttp-no-op:publishMavenPublicationToMavenCentralRepository"
    )
    group = "Deploy"
    description = "Deploy Tool NetworkEmulator OkHttp modules to Maven Central repository"
}

tasks.register("deploySentinelAll") {
    dependsOn(
        "deploySentinel",
        "deploySentinelNoOp"
    )
    group = "Deploy"
    description = "Deploy all Sentinel modules to repositories"
}

tasks.register("deployToolsAll") {
    dependsOn(
        "deployToolChucker",
        "deployToolCollar",
        "deployToolLeakCanary",
        "deployToolDbInspector",
        "deployToolAppGallery",
        "deployToolGooglePlay",
        "deployToolTimber",
        "deployToolNetworkEmulatorOkhttp"
    )
    group = "Deploy"
    description = "Deploy all tools modules to repositories"
}

tasks.register("deployAll") {
    dependsOn(
        "deploySentinel",
        "deploySentinelNoOp",
        "deployToolChucker",
        "deployToolCollar",
        "deployToolDbInspector",
        "deployToolLeakCanary",
        "deployToolAppGallery",
        "deployToolGooglePlay",
        "deployToolTimber",
        "deployToolNetworkEmulatorOkhttp"
    )
    group = "Deploy"
    description = "Deploy all modules to Maven Central repository"
}

tasks.register("deployDebug") {
    dependsOn(
        ":sentinel:clean",
        ":sentinel-no-op:clean",
        ":tool-chucker:clean",
        ":tool-collar:clean",
        ":tool-dbinspector:clean",
        ":tool-leakcanary:clean",
        ":tool-appgallery:clean",
        ":tool-googleplay:clean",
        ":tool-timber:clean",
        ":tool-networkemulator-okhttp:clean",
        ":tool-networkemulator-okhttp-no-op:clean",
        ":sentinel:publishMavenPublicationToMavenLocal",
        ":sentinel-no-op:publishMavenPublicationToMavenLocal",
        ":tool-chucker:publishMavenPublicationToMavenLocal",
        ":tool-collar:publishMavenPublicationToMavenLocal",
        ":tool-dbinspector:publishMavenPublicationToMavenLocal",
        ":tool-leakcanary:publishMavenPublicationToMavenLocal",
        ":tool-appgallery:publishMavenPublicationToMavenLocal",
        ":tool-googleplay:publishMavenPublicationToMavenLocal",
        ":tool-timber:publishMavenPublicationToMavenLocal",
        ":tool-networkemulator-okhttp:publishMavenPublicationToMavenLocal",
        ":tool-networkemulator-okhttp-no-op:publishMavenPublicationToMavenLocal"
    )
    group = "Deploy"
    description = "Deploy all modules to Maven Local repository for debugging"
}
