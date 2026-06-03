package rikser123.yandexfetcher.repository.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "request")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@NamedEntityGraph(
  name = "results",
  attributeNodes = { @NamedAttributeNode("requestResults")}
)
public class Request {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "query_text", length = 400, nullable = false)
  private String queryText;

  @Column(name = "family_mode", length = 50)
  @Enumerated(EnumType.STRING)
  private FamilyMode familyMode;

  @Column(name = "groups_on_page", length = 20)
  @Enumerated(EnumType.STRING)
  private GroupsOnPage groupsOnPage;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private RequestStatus status;

  @OneToMany(mappedBy = "request", fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
  private Set<RequestResult> requestResults = new HashSet<>();

  @UpdateTimestamp
  @Column(name = "updated", insertable = false)
  private Instant updated;

  @CreationTimestamp
  @Column(name = "created", updatable = false)
  private Instant created;
}
