package ni.edu.uam.proyectofinalivsemestre.model;

import jakarta.persistence.*;

/**
 * ENTIDAD 3: Player
 * Representa un jugador perteneciente a un equipo de las 3 ligas.
 * Almacena datos básicos para las alineaciones en la Vista de Detalle de Partido.
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Nombre completo del jugador */
    @Column(name = "name", length = 120, nullable = false)
    private String name;

    /** Posición en campo: "Goalkeeper", "Defender", "Midfielder", "Forward" */
    @Column(name = "position", length = 30)
    private String position;

    /** Número de camiseta */
    @Column(name = "shirt_number")
    private Integer shirtNumber;

    /** Nacionalidad del jugador */
    @Column(name = "nationality", length = 60)
    private String nationality;

    /** Equipo al que pertenece (FK → teams) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // -------------------------------------------------------------------------
    // Constructores
    // -------------------------------------------------------------------------
    public Player() {}

    public Player(String name, String position, Integer shirtNumber, String nationality, Team team) {
        this.name = name;
        this.position = position;
        this.shirtNumber = shirtNumber;
        this.nationality = nationality;
        this.team = team;
    }

    // -------------------------------------------------------------------------
    // Getters y Setters
    // -------------------------------------------------------------------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public Integer getShirtNumber() { return shirtNumber; }
    public void setShirtNumber(Integer shirtNumber) { this.shirtNumber = shirtNumber; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    @Override
    public String toString() {
        return "Player{id=" + id + ", name='" + name
                + "', position='" + position + "', shirt=" + shirtNumber + "}";
    }
}
