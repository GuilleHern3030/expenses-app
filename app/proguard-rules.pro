#############################################
# RETROFIT (OBLIGATORIO)
#############################################

# Mantener información genérica (CRÍTICO)
-keepattributes Signature
-keepattributes Exceptions

# Mantener anotaciones de runtime
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations

# Mantener interfaces de Retrofit
-keep interface retrofit2.** { *; }

# Mantener métodos anotados con HTTP
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

#############################################
# OKHTTP
#############################################

-dontwarn okhttp3.**
-dontwarn okio.**

#############################################
# GSON
#############################################

# Mantener modelos serializados
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

#############################################
# TU APP (MODELOS USADOS POR LA API)
#############################################

# Ajustá el paquete si lo movés
-keep class enel.dev.budgets.data.** { *; }

#############################################
# ANDROID (SEGURIDAD)
#############################################

-dontwarn javax.annotation.**