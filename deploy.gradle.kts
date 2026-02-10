tasks.register("deploySentinel") {
    dependsOn(
        ":sentinel:clean",
        ":sentinel:publishReleasePublicationToSonatypeRepository"
    )
    group = "Deploy"
    description = "Deploy module to repositories"
}

tasks.register("deploySentinelNoOp") {
    dependsOn(
        ":sentinel-no-op:clean",
        ":sentinel-no-op:publishReleasePublicationToSonatypeRepository"
    )
    group = "Deploy"
    description = "Deploy module to repositories"
}

tasks.register("deployToolChucker") {
    dependsOn(
        ":tool-chucker:clean",
        ":tool-chucker:publishReleasePublicationToSonatypeRepository"
    )
    group = "Deploy"
    description = "Deploy module to repositories"
}

tasks.register("deployToolCollar") {
    dependsOn(
        ":tool-collar:clean",
        ":tool-collar:publishReleasePublicationToSonatypeRepository"
    )
    group = "Deploy"
    description = "Deploy module to repositories"
}

tasks.register("deployToolDbInspector") {
    dependsOn(
        ":tool-dbinspector:clean",
        ":tool-dbinspector:publishReleasePublicationToSonatypeRepository"
    )
    group = "Deploy"
    description = "Deploy module to repositories"
}

tasks.register("deployToolLeakCanary") {
    dependsOn(
        ":tool-leakcanary:clean",
        ":tool-leakcanary:publishReleasePublicationToSonatypeRepository"
    )
    group = "Deploy"
    description = "Deploy module to repositories"
}

tasks.register("deployToolAppGallery") {
    dependsOn(
        ":tool-appgallery:clean",
        ":tool-appgallery:publishReleasePublicationToSonatypeRepository"
    )
    group = "Deploy"
    description = "Deploy module to repositories"
}

tasks.register("deployToolGooglePlay") {
    dependsOn(
        ":tool-googleplay:clean",
        ":tool-googleplay:publishReleasePublicationToSonatypeRepository"
    )
    group = "Deploy"
    description = "Deploy module to repositories"
}

tasks.register("deployToolTimber") {
    dependsOn(
        ":tool-timber:clean",
        ":tool-timber:publishReleasePublicationToSonatypeRepository"
    )
    group = "Deploy"
    description = "Deploy module to repositories"
}

tasks.register("deployToolNetworkEmulatorOkhttp") {
    dependsOn(
        ":tool-networkemulator-okhttp:clean",
        ":tool-networkemulator-okhttp-no-op:clean",
        ":tool-networkemulator-okhttp:publishReleasePublicationToSonatypeRepository",
        ":tool-networkemulator-okhttp-no-op:publishReleasePublicationToSonatypeRepository"
    )
    group = "Deploy"
    description = "Deploy module to repositories"
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
        "deployToolTimber"
    )
    group = "Deploy"
    description = "Deploy all modules to repositories"
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
        ":sentinel:publishReleasePublicationToMavenLocal",
        ":sentinel-no-op:publishReleasePublicationToMavenLocal",
        ":tool-chucker:publishReleasePublicationToMavenLocal",
        ":tool-collar:publishReleasePublicationToMavenLocal",
        ":tool-dbinspector:publishReleasePublicationToMavenLocal",
        ":tool-leakcanary:publishReleasePublicationToMavenLocal",
        ":tool-appgallery:publishReleasePublicationToMavenLocal",
        ":tool-googleplay:publishReleasePublicationToMavenLocal",
        ":tool-timber:publishReleasePublicationToMavenLocal"
    )
    group = "Deploy"
    description = "Deploy all modules to Maven Local repository"
}
