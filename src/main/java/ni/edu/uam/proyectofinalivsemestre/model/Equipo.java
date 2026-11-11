package ni.edu.uam.proyectofinalivsemestre.model;

// TEMPORAL: reemplazar por la versión del Integrante 1/2
public final class Equipo {
    private final Long id;
    private final String nombre;

    public Equipo(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
}
