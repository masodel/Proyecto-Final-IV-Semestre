package ni.edu.uam.proyectofinalivsemestre.ui;

import ni.edu.uam.proyectofinalivsemestre.config.HibernateUtil;
import ni.edu.uam.proyectofinalivsemestre.model.*;
import ni.edu.uam.proyectofinalivsemestre.service.FootballApiService;
import ni.edu.uam.proyectofinalivsemestre.service.real.ServiciosReales;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.io.IOException;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in: sincroniza datos reales en la BD local. No imprime credenciales. */
@EnabledIfSystemProperty(named = "futbol.test.real", matches = "true")
class RealIntegrationTest {
    @Test void sincronizaYConservaCacheEnPostgres() throws Exception {
        ServiciosReales servicio = new ServiciosReales();
        LocalDate hoy = LocalDate.now();
        servicio.sincronizar(null, hoy);
        long cantidad;
        Long id;
        LocalDate fecha;
        try (var em = HibernateUtil.getEntityManager()) {
            cantidad = em.createQuery("select count(m) from Match m", Long.class).getSingleResult();
            assertTrue(cantidad > 10);
            Match partido = em.createQuery("select m from Match m order by m.id desc", Match.class).setMaxResults(1).getSingleResult();
            id = (long) partido.getId();
            fecha = partido.getUtcDate().atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
        }
        assertFalse(servicio.buscar(null, fecha).isEmpty());
        boolean favoritoAntes = servicio.idsPartidosFavoritos().contains(id);
        servicio.alternarPartido(id);
        HibernateUtil.shutdown();
        assertEquals(!favoritoAntes, servicio.idsPartidosFavoritos().contains(id), "El favorito debe sobrevivir a la reconexión");
        servicio.alternarPartido(id);
        int anio = ServiciosReales.temporada(hoy);
        String temporada = anio + "/" + String.format("%02d", (anio + 1) % 100);
        servicio.sincronizar("LaLiga", temporada);
        assertFalse(servicio.tabla("LaLiga", temporada).isEmpty());
        servicio.actualizarDetalle(id);
        assertNotNull(servicio.detalle(id));
        ServiciosReales sinRed = new ServiciosReales(new FootballApiService(false) {
            @Override public String consultarRecurso(String recurso) throws IOException {
                throw new IOException("Fallo de red simulado");
            }
        });
        assertThrows(IOException.class, () -> sinRed.sincronizar(null, hoy));
        assertFalse(sinRed.buscar(null, fecha).isEmpty());
        try (var em = HibernateUtil.getEntityManager()) {
            assertEquals(cantidad, em.createQuery("select count(m) from Match m", Long.class).getSingleResult());
        }
        System.out.println("Integración real verificada: " + cantidad + " partidos guardados; favoritos persistentes y caché sin red.");
        HibernateUtil.shutdown();
    }
}
