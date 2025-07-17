@echo off
echo ========================================
echo Generando Release para GuardaEstados
echo ========================================
echo.

echo Limpiando proyecto...
call gradlew clean

echo.
echo Generando APK de release...
call gradlew assembleRelease

if %errorlevel% equ 0 (
    echo.
    echo APK generado exitosamente en: app/build/outputs/apk/release/
    echo.
) else (
    echo.
    echo Error al generar APK. Revisa los errores arriba.
    echo.
    pause
    exit /b 1
)

echo.
echo Generando Bundle para Google Play Store...
call gradlew bundleRelease

if %errorlevel% equ 0 (
    echo.
    echo Bundle generado exitosamente en: app/build/outputs/bundle/release/
    echo.
    echo ========================================
    echo ¡Build completado exitosamente!
    echo ========================================
    echo.
    echo Archivos generados:
    echo - APK: app/build/outputs/apk/release/app-release.apk
    echo - Bundle: app/build/outputs/bundle/release/app-release.aab
    echo.
    echo El archivo .aab es el que debes subir a Google Play Store.
    echo.
) else (
    echo.
    echo Error al generar Bundle. Revisa los errores arriba.
    echo.
)

pause 