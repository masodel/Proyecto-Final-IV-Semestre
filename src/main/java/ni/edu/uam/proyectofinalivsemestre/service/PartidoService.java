package ni.edu.uam.proyectofinalivsemestre.service;
import ni.edu.uam.proyectofinalivsemestre.model.*;
import ni.edu.uam.proyectofinalivsemestre.model.dto.PartidoDetalle;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
// TEMPORAL: reemplazar por la versión del Integrante 1/2
public interface PartidoService {
    List<Partido> buscar(String liga, LocalDate fecha);
    void sincronizar(String liga, LocalDate fecha) throws Exception;
    PartidoDetalle detalle(Long partidoId);
    default void actualizarDetalle(Long partidoId) throws Exception {
        Partido p = detalle(partidoId).partido();
        sincronizar(p.getLiga().getNombre(), p.getFecha().toLocalDate());
    }
}
