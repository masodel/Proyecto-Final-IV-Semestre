package ni.edu.uam.proyectofinalivsemestre.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ENTIDAD 1: Competition
 * Representa una de las 3 competiciones soportadas:
 *   - LaLiga         (id=2014, code="PD")
 *   - Premier League (id=2021, code="PL")
 *   - UEFA Champions League (id=2001, code="CL")
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
@Entity
@Table(name = "competitions")
public class Competition {

    /** ID oficial de football-data.org (ej: 2014, 2021, 2001) */
    @Id
    @Column(name = "id", nullable = false)
    private int id;

    /** Código corto: "PD", "PL", "CL" */
    @Column(name = "code", length = 10, nullable = false, unique = true)
    private String code;

    /** Nombre completo: "LaLiga", "Premier League", "UEFA Champions League" */
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    /** URL del emblema/escudo de la competición */
    @Column(name = "emblem_url")
    private String emblemUrl;

    /** Equipos que participan en esta competición */
    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Team> teams = new ArrayList<>();

    /** Partidos de esta competición */
    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Match> matches = new ArrayList<>();

    /** Tabla de posiciones de esta competición */
    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Standing> standings = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructores
    // -------------------------------------------------------------------------
    public Competition() {}

    public Competition(int id, String code, String name, String emblemUrl) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.emblemUrl = emblemUrl;
    }

    // -------------------------------------------------------------------------
    // Getters y Setters
    // -------------------------------------------------------------------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmblemUrl() { return emblemUrl; }
    public void setEmblemUrl(String emblemUrl) { this.emblemUrl = emblemUrl; }

    public List<Team> getTeams() { return teams; }
    public void setTeams(List<Team> teams) { this.teams = teams; }

    public List<Match> getMatches() { return matches; }
    public void setMatches(List<Match> matches) { this.matches = matches; }

    public List<Standing> getStandings() { return standings; }
    public void setStandings(List<Standing> standings) { this.standings = standings; }

    @Override
    public String toString() {
        return "Competition{id=" + id + ", code='" + code + "', name='" + name + "'}";
    }
}
