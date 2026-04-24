# SensorTV 2.0

**SensorTV 2.0** es una aplicación móvil para Android orientada a la **estimación del consumo energético de sensores** en dispositivos móviles. Permite monitorear en tiempo real el comportamiento energético de cinco sensores clave, realizar capturas de datos con parámetros configurables y exportar los resultados en formato CSV para su análisis.

La aplicación combina información del hardware de los sensores con el estado de la batería para estimar su consumo energético de forma aproximada.

## Características clave

SensorTV 2.0 permite observar el comportamiento energético de cinco sensores (acelerómetro, giroscopio, sensor de proximidad, sensor de luminosidad y magnetómetro) mediante:

- **Monitoreo en tiempo real** y **visualización gráfica dinámica** de la potencia estimada de los sensores.
- **Captura de datos configurable** (duración y frecuencia de muestreo).
- **Generación de dos archivos CSV** por cada captura: mediciones y resultados.
- **Historial local de registros** basado en las capturas realizadas.
- **Exportación de datos**, tanto individual (archivo CSV) como masiva (ZIP).

## Arquitectura

El proyecto sigue una arquitectura por capas:

- **Data**: fuentes de datos (sensores, batería), repositorios, servicio y persistencia.
- **Domain**: casos de uso que encapsulan la lógica de negocio (cálculo, captura, exportación).
- **UI**: ViewModels y pantallas reactivas con Jetpack Compose.

## Tecnologías principales

- Kotlin
- Jetpack Compose (UI declarativa)
- Coroutines & Flow
- Room (persistencia local)
- Android Foreground Service (captura en segundo plano)

## Consideraciones adicionales

- La aplicación está compilada con un Target SDK de Android 15 (API 35). Sin embargo, es compatible con dispositivos Android desde la versión 8.0 (API 26).
- La precisión de la estimación y captura de datos depende parcialmente del hardware del dispositivo y de la disponibilidad de los sensores.
- La aplicación está orientada a **análisis exploratorio**, así como a contextos **académicos o de investigación**.
