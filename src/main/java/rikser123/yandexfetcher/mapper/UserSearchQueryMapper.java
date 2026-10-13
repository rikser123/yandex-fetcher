package rikser123.yandexfetcher.mapper;

import lombok.Setter;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import rikser123.yandexfetcher.dto.request.YandexSearchQueryDto;
import rikser123.yandexfetcher.dto.response.UserSearchQueryDto;
import rikser123.yandexfetcher.repository.entity.FamilyMode;
import rikser123.yandexfetcher.repository.entity.UserSearchQuery;

import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class UserSearchQueryMapper {
  @Setter(onMethod = @__({@Autowired}))
  private SearchResponseMapper searchResponseMapper;

  @Mapping(target = "responses", source = "responses", ignore = true)
  public abstract UserSearchQueryDto mapToDto(UserSearchQuery entity);

  @AfterMapping
  void afterMapDto(UserSearchQuery entity, @MappingTarget UserSearchQueryDto dto) {
    var responses = entity.getResponses();
    var responsesDto = responses.stream().map(searchResponseMapper::mapToDto).collect(Collectors.toSet());
    dto.setResponses(responsesDto);
  }

  public abstract UserSearchQuery mapToEntity(YandexSearchQueryDto dto);

  @AfterMapping
  void afterMappingEntity(@MappingTarget UserSearchQuery entity, YandexSearchQueryDto dto) {
    if (Objects.isNull(dto.getFamilyMode())) {
      entity.setFamilyMode(FamilyMode.FAMILY_MODE_MODERATE);
    }
  }
}
