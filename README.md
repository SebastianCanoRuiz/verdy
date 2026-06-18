# Verdy 🌿

Verdy es una aplicación Android offline-first para la gestión personal de plantas. Permite registrar tus plantas, programar recordatorios de cuidado, llevar un historial de mantenimiento y exportar/importar tus datos entre dispositivos.

## Características

- **100% offline** — sin cuentas, sin servidores, sin internet requerido
- **Gestión de plantas** — nombre, foto, cuidados, estado y notas
- **Recordatorios locales** — riego, abono, trasplante, poda y cuidados personalizados
- **Historial de mantenimiento** — registro completo de cuidados realizados
- **Dashboard** — resumen visual del estado del jardín
- **Exportación/Importación** — archivo ZIP o código QR para migrar datos

## Arquitectura

El proyecto sigue los principios de **Clean Architecture** con tres capas claramente separadas:

```
Presentation (Jetpack Compose + ViewModel)
     ↕
Domain (Use Cases + Entities + Repository Interfaces)
     ↕
Data (Room + DataStore + WorkManager)
```

## Stack tecnológico

| Categoría | Tecnología |
|-----------|-----------|
| Lenguaje | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Inyección de dependencias | Hilt |
| Base de datos | Room |
| Preferencias | DataStore |
| Tareas en segundo plano | WorkManager |
| Imágenes | Coil |
| QR | ZXing |
| Serialización | Kotlinx Serialization |

## Requisitos

- Android 8.0 (API 26) o superior
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17

## Configuración

1. Clonar el repositorio
2. Abrir en Android Studio
3. Sincronizar Gradle
4. Ejecutar en dispositivo/emulador

```bash
git clone https://github.com/SebastianCanoRuiz/verdy.git
cd verdy
./gradlew assembleDebug
```

## Estructura del proyecto

```
app/src/main/java/com/verdy/
├── di/               # Módulos Hilt
├── domain/
│   ├── model/        # Entidades del dominio
│   ├── repository/   # Interfaces de repositorios
│   └── usecase/      # Casos de uso
├── data/
│   ├── local/        # Room DB + DataStore
│   ├── repository/   # Implementaciones
│   └── worker/       # WorkManager workers
├── presentation/
│   ├── theme/        # Tema Material 3
│   ├── navigation/   # Navegación
│   └── screen/       # Pantallas
└── VerdyApp.kt
```

## Licencia

Este proyecto está licenciado bajo la GNU General Public License v3.0. Ver [LICENSE](LICENSE) para más detalles.
