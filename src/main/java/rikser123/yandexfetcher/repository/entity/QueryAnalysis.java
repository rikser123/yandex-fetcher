package rikser123.yandexfetcher.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "query_analysis")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class QueryAnalysis {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "analysis", nullable = false)
  private String analysis;

  @ManyToOne
  @JoinColumn(name = "query_id", referencedColumnName = "id")
  private UserSearchQuery userSearchQuery;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private QueryAnalysisStatus status;

  @CreationTimestamp
  @Column(name = "created", updatable = false)
  private Instant created;

  @UpdateTimestamp
  @Column(name = "updated", insertable = false)
  private Instant updated;
}
