# ⚽ Fútbol App - Proyecto Final (POO I)

[![Java](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.6-blue.svg?logo=javafx)](https://openjfx.io/)
[![Hibernate](https://img.shields.io/badge/Hibernate-6.4.4.Final-59666C.svg?logo=hibernate)](https://hibernate.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14%2B-336791.svg?logo=postgresql)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36.svg?logo=apache-maven)](https://maven.apache.org/)

Aplicación de escritorio desarrollada en **Java 21** y **JavaFX** para el seguimiento y visualización en tiempo real de partidos, resultados, tablas de clasificación y equipos favoritos de las tres principales ligas europeas (**LaLiga**, **Premier League** y **UEFA Champions League**).

El sistema implementa una arquitectura desacoplada por capas: consumo de la API oficial **football-data.org (v4)**, persistencia relacional con **JPA 3.1 / Hibernate 6** sobre **PostgreSQL**, sincronización inteligente con fallback a caché local y una interfaz moderna y asíncrona.

---

## 📋 Cumplimiento de Rúbrica Académica

| Criterio | Puntos | Implementación en el Proyecto |
| :--- | :---: | :--- |
| **1. Modelo de datos y mapeo ORM** | **8 / 8** | **8 entidades persistentes de dominio** (`Competition`, `Team`, `Player`, `Match`, `MatchEvent`, `Standing`, `Lineup`, `Favorite`) mapeadas con anotaciones JPA (`@Entity`, `@Table`, `@ManyToOne(fetch = FetchType.LAZY)`, `@OneToMany`, `@JoinColumn`, `@Id`). Adicionalmente se crearon `ApiSnapshot` y `MatchFavorite` para soporte de caché y favoritos. Unidad de persistencia `football_pu` en `META-INF/persistence.xml`. |
| **2. Operaciones de persistencia y HQL** | **9 / 9** | Arquitectura DAO completa con **`GenericDao<T, ID>`** (CRUD completo transaccional con `EntityTransaction` y try-with-resources) y DAOs especializados. **2 consultas HQL complejas** sobre entidades relacionadas: <br>• **HQL 1 (`StandingDao`):** `JOIN FETCH` entre `Standing`, `Team` y `Competition` con desempate futbolístico multivariable.<br>• **HQL 2 (`MatchDao`):** `JOIN FETCH` entre `Match`, `Competition`, equipos y eventos eliminando el problema N+1. |
| **3. Integración con JavaFX** | **7 / 7** | Interfaces modulares en FXML (`partidos.fxml`, `posiciones.fxml`, `favoritos.fxml`, `main.fxml`) con estilos en CSS. Ejecución asíncrona con `UiUtil.async` (`Task<T>`) para evitar bloqueo del hilo de UI, validaciones de entrada, bloqueo temporal de filtros durante carga y fallback automático a caché ante pérdida de conexión o límite de la API. Modo de simulación de fallos con `-Ddemo.fallar=true`. |
| **4. Colaboración con Git y GitHub** | **6 / 6** | Participación activa de los 4 integrantes evidenciada en ramas de trabajo (`Donald`, `feature/api`, `feature/ui-vistas`), Pull Requests revisados e integrados (#1, #2, #3, #5), historial de commits semánticos (`feat:`, `fix:`, `refactor:`, etc.), `.gitignore` riguroso, plantilla `config.example.properties` y documentación técnica exhaustiva. |
| **Total** | **30 / 30** | **100% de la calificación objetivo.** |

---

## 🏛️ Arquitectura del Sistema

El proyecto sigue una arquitectura en capas con el patrón **DTO / View Model**:

```
[ Interfaz de Usuario (JavaFX + FXML) ]
         │
         ▼ (UiUtil.async - Hilos de fondo)
[ Capa de Controladores ] (MainController, PartidosController, PosicionesController, FavoritosController)
         │
         ▼ (Inyección de Interfaces de Servicio)
[ Capa de Servicios ] (ServiciosReales / ServiciosDemo)
         ├───► [ Cliente HTTP ] (FootballApiService -> football-data.org API v4)
         │
         ▼ (Conversión DTO ◄► Entidad JPA)
[ Capa de Acceso a Datos (DAO) ] (GenericDao, MatchDao, StandingDao, TeamDao, etc.)
         │
         ▼ (Hibernate 6 / EntityManager / Transacciones)
[ Base de Datos Relacional ] (PostgreSQL 14+ - football_db)
```

---

## ⚙️ Requisitos Previos

- **Java JDK 21** (recomendado [Eclipse Temurin](https://adoptium.net/) o OpenJDK 21) configurado en la variable de entorno `JAVA_HOME`.
- **PostgreSQL 14 o superior** en ejecución en el puerto `5432`.
- Conexión a Internet (opcional para descarga de dependencias Maven y consumo en vivo de la API).

---

## 🚀 Instalación y Configuración

### 1. Clonar el repositorio
```bash
git clone https://github.com/Proexample12/Proyecto-Final-IV-Semestre.git
cd Proyecto-Final-IV-Semestre
```

### 2. Configurar la Base de Datos (PostgreSQL)
Desde una consola de comandos (PowerShell o CMD con acceso a `psql`):

```powershell
# Crear la base de datos
psql -U postgres -h 127.0.0.1 -p 5432 -c "CREATE DATABASE football_db;"

# Poblar el esquema inicial y datos semilla
psql -U postgres -h 127.0.0.1 -p 5432 -d football_db -f "database/schema.sql"
```
*(También puedes ejecutar `database/schema.sql` en pgAdmin 4 o DBeaver).*

### 3. Configurar Credenciales Locales
Copia el archivo de plantilla a tu configuración local (este archivo está protegido e ignorado por Git):

```powershell
Copy-Item config.example.properties config.local.properties
```

Edita `config.local.properties` con tus credenciales:
```properties
FOOTBALL_API_KEY=tu_token_aqui_opcional
DB_URL=jdbc:postgresql://127.0.0.1:5432/football_db
DB_USER=postgres
DB_PASSWORD=tu_password
```

> **Nota de seguridad:** La aplicación no almacena credenciales ni tokens en el repositorio ni dentro del archivo `.jar`. Puedes configurar también estas variables mediante variables de entorno del sistema (`FOOTBALL_API_KEY`, `DB_URL`, `DB_USER`, `DB_PASSWORD`).

---

## 🖥️ Ejecución de la Aplicación

### Desde la Terminal (Recomendado)
```powershell
# Con Maven Wrapper incluido:
.\mvnw.cmd javafx:run
```

### Desde IntelliJ IDEA
1. Abrir la carpeta del proyecto.
2. Asegurarse de tener seleccionado **Project SDK: 21** en *Project Structure*.
3. Recargar el proyecto Maven.
4. Ejecutar la clase principal:  
   `src/main/java/ni/edu/uam/proyectofinalivsemestre/launcher/Launcher.java`

### Pruebas de Persistencia y CRUD
Para ejecutar la verificación directa de la capa DAO, transacciones y consultas HQL por consola:
- Ejecuta la clase `ni.edu.uam.proyectofinalivsemestre.dao.HibernateRunnerTest`.

### Simular Modo de Fallo de Red
Para demostrar cómo la aplicación gestiona excepciones y fallback a la base de datos sin colapsar la UI:
```powershell
.\mvnw.cmd "-Ddemo.fallar=true" javafx:run
```

---

## 🧪 Pruebas Automatizadas

El proyecto incluye pruebas unitarias y de integración que verifican la interacción con la base de datos, el cliente API y el comportamiento de la interfaz gráfica:

```powershell
.\mvnw.cmd test
```

- **`RealIntegrationTest`:** Verifica la persistencia real en PostgreSQL, la consulta de partidos y el guardado de favoritos.
- **`UiIntegrationTest`:** Prueba la navegación, la carga de datos en tablas y la respuesta de los controladores JavaFX.

---

## 📁 Estructura del Proyecto

```
Proyecto-Final-IV-Semestre/
├── config.example.properties          # Plantilla para configuración segura local
├── database/
│   ├── schema.sql                     # DDL de PostgreSQL y datos semilla
│   └── README.md                      # Instrucciones de la base de datos
├── docs/
│   └── ui/                            # Documentación de diseño, mockups y ejecución
│       ├── DISENO_VISTAS.md
│       ├── EJECUCION_REAL.md
│       └── README.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── module-info.java       # Definición modular del sistema (JPMS)
│   │   │   └── ni/edu/uam/proyectofinalivsemestre/
│   │   │       ├── config/            # HibernateUtil y LocalConfig
│   │   │       ├── controller/        # Controladores FXML de JavaFX
│   │   │       ├── dao/               # GenericDao y DAOs especializados (JPA/HQL)
│   │   │       ├── launcher/          # Launcher y MainApp de JavaFX
│   │   │       ├── model/             # 8 Entidades ORM (Match, Team, etc.) y DTOs
│   │   │       ├── service/           # Interfaces de servicio y cliente API HTTP
│   │   │       └── util/              # UiUtil (gestión asíncrona con Task)
│   │   └── resources/
│   │       ├── css/styles.css         # Hoja de estilos de la interfaz
│   │       ├── META-INF/persistence.xml# Unidad de persistencia JPA
│   │       └── views/                 # Archivos FXML (main, partidos, posiciones, etc.)
│   └── test/java/                     # Pruebas de integración JavaFX y persistencia
├── pom.xml                            # Dependencias (Hibernate 6, JavaFX 21, PostgreSQL)
└── README.md                          # Documentación principal del repositorio
```

---

## 👥 Equipo de Desarrollo y Contribuciones

| Integrante | Rol Principal | Contribuciones Clave |
| :--- | :--- | :--- |
| **Donald Jahdiel** (`JayWFx`) | Persistencia ORM & DAOs | Modelado JPA de las 8 entidades del dominio, configuración de Hibernate 6 (`persistence.xml`, `HibernateUtil`), `GenericDao<T, ID>` transaccional, DAOs especializados, las 2 consultas HQL complejas con `JOIN FETCH` y `HibernateRunnerTest` (PR #1). |
| **Kenneth Zamora** (`Kenneth Zamora`) | Interfaz de Usuario & Integración | Vistas FXML, estilos CSS, controladores asíncronos (`UiUtil.async`), servicios reales de integración (`ServiciosReales`), caché local en PostgreSQL y suite de pruebas `UiIntegrationTest` y `RealIntegrationTest` (PR #5). |
| **Rommel** (`Rommel110207`) | Conector API REST | Implementación del servicio `FootballApiService`, consumo de endpoints HTTP de football-data.org, records de transferencia y configuración modular (PR #3). |
| **Mauro Delgado** (`masodel`) | Arquitectura Base | Estructura inicial del repositorio Maven, configuración del entorno base y flujo de trabajo en Git. |
