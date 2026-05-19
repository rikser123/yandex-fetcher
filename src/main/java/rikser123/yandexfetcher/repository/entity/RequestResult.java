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

@Table(name = "request_result")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RequestResult {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private RequestResultStatus status;

  @Column(name = "saved_copy_url")
  private String savedCopyUrl;

  @Column(name = "url", nullable = false)
  private String url;

  @Column(name = "domain", nullable = false)
  private String domain;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "mod_time")
  private Instant modTime;

  @Column(name = "size")
  private Long size;

  @Column(name = "charset")
  private String charset;

  @Column(name = "mime_type")
  private String mimeType;

  @Column(name = "passages")
  private String passages;

  @JoinColumn(name = "request_id")
  @ManyToOne
  private Request request;

  @CreationTimestamp
  @Column(name = "created", updatable = false)
  private Instant created;

  @UpdateTimestamp
  @Column(name = "updated", insertable = false)
  private Instant updated;
}
