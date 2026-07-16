# Flowday para Android

La aplicación Android incluye el bundle React dentro del APK mediante Capacitor. En producción consume:

`https://flowday-z8hp.onrender.com`

La URL se configura en `.env.android`. No agregues secretos a ese archivo: las variables `VITE_*` quedan incluidas en el APK.

Antes de desplegar el backend en Render, configura `MOBILE_JWT_SECRET` con un valor aleatorio de al menos 32 caracteres. El backend de producción no inicia con el secreto de desarrollo. Opcionalmente configura `MOBILE_ALLOWED_ORIGINS`; el valor predeterminado ya acepta `https://localhost` y `capacitor://localhost`.

## Compilar localmente

Requisitos:

- Node.js 22
- JDK 21
- Android Studio con Android SDK 36
- `ANDROID_HOME` configurado

```powershell
cd frontend
npm ci
npm run android:sync
cd android
.\gradlew.bat assembleDebug
```

APK generado:

`frontend/android/app/build/outputs/apk/debug/app-debug.apk`

Para abrir el proyecto:

```powershell
npm run android:open
```

## Compilar en GitHub

El workflow `.github/workflows/android-apk.yml` compila el APK en cada cambio de `main` que afecte `frontend/`, y también puede ejecutarse manualmente en GitHub Actions. El artefacto se llama `flowday-android-debug`.

## Release

El APK debug sirve para pruebas e instalación directa. Para Google Play se debe:

1. Crear un keystore privado fuera del repositorio.
2. Configurar firma release mediante secretos del pipeline.
3. Ejecutar `bundleRelease` para producir un AAB.
4. No subir archivos `.jks`, `.keystore` ni contraseñas al repositorio.
