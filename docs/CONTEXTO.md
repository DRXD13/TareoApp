# Contexto del Proyecto: TareoApp

## Resumen del Proyecto
TareoApp es una aplicación móvil enfocada en el control de personal, registro de asistencia y seguimiento de actividades (tareo) en campo. Su objetivo es optimizar el flujo de trabajo operando de manera offline-first y sincronizando datos cuando haya red.

## Roles de Usuario
- **Trabajador:** Usuario base cuyas horas o actividades son registradas y monitoreadas.
- **Tareador:** Encargado de campo (Supervisor/Controlador) que registra la asistencia de las cuadrillas, asigna tareas y maneja la recolección de datos localmente.
- **Admin:** Administrador encargado de la configuración global, gestión de proyectos, empleados, centros de costo y visualización de reportes integrales.

## Funciones clave
- Marcaje de entrada y salida con GPS, solo dentro de la geocerca (radio) de la sede.
- Detección de ubicación simulada (GPS falso).
- Hora confiable para marcajes offline (guardar también el tiempo transcurrido desde el arranque del equipo).
- Tareo de cuadrillas por QR o DNI, con labor y lote.
- Offline-first: los marcajes y tareos se guardan en Room y se sincronizan con WorkManager.

## Tareo de cuadrillas
- Los tareadores (encargados) pueden registrar la asistencia y avance de los trabajadores de una cuadrilla en una labor y un lote específicos.
- El registro se realiza mediante la lectura del código QR del trabajador o digitando manualmente su DNI.
- Existen restricciones en el registro: el DNI debe corresponder a un trabajador activo en la base de datos local y no se permite registrar a la misma persona más de una vez en el mismo día para la misma labor y lote.
- Estos registros (`CrewTareoEntity`) se almacenan de manera local con estado `PENDING` y son sincronizados junto con los marcajes convencionales a través de `SyncAttendanceWorker`.
- Modo demo con datos de ejemplo que funciona sin servidor.
- API REST propia con Retrofit (fase posterior).

## Flujo offline
Los marcajes se guardan primero de manera local en Room con un estado inicial `PENDING`. WorkManager se encarga de observar los marcajes en estado `PENDING` o `FAILED` y simula una subida en un worker asíncrono con un retraso de 2 segundos. Una vez procesados exitosamente, el estado cambia a `SYNCED` con un timestamp de sincronización; en caso de error o sin conexión, el worker vuelve a reintentar usando backoff exponencial, garantizando la persistencia offline.

## Convenciones
Código, clases y variables en inglés. Textos visibles para el usuario en español, dentro de strings.xml.

## Arquitectura y Stack Tecnológico
- **Arquitectura:** Clean Architecture y Single Source of Truth, aplicando el patrón MVVM.
- **Lenguaje:** Kotlin.
- **UI:** Jetpack Compose (Material 3).
- **Inyección de Dependencias:** Dagger Hilt.
- **Base de Datos:** Room.
- **Navegación:** Navigation Compose.
- **Asincronismo:** Kotlin Coroutines y Flow.