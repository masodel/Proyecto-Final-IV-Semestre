package ni.edu.uam.proyectofinalivsemestre.service.stub;
import ni.edu.uam.proyectofinalivsemestre.model.Partido;
import ni.edu.uam.proyectofinalivsemestre.model.dto.PartidoDetalle;
import ni.edu.uam.proyectofinalivsemestre.service.PartidoService;
import java.time.LocalDate;
import java.util.List;
// TEMPORAL: reemplazar por la versión del Integrante 1/2
public final class PartidoServiceDemo implements PartidoService {
    public List<Partido> buscar(String liga, LocalDate fecha) {
        return DatosDemo.PARTIDOS.stream().filter(p -> liga == null || p.getLiga().getNombre().equals(liga))
                .filter(p -> fecha == null || p.getFecha().toLocalDate().equals(fecha)).toList();
    }
    public void sincronizar(String liga, LocalDate fecha) throws Exception { DatosDemo.sincronizar(); }
    public PartidoDetalle detalle(Long id) {
        Partido p = DatosDemo.PARTIDOS.stream().filter(x -> x.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el partido."));
        if ("PROGRAMADO".equals(p.getEstado()))
            return new PartidoDetalle(p, "[ESTADIO]", List.of("El partido todavía no ha comenzado."), null, null, null, null, null);
        List<String> eventos = id.equals(2L)
                ? List.of("12' · Gol · Real Madrid", "34' · Tarjeta amarilla · Barcelona",
                          "58' · Gol · Barcelona", "71' · Gol · Real Madrid")
                : List.of("35' · Gol · Inter", "67' · Partido en curso");
        return new PartidoDetalle(p, "[ESTADIO]", eventos, 54, 12, 9, 5, 3);
    }
}
