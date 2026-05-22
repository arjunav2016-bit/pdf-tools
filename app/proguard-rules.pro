# PDFBox Android and its font/image parsers are reached through reflective code paths.
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**

# Apache POI uses XML schema and service-discovered implementations for Office conversion.
-keep class org.apache.poi.** { *; }
-keep class org.openxmlformats.schemas.** { *; }
-dontwarn org.apache.poi.**
-dontwarn org.openxmlformats.schemas.**
-dontwarn aQute.bnd.annotation.spi.**
-dontwarn java.awt.**
-dontwarn javax.xml.stream.**
-dontwarn net.sf.saxon.**
-dontwarn org.osgi.framework.**

# Preserve generated Hilt entry points and injected members.
-keep class dagger.hilt.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class **_HiltModules* { *; }
-keep class **_Factory { *; }

# ML Kit model/component discovery needs its generated component metadata.
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text_common.** { *; }
-dontwarn com.google.mlkit.**
