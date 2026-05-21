package rikser123.yandexfetcher.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import rikser123.yandexfetcher.dto.YandexRequestDto;
import rikser123.yandexfetcher.dto.YandexSearchRequestDto;

@Mapper(componentModel = "spring")
public interface YandexMapper {
  @Mapping(source = "queryText", target = "query.queryText")
  @Mapping(source = "familyMode", target = "query.familyMode")
  @Mapping(source = "groupsOnPage", target = "groupSpec.groupsOnPage")
  @Mapping(source = "sortMode", target = "sortSpec.sortMode")
  @Mapping(source = "sortOrder", target = "sortSpec.sortOrder")
  YandexRequestDto mapToRequestDto(YandexSearchRequestDto dto);

  @AfterMapping
  default void afterMapToRequestDto(YandexSearchRequestDto dto, @MappingTarget YandexRequestDto requestDto) {
    requestDto.setResponseFormat(YandexRequestDto.ResponseFormat.FORMAT_XML);
    requestDto.getQuery().setSearchType(YandexRequestDto.SearchType.SEARCH_TYPE_RU);
  }
}
