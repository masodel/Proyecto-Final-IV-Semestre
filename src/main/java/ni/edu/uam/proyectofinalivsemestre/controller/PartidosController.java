package ni.edu.uam.proyectofinalivsemestre.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ni.edu.uam.proyectofinalivsemestre.model.Partido;
import ni.edu.uam.proyectofinalivsemestre.service.Servicios;
import ni.edu.uam.proyectofinalivsemestre.util.UiUtil;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.*;

public final class PartidosController {
    @FXML private ComboBox<String> cmbLiga;
    @FXML private DatePicker dpFecha;
    @FXML private Button btnActualizar;
    @FXML private ProgressIndicator piCarga;
    @FXML private Label lblResumen;
    @FXML private TableView<Partido> tblPartidos;
    @FXML private TableColumn<Partido, String> colHora, colLiga, colLocal, colMarcador, colVisitante, colEstado;
    @FXML private TableColumn<Partido, Partido> colFavorito;
    private Set<Long> favoritos = Set.of();
    private Consumer<Long> abrirDetalle = id -> {};
    private record Resultado(List<Partido> partidos, Set<Long> favoritos, String aviso) {}
    public void setAbrirDetalle(Consumer<Long> accion) { abrirDetalle = accion; }
    @FXML private void initialize() {
        cmbLiga.getItems().setAll("Todas", "LaLiga", "Premier League", "Champions League");
        cmbLiga.setValue("Todas");
        dpFecha.setValue(Servicios.DEMO ? LocalDate.of(2026, 11, 10) : LocalDate.now());
        tblPartidos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblPartidos.setPlaceholder(new Label("No hay partidos para estos filtros."));
        columna(colHora, x -> x.getFecha().format(DateTimeFormatter.ofPattern("HH:mm")));
        columna(colLiga, x -> UiUtil.liga(x.getLiga().getNombre()));
        columna(colLocal, x -> x.getLocal().getNombre());
        columna(colMarcador, UiUtil::marcador);
        columna(colVisitante, x -> x.getVisitante().getNombre());
        columna(colEstado, UiUtil::estado);
        for (TableColumn<Partido, String> c : List.of(colLiga, colEstado)) {
            c.setCellFactory(column -> new TableCell<>() {
                @Override protected void updateItem(String valor, boolean vacia) {
                    super.updateItem(valor, vacia); setText(null); setGraphic(null);
                    if (!vacia && valor != null) {
                        Label etiqueta = new Label(valor); etiqueta.getStyleClass().add("etiqueta");
                        if (valor.startsWith("En vivo")) etiqueta.getStyleClass().add("estado-vivo");
                        setGraphic(etiqueta);
                    }
                }
            });
        }
        colFavorito.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colFavorito.setCellFactory(c -> new TableCell<>() {
            private final Button estrella = new Button();
            { estrella.getStyleClass().add("btn-estrella"); estrella.setOnAction(e -> {
                Partido partido = getItem();
                if (partido == null) return;
                ocupado(true);
                UiUtil.async(() -> {
                    Servicios.favoritos().alternarPartido(partido.getId());
                    return Servicios.favoritos().idsPartidosFavoritos();
                }, ids -> { favoritos = ids; tblPartidos.refresh(); ocupado(false); },
                error -> { ocupado(false); UiUtil.error(error); });
            }); }
            @Override protected void updateItem(Partido item, boolean vacia) {
                super.updateItem(item, vacia); setText(null); setGraphic(null);
                if (!vacia && item != null) {
                    boolean marcado = favoritos.contains(item.getId());
                    estrella.setText(marcado ? "★" : "☆");
                    estrella.setAccessibleText((marcado ? "Quitar favorito: " : "Marcar favorito: ") + item.getLocal().getNombre());
                    setGraphic(estrella);
                }
            }
        });
        tblPartidos.setRowFactory(t -> {
            TableRow<Partido> fila = new TableRow<>();
            fila.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !fila.isEmpty()) abrirDetalle.accept(fila.getItem().getId()); });
            return fila;
        });
        tblPartidos.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER && tblPartidos.getSelectionModel().getSelectedItem() != null)
                abrirDetalle.accept(tblPartidos.getSelectionModel().getSelectedItem().getId());
        });
        cmbLiga.valueProperty().addListener((o,a,n) -> cargar(false));
        dpFecha.valueProperty().addListener((o,a,n) -> cargar(false));
        cargar(!Servicios.DEMO);
    }
    private void columna(TableColumn<Partido,String> c, Function<Partido,String> valor) {
        c.setCellValueFactory(x -> new ReadOnlyObjectWrapper<>(valor.apply(x.getValue())));
    }
    private void ocupado(boolean valor) { UiUtil.ocupado(valor, piCarga, cmbLiga, dpFecha, btnActualizar, tblPartidos); }
    @FXML private void onActualizar() { cargar(true); }
    private void cargar(boolean sincronizar) {
        LocalDate fecha = dpFecha.getValue();
        if (fecha == null) { UiUtil.aviso("Selecciona una fecha válida."); return; }
        String liga = "Todas".equals(cmbLiga.getValue()) ? null : cmbLiga.getValue();
        ocupado(true);
        UiUtil.async(() -> {
            String aviso = null;
            if (sincronizar) try { Servicios.partidos().sincronizar(liga, fecha); }
            catch (Exception e) { aviso = "No se pudo actualizar. Se muestran los datos guardados.\n" + e.getMessage(); }
            return new Resultado(Servicios.partidos().buscar(liga, fecha), Servicios.favoritos().idsPartidosFavoritos(), aviso);
        }, r -> {
            favoritos = r.favoritos(); tblPartidos.getItems().setAll(r.partidos());
            lblResumen.setText(r.partidos().size() + " partidos · " + Servicios.origen()
                    + " · doble clic en una fila abre el detalle"
                    + (r.partidos().isEmpty() ? " · prueba otra fecha" : ""));
            ocupado(false); if (r.aviso() != null) UiUtil.aviso(r.aviso());
        }, e -> { ocupado(false); UiUtil.error(e); });
    }
}
