package ni.edu.uam.proyectofinalivsemestre.model.dto;
import ni.edu.uam.proyectofinalivsemestre.model.Partido;
import java.util.List;
// TEMPORAL: reemplazar por la versión del Integrante 1/2
public record PartidoDetalle(Partido partido, String estadio, List<String> eventos,
        Integer posesionLocal, Integer tirosLocal, Integer tirosVisitante,
        Integer tirosPuertaLocal, Integer tirosPuertaVisitante) {}
