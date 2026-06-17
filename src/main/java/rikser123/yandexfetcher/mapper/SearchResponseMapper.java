package rikser123.yandexfetcher.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import rikser123.yandexfetcher.dto.response.SearchResponseDto;
import rikser123.yandexfetcher.dto.response.YandexResponseXMLData;
import rikser123.yandexfetcher.repository.entity.SearchResponse;
import rikser123.yandexfetcher.repository.entity.SearchResponseStatus;

import java.util.Collection;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface SearchResponseMapper {
  @Mapping(source = "passages", target = "passages", ignore = true)
  @Mapping(source = "id", target = "id", ignore = true)
  SearchResponse mapFromDto(YandexResponseXMLData.Doc dto);

  SearchResponseDto mapToDto(SearchResponse searchResponse);

  @AfterMapping
  default void afterMappingDto(YandexResponseXMLData.Doc dto, @MappingTarget SearchResponse entity) {
    entity.setStatus(SearchResponseStatus.CREATED);
    var passages = Optional.ofNullable(dto.getPassages())
      .map(YandexResponseXMLData.Passages::getPassages)
      .stream()
      .flatMap(Collection::stream)
      .map(YandexResponseXMLData.Passage::getText)
      .toList();

    var passagesText = String.join(";", passages);
    entity.setPassages(passagesText);
  }
}
