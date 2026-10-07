# Contexto del Proyecto: TareoApp

## Resumen del Proyecto
TareoApp es una aplicación móvil enfocada en el control de personal, registro de asistencia y seguimiento de actividades (tareo) en campo. Su objetivo es optimizar el flujo de trabajo operando de manera offline-first y sincronizando datos cuando haya red.

## Roles de Usuario
- **Trabajador:** Usuario base cuyas horas o actividades son registradas y monitoreadas.
- **Tareador:** Encargado de campo (Supervisor/Controlador) que registra la asistencia de las cuadrillas, asigna tareas y maneja la recolección de datos localmente.
- **Admin:** Administrador encargado de la configuración global, gestión de proyectos, empleados, centros de costo y visualización de reportes integrales.

## Funciones Clave
- Registro de Asistencia y Tareas (Check-in/Check-out).
- Funcionamiento Offline-First (Local Database).
- Sincronización de Datos con el Backend.

## Arquitectura y Stack Tecnológico
- **Arquitectura:** Clean Architecture y Single Source of Truth, aplicando el patrón MVVM.
- **Lenguaje:** Kotlin.
- **UI:** Jetpack Compose (Material 3).
- **Inyección de Dependencias:** Dagger Hilt.
- **Base de Datos:** Room.
- **Navegación:** Navigation Compose.
- **Asincronismo:** Kotlin Coroutines y Flow.