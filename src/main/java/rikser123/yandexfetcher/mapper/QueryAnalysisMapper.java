package rikser123.yandexfetcher.mapper;

import org.mapstruct.Mapper;
import rikser123.yandexfetcher.dto.QueryAnalysisDto;
import rikser123.yandexfetcher.repository.entity.QueryAnalysis;

@Mapper(componentModel = "spring")
public interface QueryAnalysisMapper {
  QueryAnalysis mapToEntity(QueryAnalysisDto dto);
}
