package ni.edu.uam.proyectofinalivsemestre.model;

/**
 * Record DTO temporal para representar un partido procesado desde la API o Mock.
 * Contiene únicamente los datos esenciales solicitados para el MVP:
 * - Nombre de la competición
 * - Equipo local
 * - Equipo visitante
 * - Estado del partido
 * - Marcador final
 */
public record PartidoTemp(
        String competicion,
        String equipoLocal,
        String equipoVisitante,
        String estado,
        String marcadorFinal
) {
    @Override
    public String toString() {
        return String.format("[%s] %-25s %-7s %-25s | Estado: %s",
                competicion, equipoLocal, marcadorFinal, equipoVisitante, estado);
    }
}
