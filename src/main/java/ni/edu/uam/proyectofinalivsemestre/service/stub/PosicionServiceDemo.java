package ni.edu.uam.proyectofinalivsemestre.service.stub;
import ni.edu.uam.proyectofinalivsemestre.model.Posicion;
import ni.edu.uam.proyectofinalivsemestre.service.PosicionService;
import java.util.*;
// TEMPORAL: reemplazar por la versión del Integrante 1/2
public final class PosicionServiceDemo implements PosicionService {
    public List<Posicion> tabla(String liga, String temporada) {
        if (!"2026/27".equals(temporada)) return List.of();
        String[] nombres = switch (liga) {
            case "LaLiga" -> new String[]{"Real Madrid", "Barcelona", "Atlético", "Athletic", "Villarreal", "Real Sociedad", "Betis", "Girona"};
            case "Premier League" -> new String[]{"Arsenal", "Man. City", "Liverpool", "Chelsea"};
            case "Champions League" -> new String[]{"Inter", "Bayern", "PSG", "Atlético"};
            default -> new String[0];
        };
        int[][] cifras = {{8,2,1,24,9,26},{8,1,2,28,12,25},{7,3,1,20,8,24},{6,3,2,17,10,21},
                {6,2,3,21,15,20},{5,4,2,14,9,19},{5,3,3,16,13,18},{4,3,4,13,14,15}};
        List<Posicion> resultado = new ArrayList<>();
        for (int i = 0; i < nombres.length; i++) {
            int[] c = cifras[i];
            resultado.add(new Posicion(i+1, DatosDemo.EQUIPOS.get(nombres[i]), 11, c[0], c[1], c[2], c[3], c[4], c[3]-c[4], c[5]));
        }
        return List.copyOf(resultado);
    }
    public void sincronizar(String liga, String temporada) throws Exception { DatosDemo.sincronizar(); }
}
