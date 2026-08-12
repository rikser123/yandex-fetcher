package rikser123.yandexfetcher.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import rikser123.yandexfetcher.dto.QueryAnalysisDto;
import rikser123.yandexfetcher.repository.entity.QueryAnalysis;
import rikser123.yandexfetcher.repository.entity.QueryAnalysisStatus;

@Mapper(componentModel = "spring")
public interface QueryAnalysisMapper {
  QueryAnalysis mapToEntity(QueryAnalysisDto dto);

  @AfterMapping
  default void afterMappingEntity(@MappingTarget QueryAnalysis entity, QueryAnalysisDto dto) {
    entity.setStatus(QueryAnalysisStatus.CREATED);
  }
}
