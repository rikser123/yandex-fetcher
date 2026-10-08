package rikser123.yandexfetcher.service;

import org.springframework.stereotype.Service;
import rikser123.bundle.repository.entity.OutboxMessageStatus;
import rikser123.bundle.service.OutboxMessageService;
import rikser123.bundle.service.StatusMatrix;
import rikser123.yandexfetcher.dto.request.MessageSearchResponseDto;
import rikser123.yandexfetcher.repository.SearchResponseMessageRepository;
import rikser123.yandexfetcher.repository.entity.SearchResponse;
import rikser123.yandexfetcher.repository.entity.SearchResponseMessage;
import rikser123.yandexfetcher.repository.entity.UserSearchQuery;

import java.util.List;

@Service
public class SearchResponseOutboxService extends OutboxMessageService<SearchResponseMessage> {
  public SearchResponseOutboxService(
    SearchResponseMessageRepository kafkaRequestMessageRepository,
    StatusMatrix<OutboxMessageStatus> outboxStatusMatrix)
  {
    super(kafkaRequestMessageRepository, outboxStatusMatrix);
  }

  public MessageSearchResponseDto createSearchResponseMessage(UserSearchQuery userSearchQuery, List<SearchResponse> responses) {
    var kafkaDto = new MessageSearchResponseDto();
    kafkaDto.setSearchQueryId(userSearchQuery.getId());
    kafkaDto.setUserId(userSearchQuery.getUserId());
    kafkaDto.setQueryText(userSearchQuery.getQueryText());
    kafkaDto.setSearchMode(userSearchQuery.getSearchMode());

    responses.forEach(searchResponse -> {
      var kafkaDtoResponse = new MessageSearchResponseDto.SearchResponse();
      kafkaDtoResponse.setDomain(searchResponse.getDomain());
      kafkaDtoResponse.setSearchResponseId(searchResponse.getId());
      kafkaDtoResponse.setUrl(searchResponse.getUrl());
      kafkaDtoResponse.setPassages(searchResponse.getPassages());
      kafkaDto.getSearchResponses().add(kafkaDtoResponse);
    });

    var kafkaRequestMessage = new SearchResponseMessage();
    kafkaRequestMessage.setDto(kafkaDto);
    kafkaRequestMessage.setStatus(OutboxMessageStatus.CREATED);
    save(kafkaRequestMessage);

    return kafkaDto;
  }
}
