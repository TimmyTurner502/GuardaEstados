# Configuración para Google Play Store - GuardaEstados

## 📋 Checklist de Preparación

### ✅ Configuración Técnica
- [x] ProGuard configurado para optimización
- [x] Minificación y shrinkResources habilitados
- [x] Permisos actualizados para Android 13+
- [x] Target SDK actualizado a 35
- [x] Configuración de keystore preparada

### 🔑 Keystore de Release (OBLIGATORIO)
1. **Generar keystore:**
   ```bash
   keytool -genkey -v -keystore guardaestados-release-key.keystore -alias guardaestados-key-alias -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Configurar keystore.properties:**
   - Reemplaza `tu_store_password_aqui` con tu contraseña del store
   - Reemplaza `tu_key_password_aqui` con tu contraseña de la key
   - Coloca el archivo keystore en la raíz del proyecto

### 📱 Recursos Requeridos para Google Play

#### Iconos de App
- **Icono principal:** 512x512 px (PNG)
- **Icono adaptativo:** 108x108 px (PNG)
- **Icono de Play Store:** 512x512 px (PNG)

#### Capturas de Pantalla
- **Mínimo:** 2 capturas por dispositivo
- **Recomendado:** 4-8 capturas por dispositivo
- **Dispositivos:** Phone (16:9), 7" tablet, 10" tablet
- **Resoluciones:**
  - Phone: 1080x1920 px
  - 7" tablet: 1200x1920 px
  - 10" tablet: 1920x1200 px

#### Video Promocional (Opcional)
- Duración: 30-120 segundos
- Formato: MP4
- Resolución: 1920x1080 px

### 📝 Información de la App

#### Descripción Corta (80 caracteres)
```
Guarda y comparte estados de WhatsApp fácilmente
```

#### Descripción Completa
```
GuardaEstados es la aplicación perfecta para guardar y compartir estados de WhatsApp de manera rápida y sencilla.

✨ CARACTERÍSTICAS PRINCIPALES:
• Guarda estados de WhatsApp automáticamente
• Vista previa de imágenes y videos
• Comparte estados con un solo toque
• Envía mensajes de WhatsApp directamente
• Soporte para múltiples idiomas
• Interfaz moderna y fácil de usar
• Modo oscuro y claro
• Paletas de colores personalizables

📱 FUNCIONALIDADES:
• Búsqueda y filtros avanzados
• Acciones en lote (compartir, descargar, eliminar)
• Soporte para WhatsApp Dual
• Configuración de idioma personalizable
• Vista previa con barra translúcida o sólida

🌍 IDIOMAS SOPORTADOS:
• Español
• English
• Português (Brasil)
• हिन्दी (Hindi)

🔒 PRIVACIDAD:
• No recopilamos datos personales
• Acceso solo a archivos de WhatsApp
• Permisos mínimos necesarios

Descarga GuardaEstados ahora y nunca más pierdas un estado importante de WhatsApp!
```

#### Palabras Clave
```
whatsapp, estados, guardar, compartir, mensajes, dual, backup, status
```

### 🎯 Categoría y Etiquetas
- **Categoría principal:** Comunicación
- **Categoría secundaria:** Productividad
- **Etiquetas:** WhatsApp, Estados, Mensajes, Compartir

### 🔞 Clasificación de Contenido
- **Clasificación:** Para todos (3+)
- **Contenido:** Sin contenido inapropiado

### 📊 Política de Privacidad
Crear una política de privacidad que incluya:
- Qué datos recopilas
- Cómo los usas
- Con quién los compartes
- Cómo los proteges
- Derechos del usuario

### 🚀 Pasos Finales

1. **Generar APK firmado:**
   ```bash
   ./gradlew assembleRelease
   ```

2. **Generar Bundle para Play Store:**
   ```bash
   ./gradlew bundleRelease
   ```

3. **Subir a Google Play Console:**
   - Crear cuenta de desarrollador ($25 USD)
   - Crear nueva aplicación
   - Subir APK/Bundle
   - Completar información de la app
   - Configurar precios y distribución
   - Enviar para revisión

### ⚠️ Notas Importantes

1. **AdMob:** Reemplazar el ID de prueba con tu ID real de AdMob
2. **Permisos:** La app requiere permisos de almacenamiento para acceder a estados de WhatsApp
3. **Política de WhatsApp:** Asegúrate de cumplir con las políticas de WhatsApp
4. **Testing:** Probar exhaustivamente en diferentes dispositivos antes de publicar

### 📞 Soporte
Para soporte técnico o preguntas sobre la publicación, consulta la documentación de Google Play Console. 