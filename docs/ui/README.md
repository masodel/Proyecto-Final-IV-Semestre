# Interfaz de Fútbol App

**Actualización:** la aplicación ya usa API y PostgreSQL por defecto. Consulta
[EJECUCION_REAL.md](EJECUCION_REAL.md) para configuración, ejecución y pruebas.
El resto de este documento describe la entrega inicial de UI con demo y se conserva
como registro; sus instrucciones de integración pendiente ya fueron implementadas.

Implementación del Integrante 3 en `feature/ui-vistas`. Conserva Maven, Java 21,
JavaFX 21.0.6 y el módulo/paquete base `ni.edu.uam.proyectofinalivsemestre`.

## Ejecutar

Con un JDK 21 configurado en `JAVA_HOME`, desde la raíz:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd javafx:run
```

En IntelliJ: seleccionar JDK 21, recargar Maven y ejecutar
`ni.edu.uam.proyectofinalivsemestre.launcher.Launcher`.
`MainApp` y `Launcher` están en el paquete `launcher`; se retiró la pantalla Hello de la plantilla.

No hace falta PostgreSQL ni acceso a una API para abrir esta interfaz.
La fecha inicial es **10/11/2026**, fecha de los mockups. Otros días no tienen datos de demostración.
La temporada 2026/27 contiene datos; 2025/26 permite probar el estado vacío.
LaLiga reproduce las ocho filas del diseño. Las otras competiciones usan cifras ficticias
adicionales únicamente para probar el selector. Los favoritos se reinician al cerrar la app.

## Simular errores

```powershell
.\mvnw.cmd "-Ddemo.fallar=true" javafx:run
```

En IntelliJ se puede usar la opción VM `-Dfutbol.demo.fallar=true`.
También se puede cambiar `DatosDemo.FALLAR_SINCRONIZACION` durante una prueba.
Al pulsar Actualizar, la demo espera 650 ms y lanza una excepción: aparece una alerta
y se vuelven a consultar los datos guardados. Aplica a partidos, posiciones y detalle.
Sin la bandera, Actualizar simula una sincronización correcta sin modificar los resultados ficticios.

## Referencias y decisiones

- El repositorio original no contenía `docs/ui`. Se incorporó el documento entregado
  y se extrajeron sus tres bloques HTML a `mockups/` como referencia; la app utiliza FXML.
- No se encontraron los archivos de `controllers_base/`, ni se recibieron durante la implementación.
  Por ello los cuatro controllers y `UiUtil` son nuevos; **no son copias verificadas**
  de los controllers mencionados en la solicitud.
- Se conservaron los nombres del contrato, adaptando el paquete al del proyecto.
- Todos los accesos a servicios, incluidos lectura y alternancia de favoritos, usan
  `UiUtil.async` con `Task`. Los resultados y alertas se aplican en el hilo JavaFX.
- Se bloquean los filtros durante cada petición; en Favoritos un número de solicitud
  impide que una respuesta antigua sustituya el detalle abierto después.
- El pie dice “Caché local · demostración / Datos en memoria”: no afirma que PostgreSQL esté conectado.
- Se añadieron tres labels para los valores visitantes de estadísticas, además de los IDs requeridos,
  para situar los valores a ambos lados de las barras como en el mockup.
- Doble clic o Intro sobre un partido abre el detalle; doble clic o Intro sobre una
  posición alterna el equipo favorito. En la pestaña Equipos, doble clic, Intro o Supr
  quita el equipo de favoritos. Las estrellas y el botón del detalle alternan partidos.
- Filtros y calendario tienen nombres accesibles; botones de 44 px, foco visible,
  tabla de ancho restringido y distribución mediante contenedores redimensionables.

## Integración posterior

El proyecto ya tiene entidades con nombres ingleses (`Team`, `Match`, `Standing`,
etc.) y servicios de API. No se cambiaron esas clases, DAOs, HQL, configuración
de Hibernate ni cliente API.

Las clases nuevas `Equipo`, `Liga`, `Partido`, `Posicion` y `PartidoDetalle`
son contratos temporales sin JPA. El equipo debe acordar si reemplazarlos por sus
versiones finales o conservarlos como modelos de lectura y mapear las entidades
existentes desde los servicios; no crear entidades JPA duplicadas.

Implementar `PartidoService`, `PosicionService` y `FavoritoService`, y sustituir
las tres instancias de `Servicios.java`. Mantener las firmas, la semántica de
`liga == null`, los estados y los IDs estables. Los servicios reales deben entregar
los datos necesarios ya materializados para que los getters usados por la UI no
disparen cargas lazy de Hibernate en el hilo JavaFX. La sincronización debe lanzar
sus errores y conservar la caché anterior si falla. Sustituir después los textos
“datos de ejemplo” y el estado del pie por información real.

## Validación

`UiIntegrationTest` carga FXML real y comprueba navegación, seis partidos iniciales,
filtros por liga y fecha, estado vacío, carga asíncrona, alertas y fallback de las
tres pantallas, doble clic al detalle, marcador/posesión, favoritos de partidos y
equipos, estrella, filas Champions y cambio de tamaño. Necesita una sesión gráfica.
Se habilitó Surefire 3.2.5 para ejecutar el JUnit 5 que ya estaba en el proyecto.
La ejecución de pruebas en classpath puede emitir un aviso de JavaFX sobre el
módulo sin nombre; el arranque normal por Maven utiliza el módulo del proyecto.

## Archivos de la entrega

- `src/main/resources/views/{main,partidos,posiciones,favoritos}.fxml`
- `src/main/resources/css/styles.css`
- En `src/main/java/ni/edu/uam/proyectofinalivsemestre/`:
  - `controller/{MainController,PartidosController,PosicionesController,FavoritosController}.java`
  - `util/UiUtil.java`
  - `launcher/{MainApp,Launcher}.java`
  - `model/{Equipo,Liga,Partido,Posicion}.java`, `model/dto/PartidoDetalle.java`
  - `service/{PartidoService,PosicionService,FavoritoService,Servicios}.java`
  - `service/stub/{DatosDemo,PartidoServiceDemo,PosicionServiceDemo,FavoritoServiceDemo}.java`
- `src/test/java/ni/edu/uam/proyectofinalivsemestre/ui/UiIntegrationTest.java`
- `pom.xml`, `src/main/java/module-info.java`
- `docs/ui/DISENO_VISTAS.md`, `docs/ui/mockups/{partidos,posiciones,favoritos}.html`, este README.
- Retirados: `HelloApplication.java`, `HelloController.java`, `hello-view.fxml`;
  `Launcher.java` trasladado desde el paquete raíz a `launcher`.

No se realizó push ni se abrió un Pull Request.
