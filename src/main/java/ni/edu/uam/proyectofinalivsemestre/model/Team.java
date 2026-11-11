package ni.edu.uam.proyectofinalivsemestre.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ENTIDAD 2: Team
 * Representa un equipo de fútbol participante en una de las 3 ligas.
 * Incluye el campo is_favorite para la funcionalidad de favoritos del usuario.
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
@Entity
@Table(name = "teams")
public class Team {

    /** ID oficial de football-data.org */
    @Id
    @Column(name = "id", nullable = false)
    private int id;

    /** Nombre completo del club (ej: "Real Madrid CF") */
    @Column(name = "name", length = 120, nullable = false)
    private String name;

    /** Nombre corto del club (ej: "Real Madrid") */
    @Column(name = "short_name", length = 60)
    private String shortName;

    /** Siglas del club (ej: "RMA", "BAR", "MCI") */
    @Column(name = "tla", length = 5)
    private String tla;

    /** URL del escudo del equipo desde la API */
    @Column(name = "crest_url")
    private String crestUrl;

    /** Liga a la que pertenece este equipo (FK → competitions) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    /** Marcador de favorito del usuario (guardado en BD local) */
    @Column(name = "is_favorite", nullable = false)
    private boolean isFavorite = false;

    /** Timestamp de última actualización desde la API */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Partidos como local */
    @OneToMany(mappedBy = "homeTeam", fetch = FetchType.LAZY)
    private List<Match> homeMatches = new ArrayList<>();

    /** Partidos como visitante */
    @OneToMany(mappedBy = "awayTeam", fetch = FetchType.LAZY)
    private List<Match> awayMatches = new ArrayList<>();

    /** Jugadores del equipo */
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Player> players = new ArrayList<>();

    /** Posiciones en tabla de este equipo */
    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    private List<Standing> standings = new ArrayList<>();

    /** Favoritos que referencian este equipo */
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Favorite> favorites = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructores
    // -------------------------------------------------------------------------
    public Team() {}

    public Team(int id, String name, String shortName, String tla,
                String crestUrl, Competition competition, boolean isFavorite) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.tla = tla;
        this.crestUrl = crestUrl;
        this.competition = competition;
        this.isFavorite = isFavorite;
        this.updatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Getters y Setters
    // -------------------------------------------------------------------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public String getTla() { return tla; }
    public void setTla(String tla) { this.tla = tla; }

    public String getCrestUrl() { return crestUrl; }
    public void setCrestUrl(String crestUrl) { this.crestUrl = crestUrl; }

    public Competition getCompetition() { return competition; }
    public void setCompetition(Competition competition) { this.competition = competition; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Match> getHomeMatches() { return homeMatches; }
    public List<Match> getAwayMatches() { return awayMatches; }
    public List<Player> getPlayers() { return players; }
    public List<Standing> getStandings() { return standings; }
    public List<Favorite> getFavorites() { return favorites; }

    @Override
    public String toString() {
        return "Team{id=" + id + ", tla='" + tla + "', name='" + name
                + "', favorite=" + isFavorite + "}";
    }
}
