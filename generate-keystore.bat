@echo off
echo ========================================
echo Generando Keystore para GuardaEstados
echo ========================================
echo.

echo Este script generará un keystore para firmar tu app para Google Play Store.
echo IMPORTANTE: Guarda bien las contraseñas que ingreses.
echo.

set /p store_password="Ingresa la contraseña del store (mínimo 6 caracteres): "
set /p key_password="Ingresa la contraseña de la key (mínimo 6 caracteres): "

echo.
echo Generando keystore...
keytool -genkey -v -keystore guardaestados-release-key.keystore -alias guardaestados-key-alias -keyalg RSA -keysize 2048 -validity 10000 -storepass %store_password% -keypass %key_password% -dname "CN=GuardaEstados, OU=Development, O=GuardaEstados, L=City, S=State, C=Country"

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo Keystore generado exitosamente!
    echo ========================================
    echo.
    echo Ahora actualiza el archivo keystore.properties con:
    echo storePassword=%store_password%
    echo keyPassword=%key_password%
    echo.
    echo El archivo keystore.properties ya está configurado con los valores correctos.
    echo.
    echo IMPORTANTE: Guarda el archivo keystore y las contraseñas en un lugar seguro.
    echo Sin ellos no podrás actualizar tu app en Google Play Store.
    echo.
) else (
    echo.
    echo Error al generar el keystore. Verifica que Java esté instalado y en el PATH.
    echo.
)

pause 