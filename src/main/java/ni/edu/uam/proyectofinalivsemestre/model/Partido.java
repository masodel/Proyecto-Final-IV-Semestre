package ni.edu.uam.proyectofinalivsemestre.model;

// TEMPORAL: reemplazar por la versión del Integrante 1/2
public final class Partido {
    private final Long id;
    private final java.time.LocalDateTime fecha;
    private final Liga liga;
    private final Equipo local;
    private final Equipo visitante;
    private final Integer golesLocal;
    private final Integer golesVisitante;
    private final String estado;
    private final Integer minuto;

    public Partido(Long id, java.time.LocalDateTime fecha, Liga liga, Equipo local, Equipo visitante, Integer golesLocal, Integer golesVisitante, String estado, Integer minuto) {
        this.id = id;
        this.fecha = fecha;
        this.liga = liga;
        this.local = local;
        this.visitante = visitante;
        this.golesLocal = golesLocal;
        this.golesVisitante = golesVisitante;
        this.estado = estado;
        this.minuto = minuto;
    }
    public Long getId() { return id; }
    public java.time.LocalDateTime getFecha() { return fecha; }
    public Liga getLiga() { return liga; }
    public Equipo getLocal() { return local; }
    public Equipo getVisitante() { return visitante; }
    public Integer getGolesLocal() { return golesLocal; }
    public Integer getGolesVisitante() { return golesVisitante; }
    public String getEstado() { return estado; }
    public Integer getMinuto() { return minuto; }
}
