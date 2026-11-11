package ni.edu.uam.proyectofinalivsemestre.model;

import jakarta.persistence.*;

/**
 * ENTIDAD 5: MatchEvent
 * Representa un evento ocurrido durante el desarrollo de un partido
 * (Goles, Tarjetas Amarillas/Rojas, Sustituciones).
 * Necesario para la Pantalla 3 (Detalle de Partido).
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
@Entity
@Table(name = "match_events")
public class MatchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Partido al que pertenece el evento (FK → matches) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    /** Minuto en el que ocurrió el evento (ej: 45, 90) */
    @Column(name = "minute", nullable = false)
    private int minute;

    /** Tipo de evento: 'GOAL', 'YELLOW_CARD', 'RED_CARD', 'SUBSTITUTION' */
    @Column(name = "type", length = 30, nullable = false)
    private String type;

    /** Jugador protagonista (opcional, FK → players) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    /** Equipo beneficiado o involucrado (FK → teams) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /** Detalle adicional (ej: "Asistencia de Kroos", "De penal", etc.) */
    @Column(name = "detail", length = 200)
    private String detail;

    // -------------------------------------------------------------------------
    // Constructores
    // -------------------------------------------------------------------------
    public MatchEvent() {}

    public MatchEvent(Match match, int minute, String type, Player player, Team team, String detail) {
        this.match = match;
        this.minute = minute;
        this.type = type;
        this.player = player;
        this.team = team;
        this.detail = detail;
    }

    // -------------------------------------------------------------------------
    // Getters y Setters
    // -------------------------------------------------------------------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }

    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    @Override
    public String toString() {
        String playerName = (player != null) ? player.getName() : "Jugador";
        return String.format("%d' [%s] %s (%s)", minute, type, playerName, detail != null ? detail : "");
    }
}
