package ni.edu.uam.proyectofinalivsemestre.model;
import jakarta.persistence.*;

/** Favorite existente se conserva para equipos. */
@Entity
@Table(name = "match_favorites")
public class MatchFavorite {
    @Id private Integer matchId;
    @OneToOne(fetch = FetchType.LAZY) @MapsId @JoinColumn(name = "match_id")
    private Match match;
    protected MatchFavorite() {}
    public MatchFavorite(Match match) { this.match = match; }
    public Match getMatch() { return match; }
}
