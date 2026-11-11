package ni.edu.uam.proyectofinalivsemestre.service.stub;
import ni.edu.uam.proyectofinalivsemestre.model.*;
import ni.edu.uam.proyectofinalivsemestre.service.FavoritoService;
import java.util.*;
// TEMPORAL: reemplazar por la versión del Integrante 1/2
public final class FavoritoServiceDemo implements FavoritoService {
    public Set<Long> idsPartidosFavoritos() { return Set.copyOf(DatosDemo.FAVORITOS_PARTIDOS); }
    public Set<Long> idsEquiposFavoritos() { return Set.copyOf(DatosDemo.FAVORITOS_EQUIPOS); }
    public synchronized boolean alternarPartido(Long id) { return alternar(DatosDemo.FAVORITOS_PARTIDOS, id); }
    public synchronized boolean alternarEquipo(Long id) { return alternar(DatosDemo.FAVORITOS_EQUIPOS, id); }
    private boolean alternar(Set<Long> ids, Long id) { if (ids.remove(id)) return false; ids.add(id); return true; }
    public List<Partido> partidosFavoritos() { return DatosDemo.PARTIDOS.stream().filter(p -> DatosDemo.FAVORITOS_PARTIDOS.contains(p.getId())).toList(); }
    public List<Equipo> equiposFavoritos() { return DatosDemo.EQUIPOS.values().stream().filter(e -> DatosDemo.FAVORITOS_EQUIPOS.contains(e.getId())).toList(); }
}
