package ni.edu.uam.proyectofinalivsemestre.controller;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ni.edu.uam.proyectofinalivsemestre.model.Posicion;
import ni.edu.uam.proyectofinalivsemestre.service.Servicios;
import ni.edu.uam.proyectofinalivsemestre.util.UiUtil;
import java.util.List;
import java.util.function.Function;

public final class PosicionesController {
    @FXML private ComboBox<String> cmbLiga, cmbTemporada;
    @FXML private Button btnActualizar;
    @FXML private ProgressIndicator piCarga;
    @FXML private Label lblInfo;
    @FXML private TableView<Posicion> tblPosiciones;
    @FXML private TableColumn<Posicion, Object> colPos, colEquipo, colPj, colG, colE, colP, colGf, colGc, colDg, colPts;
    private record Resultado(List<Posicion> filas, String aviso) {}
    @FXML private void initialize() {
        cmbLiga.getItems().setAll("LaLiga", "Premier League", "Champions League"); cmbLiga.setValue("LaLiga");
        int inicio = Servicios.DEMO ? 2026 : ni.edu.uam.proyectofinalivsemestre.service.real.ServiciosReales.temporada(java.time.LocalDate.now());
        for (int anio = inicio; anio >= inicio - 2; anio--)
            cmbTemporada.getItems().add(anio + "/" + String.format("%02d", (anio + 1) % 100));
        cmbTemporada.getSelectionModel().selectFirst();
        tblPosiciones.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblPosiciones.setPlaceholder(new Label("No hay posiciones guardadas para esta temporada."));
        columna(colPos, Posicion::getPosicion); columna(colEquipo, x -> x.getEquipo().getNombre());
        columna(colPj, Posicion::getPartidosJugados); columna(colG, Posicion::getGanados);
        columna(colE, Posicion::getEmpatados); columna(colP, Posicion::getPerdidos);
        columna(colGf, Posicion::getGolesFavor); columna(colGc, Posicion::getGolesContra);
        columna(colDg, x -> x.getDiferencia() > 0 ? "+" + x.getDiferencia() : x.getDiferencia());
        columna(colPts, Posicion::getPuntos);
        tblPosiciones.setRowFactory(t -> {
            TableRow<Posicion> fila = new TableRow<>() {
                @Override protected void updateItem(Posicion item, boolean vacia) {
                    super.updateItem(item, vacia); getStyleClass().remove("zona-champions");
                    if (!vacia && item != null && item.getPosicion() <= 4) getStyleClass().add("zona-champions");
                }
            };
            fila.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !fila.isEmpty()) alternar(fila.getItem()); });
            return fila;
        });
        tblPosiciones.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER && tblPosiciones.getSelectionModel().getSelectedItem() != null)
                alternar(tblPosiciones.getSelectionModel().getSelectedItem());
        });
        cmbLiga.valueProperty().addListener((o,a,n) -> cargar(!Servicios.DEMO));
        cmbTemporada.valueProperty().addListener((o,a,n) -> cargar(!Servicios.DEMO));
        cargar(!Servicios.DEMO);
    }
    private void columna(TableColumn<Posicion,Object> columna, Function<Posicion,Object> valor) {
        columna.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(valor.apply(c.getValue())));
    }
    private void ocupado(boolean valor) { UiUtil.ocupado(valor, piCarga, cmbLiga, cmbTemporada, btnActualizar, tblPosiciones); }
    private void alternar(Posicion posicion) {
        ocupado(true);
        UiUtil.async(() -> Servicios.favoritos().alternarEquipo(posicion.getEquipo().getId()), favorito -> {
            ocupado(false); lblInfo.setText(posicion.getEquipo().getNombre() + (favorito ? " agregado a favoritos." : " eliminado de favoritos."));
        }, e -> { ocupado(false); UiUtil.error(e); });
    }
    @FXML private void onActualizar() { cargar(true); }
    private void cargar(boolean sincronizar) {
        String liga = cmbLiga.getValue(), temporada = cmbTemporada.getValue();
        if (liga == null || temporada == null) { UiUtil.aviso("Selecciona liga y temporada."); return; }
        ocupado(true);
        UiUtil.async(() -> {
            String aviso = null;
            if (sincronizar) try { Servicios.posiciones().sincronizar(liga, temporada); }
            catch (Exception e) { aviso = "No se pudo actualizar. Se muestran los datos guardados.\n" + e.getMessage(); }
            return new Resultado(Servicios.posiciones().tabla(liga, temporada), aviso);
        }, r -> {
            tblPosiciones.getItems().setAll(r.filas()); ocupado(false);
            lblInfo.setText("Clasificación · " + Servicios.origen() + " · doble clic en un equipo para marcarlo favorito");
            if (r.aviso() != null) UiUtil.aviso(r.aviso());
        }, e -> { ocupado(false); UiUtil.error(e); });
    }
}
