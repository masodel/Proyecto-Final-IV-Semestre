package ni.edu.uam.proyectofinalivsemestre.service;
import ni.edu.uam.proyectofinalivsemestre.service.stub.*;
import ni.edu.uam.proyectofinalivsemestre.service.real.ServiciosReales;

/** Real por defecto; la demo se activa explícitamente para pruebas sin API/BD. */
public final class Servicios {
    public static final boolean DEMO = Boolean.getBoolean("futbol.demo");
    private static final ServiciosReales REAL = DEMO ? null : new ServiciosReales();
    private static final PartidoService PARTIDOS = DEMO ? new PartidoServiceDemo() : REAL;
    private static final PosicionService POSICIONES = DEMO ? new PosicionServiceDemo() : REAL;
    private static final FavoritoService FAVORITOS = DEMO ? new FavoritoServiceDemo() : REAL;
    private Servicios() {}
    public static PartidoService partidos() { return PARTIDOS; }
    public static PosicionService posiciones() { return POSICIONES; }
    public static FavoritoService favoritos() { return FAVORITOS; }
    public static String origen() { return DEMO ? "datos de ejemplo" : "datos guardados en PostgreSQL"; }
}
