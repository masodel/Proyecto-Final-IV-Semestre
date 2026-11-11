package ni.edu.uam.proyectofinalivsemestre.model;

import jakarta.persistence.*;

/**
 * ENTIDAD 7: Lineup
 * Representa la alineación táctica de un jugador en un partido específico
 * (indica si es titular o suplente y su rol táctico).
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
@Entity
@Table(name = "lineups")
public class Lineup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Partido al que corresponde la alineación (FK → matches) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    /** Equipo al que pertenece (FK → teams) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /** Jugador alineado (FK → players) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    /** Indica si arranca como titular (TRUE) o está en banca (FALSE) */
    @Column(name = "is_starter", nullable = false)
    private boolean isStarter = true;

    /** Posición táctica en cancha: 'GK', 'DF', 'MF', 'FW', 'SUB' */
    @Column(name = "tactical_position", length = 20)
    private String tacticalPosition;

    /** Dorsal utilizado en este partido */
    @Column(name = "shirt_number")
    private Integer shirtNumber;

    // -------------------------------------------------------------------------
    // Constructores
    // -------------------------------------------------------------------------
    public Lineup() {}

    public Lineup(Match match, Team team, Player player, boolean isStarter, String tacticalPosition, Integer shirtNumber) {
        this.match = match;
        this.team = team;
        this.player = player;
        this.isStarter = isStarter;
        this.tacticalPosition = tacticalPosition;
        this.shirtNumber = shirtNumber;
    }

    // -------------------------------------------------------------------------
    // Getters y Setters
    // -------------------------------------------------------------------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public boolean isStarter() { return isStarter; }
    public void setStarter(boolean starter) { isStarter = starter; }

    public String getTacticalPosition() { return tacticalPosition; }
    public void setTacticalPosition(String tacticalPosition) { this.tacticalPosition = tacticalPosition; }

    public Integer getShirtNumber() { return shirtNumber; }
    public void setShirtNumber(Integer shirtNumber) { this.shirtNumber = shirtNumber; }

    @Override
    public String toString() {
        String playerName = (player != null) ? player.getName() : "Jugador";
        return String.format("[%s] #%d %s (%s)",
                isStarter ? "TITULAR" : "BANCA",
                shirtNumber != null ? shirtNumber : 0,
                playerName,
                tacticalPosition != null ? tacticalPosition : "-");
    }
}
