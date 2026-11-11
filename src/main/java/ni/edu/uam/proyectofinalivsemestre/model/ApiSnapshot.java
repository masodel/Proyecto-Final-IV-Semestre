package ni.edu.uam.proyectofinalivsemestre.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Respuesta guardada para detalle y clasificación por temporada, sin credenciales. */
@Entity
@Table(name = "api_snapshots")
public class ApiSnapshot {
    @Id @Column(length = 100) private String id;
    @Column(nullable = false, columnDefinition = "text") private String payload;
    @Column(nullable = false) private LocalDateTime updatedAt;
    protected ApiSnapshot() {}
    public ApiSnapshot(String id, String payload) {
        this.id = id; this.payload = payload; this.updatedAt = LocalDateTime.now();
    }
    public String getPayload() { return payload; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
