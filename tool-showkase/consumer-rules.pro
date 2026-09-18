# ShowkaseBrowserActivity appends "Codegen" to the canonical name of the app's @ShowkaseRoot module
# and calls Class.forName on the result, so both names have to survive obfuscation. Showkase's own
# AAR ships an empty proguard.txt, so the rules live here.
-keep class * implements com.airbnb.android.showkase.annotation.ShowkaseRootModule
-keep @com.airbnb.android.showkase.annotation.ShowkaseRootCodegen class * { *; }

# The browser runtime is compileOnly, so ShowkaseTool's reference to it dangles in variants that
# did not supply it, and R8 fails those with "Missing class".
-dontwarn com.airbnb.android.showkase.ui.**
