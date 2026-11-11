package ni.edu.uam.proyectofinalivsemestre.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ni.edu.uam.proyectofinalivsemestre.model.*;
import ni.edu.uam.proyectofinalivsemestre.model.dto.PartidoDetalle;
import ni.edu.uam.proyectofinalivsemestre.service.Servicios;
import ni.edu.uam.proyectofinalivsemestre.util.UiUtil;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class FavoritosController {
    @FXML private ToggleGroup grupoTipo;
    @FXML private ToggleButton btnTipoPartidos, btnTipoEquipos;
    @FXML private ListView<Object> lstFavoritos;
    @FXML private Label lblSinSeleccion, lblLiga, lblFechaEstadio, lblEstado, lblLocal, lblMarcador, lblVisitante;
    @FXML private Label lblPosesion, lblTiros, lblTirosPuerta;
    @FXML private Label lblPosesionVisitante, lblTirosVisitante, lblTirosPuertaVisitante;
    @FXML private VBox panelDetalle;
    @FXML private ListView<String> lstEventos;
    @FXML private ProgressBar pbPosesion, pbTiros, pbTirosPuerta;
    @FXML private Button btnFavorito, btnActualizarDetalle;
    @FXML private ProgressIndicator piCarga;
    private Long partidoId;
    private PartidoDetalle detalle;
    private int solicitud;
    private boolean cambiandoLista;
    private record Resultado(List<?> favoritos, PartidoDetalle detalle, boolean marcado, String aviso) {}

    @FXML private void initialize() {
        grupoTipo.selectedToggleProperty().addListener((o,a,n) -> { if (n == null && a != null) a.setSelected(true); });
        lstFavoritos.setPlaceholder(new Label("Todavía no tienes favoritos."));
        lstFavoritos.setCellFactory(l -> new ListCell<>() {
            @Override protected void updateItem(Object item, boolean vacia) {
                super.updateItem(item, vacia);
                setText(vacia || item == null ? null : item instanceof Partido p
                        ? p.getLocal().getNombre() + " " + UiUtil.marcador(p) + " " + p.getVisitante().getNombre() + "  ★"
                        : ((Equipo) item).getNombre() + "  ★");
            }
        });
        lstFavoritos.getSelectionModel().selectedItemProperty().addListener((o,a,n) -> {
            if (!cambiandoLista && n instanceof Partido partido) abrirPartido(partido.getId());
        });
        lstFavoritos.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && lstFavoritos.getSelectionModel().getSelectedItem() instanceof Equipo equipo) quitarEquipo(equipo);
        });
        lstFavoritos.setOnKeyPressed(e -> {
            if ((e.getCode() == javafx.scene.input.KeyCode.DELETE || e.getCode() == javafx.scene.input.KeyCode.ENTER)
                    && lstFavoritos.getSelectionModel().getSelectedItem() instanceof Equipo equipo) quitarEquipo(equipo);
        });
        mostrarDetalle(false);
        cargar(false);
    }
    public void abrirPartido(Long id) {
        btnTipoPartidos.setSelected(true); partidoId = id; cargar(false);
    }
    @FXML private void onCambiarTipo() {
        partidoId = null; detalle = null; mostrarDetalle(false);
        lblSinSeleccion.setText(btnTipoPartidos.isSelected() ? "Selecciona un partido para ver el detalle"
                : "Doble clic o Supr en un equipo para quitarlo de favoritos");
        cargar(false);
    }
    private void ocupado(boolean valor) {
        UiUtil.ocupado(valor, piCarga, btnTipoPartidos, btnTipoEquipos, lstFavoritos, btnFavorito, btnActualizarDetalle);
        if (!valor) btnActualizarDetalle.setDisable(partidoId == null);
    }
    private void mostrarDetalle(boolean visible) {
        panelDetalle.setVisible(visible); panelDetalle.setManaged(visible);
        lblSinSeleccion.setVisible(!visible); lblSinSeleccion.setManaged(!visible);
    }
    @FXML private void onActualizarDetalle() { if (partidoId != null) cargar(true); }
    private void cargar(boolean sincronizar) {
        int version = ++solicitud;
        Long id = partidoId; boolean partidos = btnTipoPartidos.isSelected();
        ocupado(true);
        UiUtil.async(() -> {
            String aviso = null;
            PartidoDetalle d = id == null ? null : Servicios.partidos().detalle(id);
            if (sincronizar && d != null) {
                try {
                    Servicios.partidos().actualizarDetalle(id);
                } catch (Exception e) { aviso = "No se pudo actualizar. Se muestra el detalle guardado.\n" + e.getMessage(); }
                d = Servicios.partidos().detalle(id);
            }
            List<?> lista = partidos ? Servicios.favoritos().partidosFavoritos() : Servicios.favoritos().equiposFavoritos();
            boolean marcado = id != null && Servicios.favoritos().idsPartidosFavoritos().contains(id);
            return new Resultado(lista, d, marcado, aviso);
        }, r -> {
            if (version != solicitud) return;
            cambiandoLista = true;
            try {
                lstFavoritos.getItems().setAll(r.favoritos());
                if (id != null) lstFavoritos.getItems().stream()
                        .filter(x -> x instanceof Partido partido && partido.getId().equals(id)).findFirst()
                        .ifPresent(x -> lstFavoritos.getSelectionModel().select(x));
            } finally { cambiandoLista = false; }
            detalle = r.detalle();
            mostrarDetalle(detalle != null);
            if (detalle != null) pintarDetalle(detalle, r.marcado());
            ocupado(false);
            if (r.aviso() != null) UiUtil.aviso(r.aviso());
        }, e -> { if (version == solicitud) { ocupado(false); UiUtil.error(e); } });
    }
    private void pintarDetalle(PartidoDetalle d, boolean marcado) {
        Partido p = d.partido();
        lblLiga.setText(UiUtil.liga(p.getLiga().getNombre()));
        lblFechaEstadio.setText(p.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm")) + " · Estadio " + d.estadio());
        lblEstado.setText(UiUtil.estado(p)); lblEstado.getStyleClass().remove("estado-vivo");
        if ("EN_VIVO".equals(p.getEstado())) lblEstado.getStyleClass().add("estado-vivo");
        lblLocal.setText(p.getLocal().getNombre()); lblVisitante.setText(p.getVisitante().getNombre());
        lblMarcador.setText(UiUtil.marcador(p));
        lstEventos.getItems().setAll(d.eventos() == null ? List.of() : d.eventos());
        estadistica(pbPosesion, lblPosesion, lblPosesionVisitante, d.posesionLocal(), d.posesionLocal() == null ? null : 100 - d.posesionLocal(), true);
        estadistica(pbTiros, lblTiros, lblTirosVisitante, d.tirosLocal(), d.tirosVisitante(), false);
        estadistica(pbTirosPuerta, lblTirosPuerta, lblTirosPuertaVisitante, d.tirosPuertaLocal(), d.tirosPuertaVisitante(), false);
        btnFavorito.setText(marcado ? "Quitar de favoritos" : "Agregar a favoritos");
    }
    private void estadistica(ProgressBar barra, Label etiqueta, Label etiquetaVisitante, Integer local, Integer visitante, boolean porcentaje) {
        if (local == null || visitante == null) {
            barra.setProgress(0); etiqueta.setText("—"); etiquetaVisitante.setText("—"); return;
        }
        int total = local + visitante; barra.setProgress(total <= 0 ? 0 : (double) local / total);
        String unidad = porcentaje ? "%" : "";
        etiqueta.setText(local + unidad); etiquetaVisitante.setText(visitante + unidad);
    }
    @FXML private void onAlternarFavorito() {
        if (partidoId == null) return;
        Long id = partidoId; ocupado(true);
        UiUtil.async(() -> Servicios.favoritos().alternarPartido(id), marcado -> cargar(false),
                e -> { ocupado(false); UiUtil.error(e); });
    }
    private void quitarEquipo(Equipo equipo) {
        ocupado(true);
        UiUtil.async(() -> Servicios.favoritos().alternarEquipo(equipo.getId()), marcado -> cargar(false),
                e -> { ocupado(false); UiUtil.error(e); });
    }
}
