# GuardaEstados

**GuardaEstados** es una aplicación Android para visualizar y descargar los estados de WhatsApp y WhatsApp Business directamente desde tu dispositivo, compatible con Android 11+.

## Características
- Visualiza todos los estados (imágenes y videos) de WhatsApp y WhatsApp Business.
- Descarga estados a tu almacenamiento local.
- Interfaz intuitiva y moderna.
- Soporte para múltiples idiomas.
- Persistencia de la instancia seleccionada (WhatsApp o Business).

## Instalación

1. **Clona el repositorio:**
   ```bash
   git clone https://github.com/TimmyTurner502/GuardaEstados.git
   ```
2. **Abre el proyecto en Android Studio.**
3. **Conecta tu dispositivo o usa un emulador.**
4. **Ejecuta la app.**

## Permisos necesarios
- Acceso a almacenamiento para leer y guardar archivos de estados.
- En Android 11+ la app accede a `/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/.Statuses` y la ruta equivalente de Business.

## Problemas comunes
- **No aparecen estados:**
  - Asegúrate de haber visto los estados en WhatsApp/Business (los archivos solo existen si los has visto).
  - Verifica que la app tenga permisos de almacenamiento.
  - En algunos dispositivos, el acceso a la carpeta puede estar restringido por el fabricante.
  - Revisa el Logcat para ver si la app detecta archivos en la carpeta.
- **No aparecen opciones duales:**
  - Por restricciones de Android, la app solo puede acceder a la instancia principal de WhatsApp y Business.

## Contribuir
1. Haz un fork del repositorio.
2. Crea una rama para tu feature o fix: `git checkout -b mi-feature`.
3. Haz tus cambios y commitea: `git commit -am 'Agrega mi feature'`.
4. Haz push a tu rama: `git push origin mi-feature`.
5. Abre un Pull Request.

## Depuración
- Usa el Logcat de Android Studio y filtra por `FileUtils` o `EstadosTab` para ver los archivos detectados y depurar problemas de acceso.

## Licencia
Este proyecto está licenciado bajo la **GNU General Public License v3.0** (GPL v3).

La GPL v3 es una licencia de software libre que:
- Permite usar, modificar y distribuir el software
- **Requiere** que cualquier trabajo derivado también sea de código abierto
- **Requiere** que el código fuente esté disponible
- Protege los derechos de los usuarios finales

Para más detalles, consulta el archivo [LICENSE](LICENSE) en este repositorio.

### ¿Por qué GPL v3?
- **Protección**: Cualquier modificación debe mantenerse como código abierto
- **Transparencia**: El código fuente siempre debe estar disponible
- **Comunidad**: Fomenta la colaboración y el desarrollo abierto
- **Educativo**: Ideal para proyectos académicos y de aprendizaje 