package ni.edu.uam.proyectofinalivsemestre.service;
import ni.edu.uam.proyectofinalivsemestre.model.*;
import ni.edu.uam.proyectofinalivsemestre.model.dto.PartidoDetalle;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
// TEMPORAL: reemplazar por la versión del Integrante 1/2
public interface FavoritoService {
    Set<Long> idsPartidosFavoritos();
    Set<Long> idsEquiposFavoritos();
    boolean alternarPartido(Long partidoId);
    boolean alternarEquipo(Long equipoId);
    List<Partido> partidosFavoritos();
    List<Equipo> equiposFavoritos();
}
