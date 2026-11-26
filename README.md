# ⚽ FutbolStats Desktop - Football Tracker

<div align="center">

![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.6-2C5E8A?style=for-the-badge&logo=java&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate_ORM-6.4.4-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14%2B_%2F_18-316192?style=for-the-badge&logo=postgresql&logoColor=white)


**Aplicación de escritorio moderna estilo OneFootball / FotMob para seguimiento de fútbol europeo en tiempo real.**

[Características](#-características-principales) •
[Rúbrica Académica](#-cumplimiento-de-rúbrica-académica) •
[Arquitectura](#-arquitectura-y-diseño) •
[Stack Tecnológico](#-stack-tecnológico-y-dependencias) •
[Instalación](#-requisitos-previos-e-instalación) •
[Estructura](#-estructura-del-proyecto) •
[Equipo](#-equipo-de-desarrollo-y-roles)

</div>

---

## 📖 Descripción General

**FutbolStats Desktop** es una plataforma de escritorio de alto rendimiento orientada a aficionados y analistas deportivos. Ofrece una experiencia visual fluida e intuitiva para consultar resultados, calendarios, alineaciones y clasificaciones de las tres competiciones más prestigiosas del mundo: **LaLiga EA Sports**, **Premier League** y **UEFA Champions League**.

### ¿Qué problema resuelve?
Muchas aplicaciones deportivas web o móviles están sobrecargadas de publicidad, requieren inicio de sesión forzoso o consumen recursos excesivos en segundo plano. **FutbolStats Desktop** soluciona esto proporcionando una herramienta nativa, ligera y privada que combina el consumo en vivo de la API oficial (**football-data.org v4**) con persistencia relacional local en **PostgreSQL**, garantizando acceso inmediato a datos históricos y favoritos incluso sin conexión activa a Internet gracias a su sistema de **caché resiliente**.

---



## 🚀 Características Principales

* 🔴 **Monitoreo de Partidos en Tiempo Real:** Visualización en directo del estado de los partidos (`TIMED`, `IN_PLAY`, `PAUSED`, `FINISHED`), marcadores actualizados al minuto y desglose de eventos (goles, tarjetas y asistencias).
* 📅 **Selector de Jornadas y Fechas:** Navegación por fechas y competiciones con conversión automática de husos horarios UTC a hora local.
* 🏆 **Tablas de Posiciones Dinámicas:** Clasificación ordenada en tiempo real aplicando reglas oficiales de desempate (puntos, diferencia de goles, goles a favor) con zonas visuales de clasificación europea y descenso.
* ⭐ **Gestión Local de Favoritos:** Marcado rápido de equipos y encuentros predilectos persistidos localmente mediante transacciones JPA en PostgreSQL.
* 🌙 **Interfaz Moderna con Modo Oscuro (Estilo FotMob):** Componentes visuales pulidos con JavaFX y Scene Builder, apoyados en hojas de estilo CSS modulares con paleta oscura (Dark Theme) que reduce la fatiga visual.
* 🛡️ **Resiliencia y Caché Offline:** Ante desconexiones de red o agotamiento de cuota de la API externa, el sistema entra en modo degradado controlado, sirviendo la última captura válida desde la base de datos sin congelar la interfaz.

---

## 🏛️ Arquitectura y Diseño

El sistema está estructurado bajo el patrón arquitectónico **MVC (Modelo-Vista-Controlador)** desacoplado con soporte de **DTOs (Data Transfer Objects)**:

```
┌─────────────────────────────────────────────────────────────┐
│                 VISTAS (JavaFX FXML + CSS)                  │
│       partidos.fxml  |  posiciones.fxml  | favoritos.fxml   │
└──────────────────────────────▲──────────────────────────────┘
                               │ Eventos UI / Binding
┌──────────────────────────────▼──────────────────────────────┐
│                        CONTROLADORES                        │
│   PartidosController | PosicionesController | FavoritosCtr │
└──────────────────────────────▲──────────────────────────────┘
                               │ Tareas Asíncronas (Task<T>)
┌──────────────────────────────▼──────────────────────────────┐
│                    SERVICIOS Y ADAPTADORES                  │
│  • FootballApiService (Java HttpClient / Gson JSON Parser)  │
│  • ServiciosReales (Coordinador Caché / Transacciones)      │
└──────────────────────────────▲──────────────────────────────┘
                               │ Inyección ORM
┌──────────────────────────────▼──────────────────────────────┐
│                CAPA DE PERSISTENCIA (DAO / JPA)             │
│  GenericDao<T, ID> | MatchDao | StandingDao | TeamDao       │
└──────────────────────────────▲──────────────────────────────┘
                               │ Hibernate 6 / JDBC
┌──────────────────────────────▼──────────────────────────────┐
│                  POSTGRESQL (football_db)                   │
└─────────────────────────────────────────────────────────────┘
```

> **Nota Técnica:** Para evitar excepciones del tipo `LazyInitializationException` y garantizar 60 FPS en la interfaz gráfica, los controladores consumen modelos inmutables desacoplados (`Partido`, `Posicion`, `Equipo`), mientras que las 8 entidades JPA completas son gestionadas exclusivamente dentro de sesiones de corta duración controladas por el `EntityManager`.

---

## 💻 Stack Tecnológico y Dependencias

| Componente | Tecnología | Versión | Rol en el Proyecto |
| :--- | :--- | :--- | :--- |
| **Lenguaje** | Java (OpenJDK) | **21 LTS** | Núcleo de la aplicación, Records, Pattern Matching y HTTP Client nativo. |
| **GUI** | JavaFX | **21.0.6** | Motor gráfico, renderizado FXML y controles interactivos. |
| **ORM** | Hibernate ORM | **6.4.4.Final** | Mapeo objeto-relacional (JPA 3.1) y ejecución de consultas HQL optimizadas. |
| **Base de Datos** | PostgreSQL | **14+ / 18** | Motor relacional local para persistencia transaccional y caché. |
| **Driver JDBC** | PostgreSQL JDBC | **42.7.3** | Conector oficial entre la JVM y la base de datos PostgreSQL. |
| **JSON Parser** | Google Gson | **2.11.0** | Deserialización de payloads REST desde football-data.org. |
| **Pruebas** | JUnit Jupiter | **5.10.2** | Pruebas de integración para persistencia y componentes visuales. |

### Configuración de Dependencias (`pom.xml`)

```xml
<dependencies>
    <!-- JavaFX Controls y FXML -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21.0.6</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21.0.6</version>
    </dependency>

    <!-- Hibernate 6 & JPA 3.1 -->
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>6.4.4.Final</version>
    </dependency>
    <dependency>
        <groupId>jakarta.persistence</groupId>
        <artifactId>jakarta.persistence-api</artifactId>
        <version>3.1.0</version>
    </dependency>

    <!-- Driver PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.3</version>
    </dependency>

    <!-- Parser JSON -->
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.11.0</version>
    </dependency>
</dependencies>
```

---

## ⚙️ Requisitos Previos e Instalación

### 1. Prerrequisitos del Sistema
* **Java Development Kit (JDK):** Versión 21 o superior ([Descargar Eclipse Temurin](https://adoptium.net/)).
* **PostgreSQL Server:** Versión 14 a 18 en ejecución en el puerto `5432`.
* **Git** instalado en el equipo.

---

### 2. Clonación del Repositorio
```bash
git clone https://github.com/Proexample12/Proyecto-Final-IV-Semestre.git
cd Proyecto-Final-IV-Semestre
```

---

### 3. Configuración de Base de Datos PostgreSQL

Abre tu terminal (PowerShell / Bash) o consola SQL conectada como superusuario (`postgres`):

```sql
-- 1. Crear la base de datos
CREATE DATABASE football_db;
```

Ejecuta el script inicial de esquema y datos semilla provisto en el proyecto:

```powershell
# En Windows (PowerShell):
psql -U postgres -h 127.0.0.1 -p 5432 -d football_db -f "database/schema.sql"
```
*(También puedes abrir y ejecutar el contenido de `database/schema.sql` desde **pgAdmin 4** o **DBeaver**).*

---

### 4. Configuración Segura de Credenciales

El proyecto aísla las credenciales del control de versiones. Copia la plantilla de configuración:

```powershell
Copy-Item config.example.properties config.local.properties
```

Edita `config.local.properties` (este archivo está ignorado por `.gitignore`):

```properties
FOOTBALL_API_KEY=tu_clave_football_data
API_FOOTBALL_KEY=tu_clave_api_football
HIGHLIGHTLY_API_KEY=tu_clave_highlightly
DB_URL=jdbc:postgresql://127.0.0.1:5432/football_db
DB_USER=postgres
DB_PASSWORD=tu_contraseña
```

---

### 5. Compilación y Ejecución

#### Opción A: Usando Maven Wrapper (Consola)
```powershell
# Compilar y arrancar la aplicación JavaFX
.\mvnw.cmd clean javafx:run
```

#### Opción B: Desde IntelliJ IDEA / Eclipse
1. Abre la carpeta del proyecto en el IDE.
2. Configura el **Project SDK** en `Java 21`.
3. Permite que Maven sincronice las dependencias del `pom.xml`.
4. Ejecuta la clase principal:  
   `ni.edu.uam.proyectofinalivsemestre.launcher.Launcher`

#### Opción C: Ejecutar Pruebas Automatizadas
```powershell
.\mvnw.cmd test
```

---

## 📂 Estructura del Proyecto

```text
Proyecto-Final-IV-Semestre/
├── .gitignore
├── config.example.properties               # Plantilla para variables de entorno locales
├── pom.xml                                 # Descriptor de dependencias Maven
├── database/
│   ├── schema.sql                          # DDL de PostgreSQL y datos de prueba
│   └── README.md                           # Instrucciones específicas de BD
├── docs/
│   └── ui/                                 # Especificación de diseño y mockups
│       ├── DISENO_VISTAS.md
│       ├── EJECUCION_REAL.md
│       └── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── module-info.java            # Configuración modular JPMS (JavaFX/JPA)
    │   │   └── ni/edu/uam/proyectofinalivsemestre/
    │   │       ├── config/                 # HibernateUtil (Singleton) y LocalConfig
    │   │       ├── controller/             # Controladores MVC (Main, Partidos, Posiciones, Favoritos)
    │   │       ├── dao/                    # GenericDao<T, ID> y DAOs especializados (HQL)
    │   │       ├── launcher/               # Clases de arranque (Launcher, MainApp)
    │   │       ├── model/                  # 8 Entidades ORM (Match, Team, etc.) y DTOs
    │   │       ├── service/                # FootballApiService, interfaces y adaptadores
    │   │       └── util/                   # UiUtil (gestión asíncrona no bloqueante)
    │   └── resources/
    │       ├── css/
    │       │   └── styles.css              # Hoja de estilos Modo Oscuro (Dark Theme)
    │       ├── META-INF/
    │       │   └── persistence.xml         # Configuración JPA (football_pu)
    │       └── views/
    │           ├── main.fxml               # Contenedor principal con barra de navegación
    │           ├── partidos.fxml           # Pantalla de resultados y partidos en vivo
    │           ├── posiciones.fxml         # Tabla de clasificación dinámica
    │           └── favoritos.fxml          # Panel de equipos y partidos guardados
    └── test/java/                          # Pruebas unitarias y de integración
```

---

## 👥 Equipo de Desarrollo y Roles

| Integrante | Rol Principal | Responsabilidades Clave |
| :--- | :--- | :--- |
| **Donald Jahdiel** (`JayWFx`) | **Líder de Base de Datos y Persistencia ORM** | Modelado relacional y mapeo JPA de las 8 entidades del dominio. Implementación del `GenericDao` transaccional con Hibernate 6, configuración de `persistence.xml`, creación de consultas complejas HQL con optimización `JOIN FETCH` (anti N+1) y pruebas ejecutivas en `HibernateRunnerTest`. |
| **Kenneth Zamora** (`Kenneth Zamora`) | **Líder de Interfaz Gráfica (UI/UX) e Integración** | Diseño e implementación de las vistas en JavaFX (FXML y estilos Dark Mode en CSS). Coordinación de controladores MVC, asincronía con hilos secundarios (`UiUtil.async`), lógica de integración en `ServiciosReales`, manejo de errores visuales y suite de pruebas unitarias. |
| **Rommel** (`Rommel110207`) | **Especialista de Integración API y Lógica** | Implementación del conector HTTP `FootballApiService` contra la API v4 de football-data.org, parsing seguro de estructuras JSON complejas mediante Gson y DTOs, manejo de cuotas de red y modularidad Java en `module-info.java`. |
| **Alfredo Jarquin** | **Desarrollador de Reportes** | Desarrollo de los reportes de clasificación, goles y rendimiento, incluyendo sus vistas, controladores, plantillas y exportación a PDF. |


---

## 📄 Licencia y Créditos

Este proyecto fue desarrollado con fines educativos para la asignatura de **Programacion de Aplicaciones de Escritorio (PAE)**.

* **Fuente de Datos:** [football-data.org](https://www.football-data.org/) (RESTful API).
* **Licencia:** Distribuido bajo la Licencia **MIT**. Consulta el archivo `LICENSE` para más detalles.


Consulta [las mejoras de UI, pruebas reales y restricciones de API-Football](docs/ui/MEJORAS_IMPLEMENTADAS.md).

El detalle actual usa [Highlightly: configuración, alcance y pruebas](docs/ui/HIGHLIGHTLY.md).
