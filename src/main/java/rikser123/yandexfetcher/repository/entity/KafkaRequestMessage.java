package rikser123.yandexfetcher.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import rikser123.yandexfetcher.dto.request.KafkaMessageRequestResultDto;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "kafka_request_message")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class KafkaRequestMessage {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", name = "dto", nullable = false)
  private KafkaMessageRequestResultDto dto;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private KafkaEntityStatus status;

  @Column(name = "created", nullable = false, updatable = false)
  @CreationTimestamp
  private Instant created;

  @Column(name = "updated", insertable = false)
  @UpdateTimestamp
  private Instant updated;
}
