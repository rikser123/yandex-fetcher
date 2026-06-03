package rikser123.yandexfetcher.mapper;

import lombok.Setter;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import rikser123.yandexfetcher.dto.response.RequestResponseDto;
import rikser123.yandexfetcher.repository.entity.Request;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class RequestMapper {
  @Setter(onMethod = @__({@Autowired}))
  private RequestResultMapper requestResultMapper;

  @Mapping(target = "requestResults", source = "requestResults", ignore = true)
  public abstract RequestResponseDto mapToDto(Request entity);

  @AfterMapping
  void afterMapDto(Request entity, @MappingTarget RequestResponseDto dto) {
    var requestResults = entity.getRequestResults();
    var requestResultsDto = requestResults.stream().map(requestResultMapper::mapToDto).collect(Collectors.toSet());
    dto.setRequestResults(requestResultsDto);
  }
}
