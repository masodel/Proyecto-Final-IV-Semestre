package ni.edu.uam.proyectofinalivsemestre.controller;

import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import ni.edu.uam.proyectofinalivsemestre.util.UiUtil;
import java.io.IOException;

public final class MainController {
    @FXML private ToggleGroup grupoNav;
    @FXML private ToggleButton btnPartidos, btnPosiciones, btnFavoritos;
    @FXML private Label lblEstadoCache;
    @FXML private StackPane contenido;
    @FXML private void initialize() {
        lblEstadoCache.setText(ni.edu.uam.proyectofinalivsemestre.service.Servicios.DEMO
                ? "Caché local · demostración\nDatos en memoria" : "Modo API real\nCaché en PostgreSQL");
        grupoNav.selectedToggleProperty().addListener((o, anterior, actual) -> {
            if (actual == null && anterior != null) anterior.setSelected(true);
        });
        irAPartidos();
    }
    private <T> T abrir(String vista, ToggleButton boton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/" + vista + ".fxml"));
            Parent root = loader.load();
            contenido.getChildren().setAll(root);
            boton.setSelected(true);
            return loader.getController();
        } catch (IOException e) { UiUtil.error(e); return null; }
    }
    @FXML private void irAPartidos() {
        PartidosController c = abrir("partidos", btnPartidos);
        if (c != null) c.setAbrirDetalle(this::abrirDetalle);
    }
    @FXML private void irAPosiciones() { abrir("posiciones", btnPosiciones); }
    @FXML private void irAFavoritos() { abrir("favoritos", btnFavoritos); }
    public void abrirDetalle(Long id) {
        FavoritosController c = abrir("favoritos", btnFavoritos);
        if (c != null) c.abrirPartido(id);
    }
}
