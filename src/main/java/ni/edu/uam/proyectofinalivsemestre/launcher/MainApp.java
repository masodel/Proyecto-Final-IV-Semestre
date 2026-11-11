package ni.edu.uam.proyectofinalivsemestre.launcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ni.edu.uam.proyectofinalivsemestre.util.UiUtil;

public final class MainApp extends Application {
    @Override public void start(Stage stage) throws Exception {
        java.util.Locale.setDefault(java.util.Locale.forLanguageTag("es"));
        Scene scene = new Scene(FXMLLoader.load(MainApp.class.getResource("/views/main.fxml")), 1100, 680);
        scene.getStylesheets().add(MainApp.class.getResource("/css/styles.css").toExternalForm());
        stage.setTitle("Fútbol App");
        stage.setMinWidth(1100); stage.setMinHeight(680);
        stage.setScene(scene); stage.setWidth(1100); stage.setHeight(680); stage.show();
    }
    @Override public void stop() {
        UiUtil.cerrar();
        new Thread(ni.edu.uam.proyectofinalivsemestre.config.HibernateUtil::shutdown, "cerrar-hibernate").start();
    }
    public static void main(String[] args) { launch(args); }
}
