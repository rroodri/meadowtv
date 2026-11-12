# MEADOW TV — Liquid Glass Launcher (Android 9 / API 28)

Proyecto Android Studio (Java, sin dependencias externas). **Estado: código completo, SIN compilar ni probar.**

## Compilar (necesita tu PC)
1. Instala Android Studio (incluye JDK 17) y abre la carpeta `MeadowTV`.
2. Cuando lo pida, instala el SDK Platform 34 y deja que Gradle sincronice (necesita Internet esa vez).
3. Menú **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Instalar con pendrive
1. Copia `app-debug.apk` al pendrive y conéctalo al TV Box.
2. Con un explorador de archivos del box (p. ej. el instalador de la caja o "Files"), abre el APK.
3. Si lo pide: Ajustes > Seguridad > Orígenes desconocidos, permite ese explorador y repite.

## Usar como pantalla de inicio
- Abre MEADOW TV y pulsa **Home**: Android pregunta qué launcher usar; elige MEADOW TV y "Siempre".
- Si no pregunta (firmware Hyundai): Ajustes > Aplicaciones > Aplicaciones predeterminadas > Pantalla de inicio.

## Volver al launcher original
Ajustes > Aplicaciones > MEADOW TV > "Borrar valores predeterminados" (o Aplicaciones predeterminadas > Pantalla de inicio > launcher original). No se toca el launcher original.

## Personalizar
- Foto real de fondo: copia un JPG (máx. 1920x1080) como `app/src/main/res/drawable-nodpi/meadow_wallpaper.jpg`. Si no existe, se usa la pradera dibujada por código.
- Si YouTube / Apple Music / Play Store no se detectan, o quieres otra app: mantén pulsado OK sobre la tarjeta y elige.
