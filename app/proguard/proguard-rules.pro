-dontobfuscate

# Keep nested objects of sealed classes that use Kotlin reflection (::class.nestedClasses)
# for runtime enumeration. R8 strips them as "unused" since the references are reflection-only.
-keep class com.servalabs.perms.permissions.core.known.APerm$* { *; }
-keep class com.servalabs.perms.permissions.core.known.APermGrp$* { *; }
-keep class com.servalabs.perms.permissions.core.known.AExtraPerm$* { *; }
-keep class com.servalabs.perms.apps.core.known.AKnownPkg$* { *; }