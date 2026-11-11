package ni.edu.uam.proyectofinalivsemestre.model;

// TEMPORAL: reemplazar por la versión del Integrante 1/2
public final class Posicion {
    private final Integer posicion;
    private final Equipo equipo;
    private final Integer partidosJugados;
    private final Integer ganados;
    private final Integer empatados;
    private final Integer perdidos;
    private final Integer golesFavor;
    private final Integer golesContra;
    private final Integer diferencia;
    private final Integer puntos;

    public Posicion(Integer posicion, Equipo equipo, Integer partidosJugados, Integer ganados, Integer empatados, Integer perdidos, Integer golesFavor, Integer golesContra, Integer diferencia, Integer puntos) {
        this.posicion = posicion;
        this.equipo = equipo;
        this.partidosJugados = partidosJugados;
        this.ganados = ganados;
        this.empatados = empatados;
        this.perdidos = perdidos;
        this.golesFavor = golesFavor;
        this.golesContra = golesContra;
        this.diferencia = diferencia;
        this.puntos = puntos;
    }
    public Integer getPosicion() { return posicion; }
    public Equipo getEquipo() { return equipo; }
    public Integer getPartidosJugados() { return partidosJugados; }
    public Integer getGanados() { return ganados; }
    public Integer getEmpatados() { return empatados; }
    public Integer getPerdidos() { return perdidos; }
    public Integer getGolesFavor() { return golesFavor; }
    public Integer getGolesContra() { return golesContra; }
    public Integer getDiferencia() { return diferencia; }
    public Integer getPuntos() { return puntos; }
}
