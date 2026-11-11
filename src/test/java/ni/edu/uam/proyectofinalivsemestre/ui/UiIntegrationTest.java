package ni.edu.uam.proyectofinalivsemestre.ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.*;
import ni.edu.uam.proyectofinalivsemestre.model.*;
import ni.edu.uam.proyectofinalivsemestre.service.Servicios;
import ni.edu.uam.proyectofinalivsemestre.service.stub.DatosDemo;
import ni.edu.uam.proyectofinalivsemestre.util.UiUtil;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

/** Prueba de integración local: necesita una sesión gráfica, no BD ni API. */
class UiIntegrationTest {
    private static Stage stage;
    private static Parent root;
    private static final List<Throwable> errores = new CopyOnWriteArrayList<>();

    @BeforeAll static void iniciar() throws Exception {
        CountDownLatch iniciado = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); iniciado.countDown(); });
        assertTrue(iniciado.await(10, TimeUnit.SECONDS));
        fx(() -> {
            Thread.currentThread().setUncaughtExceptionHandler((t,e) -> errores.add(e));
            root = FXMLLoader.load(UiIntegrationTest.class.getResource("/views/main.fxml"));
            Scene scene = new Scene(root, 1100, 680);
            scene.getStylesheets().add(UiIntegrationTest.class.getResource("/css/styles.css").toExternalForm());
            stage = new Stage(); stage.setScene(scene); stage.show();
            return null;
        });
    }
    @AfterAll static void cerrar() throws Exception {
        DatosDemo.FALLAR_SINCRONIZACION = false;
        fx(() -> { for (Window w : List.copyOf(Window.getWindows())) w.hide(); return null; });
        UiUtil.cerrar(); Platform.exit();
    }
    private static <T> T fx(Callable<T> accion) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea); return tarea.get(10, TimeUnit.SECONDS);
    }
    @SuppressWarnings("unchecked") private static <T extends Node> T control(String id) {
        Node n = root.lookup("#" + id); assertNotNull(n, id); return (T) n;
    }
    private static void esperar(BooleanSupplier condicion) throws Exception {
        long fin = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
        while (!fx(condicion::getAsBoolean) && System.nanoTime() < fin) Thread.sleep(25);
        assertTrue(fx(condicion::getAsBoolean), "La operación asíncrona no terminó");
    }
    private static void listo() throws Exception {
        esperar(() -> !UiIntegrationTest.<ProgressIndicator>control("piCarga").isVisible());
    }
    private static void pulsar(String id) throws Exception {
        fx(() -> { UiIntegrationTest.<ButtonBase>control(id).fire(); return null; });
    }
    private static void cerrarAlertas() throws Exception {
        fx(() -> { for (Window w : List.copyOf(Window.getWindows())) if (w != stage) w.hide(); return null; });
    }
    private static void dobleClic(TableRow<?> fila) {
        fila.getOnMouseClicked().handle(new MouseEvent(MouseEvent.MOUSE_CLICKED,
                0,0,0,0, MouseButton.PRIMARY,2,false,false,false,false,
                true,false,false,false,false,false,null));
    }
    @Test void recorridoCompletoConCacheYFavoritos() throws Exception {
        listo();
        assertEquals(6, fx(() -> UiIntegrationTest.<TableView<?>>control("tblPartidos").getItems().size()));
        assertEquals(2, Servicios.favoritos().idsPartidosFavoritos().size());
        fx(() -> {
            UiIntegrationTest.<ComboBox<String>>control("cmbLiga").setValue("LaLiga"); return null;
        });
        listo();
        assertEquals(2, fx(() -> UiIntegrationTest.<TableView<?>>control("tblPartidos").getItems().size()));
        fx(() -> { UiIntegrationTest.<DatePicker>control("dpFecha").setValue(LocalDate.of(2026,11,11)); return null; });
        listo();
        assertEquals(0, fx(() -> UiIntegrationTest.<TableView<?>>control("tblPartidos").getItems().size()));
        fx(() -> { UiIntegrationTest.<DatePicker>control("dpFecha").setValue(DatosDemo.FECHA); return null; });
        listo();

        DatosDemo.FALLAR_SINCRONIZACION = true;
        pulsar("btnActualizar");
        assertTrue(fx(() -> UiIntegrationTest.<ProgressIndicator>control("piCarga").isVisible()));
        // Un pulso responde mientras sincronizar duerme en segundo plano.
        assertEquals("hilo activo", fx(() -> "hilo activo"));
        listo();
        assertEquals(2, fx(() -> UiIntegrationTest.<TableView<?>>control("tblPartidos").getItems().size()));
        assertTrue(fx(() -> Window.getWindows().size() > 1), "Debe aparecer una alerta");
        cerrarAlertas(); DatosDemo.FALLAR_SINCRONIZACION = false;

        fx(() -> {
            TableView<Partido> tabla = control("tblPartidos"); tabla.applyCss(); tabla.layout();
            TableRow<?> fila = tabla.lookupAll(".table-row-cell").stream()
                    .filter(n -> n instanceof TableRow<?> r && r.getItem() instanceof Partido partido && partido.getId().equals(2L))
                    .map(n -> (TableRow<?>)n).findFirst().orElseThrow();
            dobleClic(fila); return null;
        });
        listo();
        assertEquals("2 - 1", fx(() -> UiIntegrationTest.<Label>control("lblMarcador").getText()));
        assertEquals(0.54, fx(() -> UiIntegrationTest.<ProgressBar>control("pbPosesion").getProgress()), 0.001);
        pulsar("btnFavorito"); listo();
        assertFalse(Servicios.favoritos().idsPartidosFavoritos().contains(2L));
        assertEquals("Agregar a favoritos", fx(() -> UiIntegrationTest.<Button>control("btnFavorito").getText()));
        pulsar("btnFavorito"); listo();
        assertTrue(Servicios.favoritos().idsPartidosFavoritos().contains(2L));
        DatosDemo.FALLAR_SINCRONIZACION = true;
        pulsar("btnActualizarDetalle"); listo();
        assertEquals("2 - 1", fx(() -> UiIntegrationTest.<Label>control("lblMarcador").getText()));
        cerrarAlertas(); DatosDemo.FALLAR_SINCRONIZACION = false;

        pulsar("btnPosiciones"); listo();
        assertEquals(8, fx(() -> UiIntegrationTest.<TableView<?>>control("tblPosiciones").getItems().size()));
        fx(() -> {
            TableView<Posicion> tabla = control("tblPosiciones"); tabla.applyCss(); tabla.layout();
            TableRow<?> fila = tabla.lookupAll(".table-row-cell").stream()
                    .filter(n -> n instanceof TableRow<?> r && r.getItem() instanceof Posicion pos && pos.getPosicion() == 2)
                    .map(n -> (TableRow<?>) n).findFirst().orElseThrow();
            assertTrue(fila.getStyleClass().contains("zona-champions")); dobleClic(fila); return null;
        });
        listo();
        assertEquals(2, Servicios.favoritos().equiposFavoritos().size());
        DatosDemo.FALLAR_SINCRONIZACION = true;
        pulsar("btnActualizar"); listo();
        assertEquals(8, fx(() -> UiIntegrationTest.<TableView<?>>control("tblPosiciones").getItems().size()));
        cerrarAlertas(); DatosDemo.FALLAR_SINCRONIZACION = false;
        pulsar("btnFavoritos"); listo(); pulsar("btnTipoEquipos"); listo();
        assertEquals(2, fx(() -> UiIntegrationTest.<ListView<?>>control("lstFavoritos").getItems().size()));
        fx(() -> {
            stage.setWidth(1400); stage.setHeight(850); root.applyCss(); root.layout();
            assertTrue(control("lstFavoritos").getBoundsInParent().getHeight() > 0);
            stage.setWidth(1100); stage.setHeight(680); return null;
        });
        pulsar("btnPartidos"); listo();
        fx(() -> {
            root.applyCss(); root.layout();
            Button estrella = (Button) root.lookupAll(".btn-estrella").stream()
                    .filter(n -> n instanceof Button button && button.getAccessibleText().equals("Marcar favorito: Arsenal"))
                    .findFirst().orElseThrow();
            assertTrue(estrella.getHeight() >= 44); estrella.fire(); return null;
        });
        listo();
        assertTrue(Servicios.favoritos().idsPartidosFavoritos().contains(3L));
        assertTrue(errores.isEmpty(), () -> "Errores del hilo JavaFX: " + errores);
    }
}
