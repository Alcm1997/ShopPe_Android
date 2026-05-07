Auditoría y Registro de Configuración - ShopPe
Este documento registra los identificadores únicos y los cambios recientes para asegurar la persistencia de la configuración del proyecto.

Identificadores Críticos (IDs Únicos)
Componente	Valor	Ubicación
Firebase Project ID	apptienda-e4cf4	google-services.json
OAuth Client ID (Web)	944113440911-e6gprivm1354fnlqoi6dnpiibh75glml.apps.googleusercontent.com	LoginScreen.kt
API Key	AIzaSyBYhdfrIqvcoPvgo3-R3HaodNcXiLDyfcY	google-services.json
Package Name	com.idat.tiendonline	AndroidManifest.xml, build.gradle
IMPORTANT

No modificar estos valores a menos que se cambie de proyecto en la consola de Firebase, de lo contrario la Autenticación de Google y Firestore dejarán de funcionar.

Historial de Cambios de la Sesión
1. Unificación de Roles (Admin)
Se ha centralizado la validación del usuario administrador para evitar la fragmentación que existía anteriormente.

Archivo: 
AdminAccess.kt
Admin Oficial: yeffercastillovega24@gmail.com
Uso: Todas las pantallas (AppNavigation, MisPedidosViewModel) ahora consultan este archivo en lugar de usar correos hardcodeados.
2. Sincronización con Repositorio (Git Pull)
Se trajeron los últimos cambios realizados por el compañero Yefferson:

Commit 638a1b8: Unificación inicial de roles y permisos de Firestore.
Commit ff42c25: Actualizaciones menores de sincronización ("pe causaaa gaaa").
3. Estado de la Gestión de Productos
Se identificó un bloqueo en la creación de productos.
Causa probable: Falta de el documento metadata/counters en la consola de Firestore o falta de permisos en dicha colección.
Acción requerida: Verificar en la consola de Firebase que el usuario administrador tenga permisos sobre la colección metadata.
Estructura de Seguridad Sugerida para Firestore
Para que el Admin pueda agregar productos, las reglas en Firebase deben ser similares a:

javascript
match /productos/{document} {
  allow write: if request.auth.token.email == "yeffercastillovega24@gmail.com";
}
match /metadata/{document} {
  allow write: if request.auth.token.email == "yeffercastillovega24@gmail.com";
}