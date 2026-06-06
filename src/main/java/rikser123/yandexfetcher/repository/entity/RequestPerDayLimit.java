package rikser123.yandexfetcher.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "request_per_day_limit")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RequestPerDayLimit {
  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "start_time", nullable = false)
  private Instant startTime;

  @Column(name = "end_time", nullable = false)
  private Instant endTime;

  @Column(name = "request_count", nullable = false)
  private Integer requestCount;

  @Column(name = "request_limit", nullable = false)
  private Integer requestLimit;

  @Column(name = "created", updatable = false)
  @CreationTimestamp
  private Instant created;

  @Column(name = "updated", insertable = false)
  @UpdateTimestamp
  private Instant updated;
}
