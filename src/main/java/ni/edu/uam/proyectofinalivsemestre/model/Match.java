package ni.edu.uam.proyectofinalivsemestre.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ENTIDAD 4: Match
 * Representa un partido de fútbol programado, en vivo o finalizado.
 * Clave para la Pantalla Principal / En Vivo del MVP.
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
@Entity
@Table(name = "matches")
public class Match {

    /** ID oficial de football-data.org */
    @Id
    @Column(name = "id", nullable = false)
    private int id;

    /** Competición a la que pertenece el partido (FK → competitions) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    /** Fecha y hora UTC del encuentro */
    @Column(name = "utc_date", nullable = false)
    private LocalDateTime utcDate;

    /** Estado: 'TIMED' (programado), 'IN_PLAY' (en vivo), 'PAUSED', 'FINISHED' */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    /** Número de jornada (Matchday) */
    @Column(name = "matchday")
    private Integer matchday;

    /** Equipo local (FK → teams) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    /** Equipo visitante (FK → teams) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    /** Marcador del equipo local */
    @Column(name = "home_score")
    private Integer homeScore = 0;

    /** Marcador del equipo visitante */
    @Column(name = "away_score")
    private Integer awayScore = 0;

    /** Timestamp de sincronización local */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Eventos del partido (goles, tarjetas) */
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MatchEvent> events = new ArrayList<>();

    /** Alineaciones del partido */
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Lineup> lineups = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructores
    // -------------------------------------------------------------------------
    public Match() {}

    public Match(int id, Competition competition, LocalDateTime utcDate, String status,
                 Integer matchday, Team homeTeam, Team awayTeam, Integer homeScore, Integer awayScore) {
        this.id = id;
        this.competition = competition;
        this.utcDate = utcDate;
        this.status = status;
        this.matchday = matchday;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.updatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Métodos Helper de Relación Bidireccional
    // -------------------------------------------------------------------------
    public void addEvent(MatchEvent event) {
        events.add(event);
        event.setMatch(this);
    }

    public void removeEvent(MatchEvent event) {
        events.remove(event);
        event.setMatch(null);
    }

    public void addLineup(Lineup lineup) {
        lineups.add(lineup);
        lineup.setMatch(this);
    }

    public void removeLineup(Lineup lineup) {
        lineups.remove(lineup);
        lineup.setMatch(null);
    }

    // -------------------------------------------------------------------------
    // Getters y Setters
    // -------------------------------------------------------------------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Competition getCompetition() { return competition; }
    public void setCompetition(Competition competition) { this.competition = competition; }

    public LocalDateTime getUtcDate() { return utcDate; }
    public void setUtcDate(LocalDateTime utcDate) { this.utcDate = utcDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getMatchday() { return matchday; }
    public void setMatchday(Integer matchday) { this.matchday = matchday; }

    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }

    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }

    public Integer getHomeScore() { return homeScore; }
    public void setHomeScore(Integer homeScore) { this.homeScore = homeScore; }

    public Integer getAwayScore() { return awayScore; }
    public void setAwayScore(Integer awayScore) { this.awayScore = awayScore; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<MatchEvent> getEvents() { return events; }
    public void setEvents(List<MatchEvent> events) { this.events = events; }

    public List<Lineup> getLineups() { return lineups; }
    public void setLineups(List<Lineup> lineups) { this.lineups = lineups; }

    @Override
    public String toString() {
        String home = (homeTeam != null) ? homeTeam.getShortName() : "Local";
        String away = (awayTeam != null) ? awayTeam.getShortName() : "Visitante";
        return String.format("Match #%d [%s]: %s %d - %d %s (%s)",
                id, (competition != null ? competition.getCode() : "LIGA"),
                home, homeScore, awayScore, away, status);
    }
}
