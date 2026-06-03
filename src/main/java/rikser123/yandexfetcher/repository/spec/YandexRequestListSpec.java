package rikser123.yandexfetcher.repository.spec;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import rikser123.yandexfetcher.dto.request.YandexRequestListDto;
import rikser123.yandexfetcher.repository.entity.Request;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class YandexRequestListSpec implements Specification<Request> {
  private final YandexRequestListDto filter;
  private final UUID userId;

  @Override
  public Predicate toPredicate(Root<Request> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    var predicates = new ArrayList<Predicate>();

    root.fetch("requestResults", JoinType.LEFT);

    if (StringUtils.isNotEmpty(filter.getQueryText())) {
      predicates.add(
        cb.like(cb.lower(root.get("queryText")),
          "%" + filter.getQueryText().toLowerCase() + "%")
      );
    }

    if (!Objects.isNull(filter.getStatus())) {
      predicates.add(cb.equal(root.get("status"), filter.getStatus()));
    }

    if (!Objects.isNull(filter.getFamilyMode())) {
      predicates.add(cb.equal(root.get("familyMode"), filter.getFamilyMode()));
    }

    if (!Objects.isNull(filter.getGroupsOnPage())) {
      predicates.add(cb.equal(root.get("groupsOnPage"), filter.getGroupsOnPage()));
    }

    if (!Objects.isNull(filter.getDateFrom()) && !Objects.isNull(filter.getDateTo())) {
      predicates.add(
        cb.between(root.get("created"), filter.getDateFrom(), filter.getDateTo())
      );
    } else if (!Objects.isNull(filter.getDateFrom())) {
      predicates.add(cb.greaterThanOrEqualTo(root.get("created"), filter.getDateFrom()));
    } else if (!Objects.isNull(filter.getDateTo())) {
      predicates.add(cb.lessThanOrEqualTo(root.get("created"), filter.getDateTo()));
    }

    predicates.add(cb.equal(root.get("userId"), userId));

    return cb.and(predicates.toArray(Predicate[]::new));
  }
}
