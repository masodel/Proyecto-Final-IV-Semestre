package ni.edu.uam.proyectofinalivsemestre.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ENTIDAD 8: Favorite
 * Representa la relación explícita de seguimiento y preferencia del usuario
 * hacia un equipo de fútbol. Permite almacenar notas o fecha de adición.
 *
 * Módulo 1 — Integrante 1 (Donald)
 */
@Entity
@Table(
    name = "favorites",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_favorite_team", columnNames = {"team_id"})
    }
)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Equipo marcado como favorito (FK → teams) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /** Nota personalizada del usuario (ej: "Mi club de la infancia") */
    @Column(name = "notes", length = 255)
    private String notes;

    /** Fecha y hora en la que se marcó como favorito */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // -------------------------------------------------------------------------
    // Constructores
    // -------------------------------------------------------------------------
    public Favorite() {}

    public Favorite(Team team, String notes) {
        this.team = team;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Getters y Setters
    // -------------------------------------------------------------------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        String teamName = (team != null) ? team.getName() : "Equipo";
        return String.format("Favorite⭐ [id=%d, team=%s, agregado=%s]", id, teamName, createdAt);
    }
}
