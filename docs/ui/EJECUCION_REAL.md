# Ejecutar Fútbol App con datos reales

La aplicación usa **football-data.org y PostgreSQL por defecto**. El paquete de arranque es
`ni.edu.uam.proyectofinalivsemestre.launcher`; se conservan Java 21 y JavaFX 21.0.6.

## Configuración local

Copiar `config.example.properties` a `config.local.properties` en la raíz del proyecto
y completar `FOOTBALL_API_KEY`, `DB_URL`, `DB_USER` y `DB_PASSWORD`.
El archivo local está excluido de Git y fuera de resources: no se incluye en el JAR.
En este equipo ya está configurado; no copiar sus valores a commits ni capturas.

También se aceptan variables de entorno con esos nombres. El orden de prioridad es:
entorno, propiedades JVM, archivo local. Para ejecutar desde otro directorio,
usar `-Dfutbol.config=<archivo>` en las opciones VM de IntelliJ.

PostgreSQL debe estar iniciado y la base `football_db` debe existir.
Hibernate conserva el modo `update` del proyecto y añade las tablas de caché y favoritos
de partidos sin recrear la base.

## Arranque

Con `JAVA_HOME` apuntando a JDK 21, desde la raíz:

```powershell
.\mvnw.cmd javafx:run
```

En IntelliJ, recargar Maven y ejecutar
`ni.edu.uam.proyectofinalivsemestre.launcher.Launcher` con el directorio de trabajo
en la raíz del proyecto.

Al abrir Partidos se consultan LaLiga, Premier League y Champions League para la
temporada correspondiente al día actual. Las respuestas se guardan por ID oficial
en una transacción Hibernate y la tabla lee desde PostgreSQL. Las horas se convierten
de UTC a la zona horaria del equipo.

El filtro inicial es **hoy**: si no hay encuentros de esas competiciones ese día,
la lista está vacía. Cambiar la fecha permite ver otros partidos ya guardados;
Actualizar sincroniza la temporada de la fecha elegida. No se muestran los partidos
ficticios que pudiera contener el script de ejemplo del proyecto.

Posiciones sincroniza al abrir y al cambiar liga/temporada. Actualizar detalle consulta
el endpoint del partido y guarda el resultado. Los campos que no entrega la API se
muestran como no disponibles. No se inventan posesión, tiros, minutos ni eventos.

Las sincronizaciones ocurren en segundo plano, al abrir las pantallas y al pulsar
Actualizar; no hay sondeo periódico. La disponibilidad y actualización de los datos
dependen del proveedor y del plan de la cuenta.

## Caché y favoritos

Si falla la API o se alcanza su límite, aparece una alerta y se conservan los datos
guardados. No se reemplazan por datos de demostración. Si PostgreSQL no está disponible,
se informa del error; no existe una segunda base de datos oculta en memoria.

- `Match`, `Team` y `Competition`: se reutilizan las entidades existentes.
- `Standing`: contiene la clasificación de la temporada actual.
- `ApiSnapshot`: conserva respuestas de detalle y clasificaciones por temporada.
- `Favorite` y `Team.isFavorite`: favoritos de equipos.
- `MatchFavorite`: favoritos de partidos, persistentes entre ejecuciones.
- `ServiciosReales`: coordina API, transacciones JPA y conversión a modelos de lectura.
- `Servicios`: selecciona la integración real o la demo explícita.

Los modelos `Equipo`, `Liga`, `Partido`, `Posicion` y el DTO
`PartidoDetalle` funcionan como modelos de lectura de UI; no duplican entidades JPA.
Los getters usados por JavaFX no hacen consultas lazy.

## Pruebas

```powershell
.\mvnw.cmd clean test
```

La prueba de UI usa demo y necesita sesión gráfica. No consume API ni escribe en BD.
La integración real es optativa y sí actualiza la caché local:

```powershell
.\mvnw.cmd "-Dtest=RealIntegrationTest" "-Dfutbol.test.real=true" test
```

Comprueba sincronización, clasificación, detalle, persistencia de favoritos al
reabrir Hibernate y conservación de datos ante un fallo de red simulado.
Restaura el estado del favorito que alterna durante la prueba.

Para la demo sin servicios externos:

```powershell
.\mvnw.cmd "-Ddemo=true" javafx:run
.\mvnw.cmd "-Ddemo=true" "-Ddemo.fallar=true" javafx:run
```

La demo conserva la fecha 10/11/2026 del diseño. Sus favoritos no son persistentes.

## Alcance de esta integración

La primera entrega era solo UI y demo. Con la autorización posterior se ajustaron
`FootballApiService` para propagar errores y ofrecer respuestas completas,
`HibernateUtil` para leer credenciales locales y reintentar conexiones,
los controllers para usar datos reales, y se añadieron el adaptador y las dos
entidades auxiliares. No se modificaron las entidades originales ni los DAOs existentes.

Los controllers de referencia no estaban disponibles en la entrega inicial;
los controllers implementados son nuevos. Las vistas y el CSS siguen en
`src/main/resources/views/` y `src/main/resources/css/`.
Los mockups y `DISENO_VISTAS.md` son la referencia visual histórica.

Documentación del proveedor utilizada:
[Partidos](https://docs.football-data.org/general/v4/match.html) y
[Competiciones y clasificación](https://docs.football-data.org/general/v4/competition.html).

Se trabaja en `feature/ui-vistas`. No se realiza push ni se abre un Pull Request.
