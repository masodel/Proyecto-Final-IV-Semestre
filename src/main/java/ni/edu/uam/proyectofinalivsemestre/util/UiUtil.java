package ni.edu.uam.proyectofinalivsemestre.util;

import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import ni.edu.uam.proyectofinalivsemestre.model.Partido;
import java.util.concurrent.*;
import java.util.function.Consumer;

public final class UiUtil {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "futbol-ui-worker"); t.setDaemon(true); return t;
    });
    private UiUtil() {}
    public static <T> void async(Callable<T> trabajo, Consumer<T> exito, Consumer<Throwable> error) {
        Task<T> task = new Task<>() { protected T call() throws Exception { return trabajo.call(); } };
        task.setOnSucceeded(e -> exito.accept(task.getValue()));
        task.setOnFailed(e -> error.accept(task.getException()));
        EXECUTOR.execute(task);
    }
    public static void ocupado(boolean valor, ProgressIndicator indicador, Node... controles) {
        indicador.setVisible(valor); indicador.setManaged(valor);
        for (Node control : controles) control.setDisable(valor);
    }
    public static void error(Throwable error) {
        alerta(Alert.AlertType.ERROR, "No se pudo completar la operación",
                error.getMessage() == null ? "Inténtalo nuevamente." : error.getMessage());
    }
    public static void aviso(String mensaje) { alerta(Alert.AlertType.WARNING, "Datos guardados", mensaje); }
    private static void alerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert a = new Alert(tipo, mensaje, ButtonType.OK);
        a.setTitle("Fútbol App"); a.setHeaderText(titulo); a.show();
    }
    public static String marcador(Partido p) {
        return p.getGolesLocal() == null || p.getGolesVisitante() == null ? "—" : p.getGolesLocal() + " - " + p.getGolesVisitante();
    }
    public static String estado(Partido p) {
        return switch (p.getEstado()) {
            case "EN_VIVO" -> "En vivo" + (p.getMinuto() == null ? "" : " " + p.getMinuto() + "'");
            case "FINALIZADO" -> "Finalizado";
            case "APLAZADO" -> "Aplazado";
            case "SUSPENDIDO" -> "Suspendido";
            case "CANCELADO" -> "Cancelado";
            default -> "Programado";
        };
    }
    public static String liga(String nombre) {
        return switch (nombre) { case "Champions League" -> "Champions"; case "Premier League" -> "Premier"; default -> nombre; };
    }
    public static void cerrar() { EXECUTOR.shutdownNow(); }
}
