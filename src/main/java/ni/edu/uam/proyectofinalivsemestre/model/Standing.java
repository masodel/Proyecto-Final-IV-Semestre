package ni.edu.uam.proyectofinalivsemestre.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ENTIDAD 6: Standing
 * Representa la entrada de clasificación/tabla de posiciones de un equipo
 * dentro de una de las 3 ligas (LaLiga, Premier League o Champions League).
 * Soporta la Pantalla 2 (Tablas de Posiciones) y la Consulta HQL 1.
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
@Entity
@Table(
    name = "standings",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_competition_team", columnNames = {"competition_id", "team_id"})
    }
)
public class Standing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Competición a la que pertenece esta tabla (FK → competitions) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    /** Equipo posicionado (FK → teams) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /** Posición en la tabla (1, 2, 3...) */
    @Column(name = "position", nullable = false)
    private int position;

    /** Partidos jugados (PJ) */
    @Column(name = "played_games", nullable = false)
    private int playedGames = 0;

    /** Partidos ganados (PG) */
    @Column(name = "won", nullable = false)
    private int won = 0;

    /** Partidos empatados (PE) */
    @Column(name = "draw", nullable = false)
    private int draw = 0;

    /** Partidos perdidos (PP) */
    @Column(name = "lost", nullable = false)
    private int lost = 0;

    /** Puntos acumulados (PTS) */
    @Column(name = "points", nullable = false)
    private int points = 0;

    /** Goles a favor (GF) */
    @Column(name = "goals_for", nullable = false)
    private int goalsFor = 0;

    /** Goles en contra (GC) */
    @Column(name = "goals_against", nullable = false)
    private int goalsAgainst = 0;

    /** Diferencia de goles (DG) */
    @Column(name = "goal_difference", nullable = false)
    private int goalDifference = 0;

    /** Timestamp de sincronización */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // -------------------------------------------------------------------------
    // Constructores
    // -------------------------------------------------------------------------
    public Standing() {}

    public Standing(Competition competition, Team team, int position, int playedGames,
                    int won, int draw, int lost, int points,
                    int goalsFor, int goalsAgainst, int goalDifference) {
        this.competition = competition;
        this.team = team;
        this.position = position;
        this.playedGames = playedGames;
        this.won = won;
        this.draw = draw;
        this.lost = lost;
        this.points = points;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
        this.goalDifference = goalDifference;
        this.updatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Getters y Setters
    // -------------------------------------------------------------------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Competition getCompetition() { return competition; }
    public void setCompetition(Competition competition) { this.competition = competition; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public int getPlayedGames() { return playedGames; }
    public void setPlayedGames(int playedGames) { this.playedGames = playedGames; }

    public int getWon() { return won; }
    public void setWon(int won) { this.won = won; }

    public int getDraw() { return draw; }
    public void setDraw(int draw) { this.draw = draw; }

    public int getLost() { return lost; }
    public void setLost(int lost) { this.lost = lost; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public int getGoalsFor() { return goalsFor; }
    public void setGoalsFor(int goalsFor) { this.goalsFor = goalsFor; }

    public int getGoalsAgainst() { return goalsAgainst; }
    public void setGoalsAgainst(int goalsAgainst) { this.goalsAgainst = goalsAgainst; }

    public int getGoalDifference() { return goalDifference; }
    public void setGoalDifference(int goalDifference) { this.goalDifference = goalDifference; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        String teamName = (team != null) ? team.getShortName() : "Equipo";
        return String.format("#%2d %-15s | PJ: %2d | PG: %2d | PE: %2d | PP: %2d | DG: %+3d | PTS: %2d",
                position, teamName, playedGames, won, draw, lost, goalDifference, points);
    }
}
