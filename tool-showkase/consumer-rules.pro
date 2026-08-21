# ShowkaseBrowserActivity appends "Codegen" to the canonical name of the app's @ShowkaseRoot module
# and calls Class.forName on the result, so both names must survive shrinking and obfuscation.
# Showkase's own AAR ships an empty proguard.txt, so these rules are supplied here instead.

# The generated class is annotated with @ShowkaseRootCodegen, which has RUNTIME retention.
-keepattributes RuntimeVisibleAnnotations

# The app's @ShowkaseRoot class, whose canonical name ShowkaseTool passes to the browser.
-keep class * implements com.airbnb.android.showkase.annotation.ShowkaseRootModule { *; }

# The generated provider, matched two ways in case R8 cannot see one of the supertypes.
# Members are kept too because the class is only ever instantiated reflectively.
-keep class * implements com.airbnb.android.showkase.models.ShowkaseProvider { *; }
-keep @com.airbnb.android.showkase.annotation.ShowkaseRootCodegen class * { *; }
