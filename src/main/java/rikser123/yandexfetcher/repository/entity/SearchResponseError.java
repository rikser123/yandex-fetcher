package rikser123.yandexfetcher.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "search_response_error")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SearchResponseError {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @JoinColumn(name = "search_response_error_id")
  @ManyToOne
  private SearchResponse searchResponse;

  @Column(name = "code", length = 100)
  private String code;

  @Column(name = "message", nullable = false)
  private String message;

  @Column(name = "created", updatable = false)
  @CreationTimestamp
  private Instant created;

  @Column(name = "updated", insertable = false)
  @CreationTimestamp
  private Instant updated;
}
