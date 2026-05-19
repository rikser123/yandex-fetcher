package rikser123.yandexfetcher.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import rikser123.yandexfetcher.dto.YandexResponseXMLData;
import rikser123.yandexfetcher.repository.entity.RequestResult;
import rikser123.yandexfetcher.repository.entity.RequestResultStatus;

import java.util.Collections;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface RequestResultMapper {
  @Mapping(source = "passages", target = "passages", ignore = true)
  @Mapping(source = "id", target = "id", ignore = true)
  RequestResult mapFromDto(YandexResponseXMLData.Doc dto);

  @AfterMapping
  default void afterMappingDto(YandexResponseXMLData.Doc dto, @MappingTarget RequestResult entity) {
    entity.setStatus(RequestResultStatus.CREATED);
    var passages = Optional.ofNullable(dto.getPassages())
      .map(YandexResponseXMLData.Passages::getPassages)
      .orElse(Collections.emptyList());

    var passagesText = String.join(";", passages);
    entity.setPassages(passagesText);
  }
}
