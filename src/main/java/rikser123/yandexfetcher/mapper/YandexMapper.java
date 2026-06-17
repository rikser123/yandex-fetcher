package rikser123.yandexfetcher.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import rikser123.yandexfetcher.dto.request.YandexQueryDto;
import rikser123.yandexfetcher.dto.request.YandexSearchQueryDto;

@Mapper(componentModel = "spring")
public interface YandexMapper {
  @Mapping(source = "queryText", target = "query.queryText")
  @Mapping(source = "familyMode", target = "query.familyMode")
  @Mapping(source = "groupsOnPage", target = "groupSpec.groupsOnPage")
  @Mapping(source = "sortMode", target = "sortSpec.sortMode")
  @Mapping(source = "sortOrder", target = "sortSpec.sortOrder")
  YandexQueryDto mapToRequestDto(YandexSearchQueryDto dto);

  @AfterMapping
  default void afterMapToRequestDto(YandexSearchQueryDto dto, @MappingTarget YandexQueryDto requestDto) {
    requestDto.setResponseFormat(YandexQueryDto.ResponseFormat.FORMAT_XML);
    requestDto.getQuery().setSearchType(YandexQueryDto.SearchType.SEARCH_TYPE_RU);
  }
}
