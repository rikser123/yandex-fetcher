package rikser123.yandexfetcher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YandexResponseDto {
  private String id;
  private String description;
  private String createdAt;
  private String createdBy;
  private String modifiedAt;
  private boolean done;
  private Object metadata;
  private Error error;
  private Response response;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  private static class Error {
    private Code code;
    private String message;
    private List<Object> details;
  }

  @AllArgsConstructor
  private enum Code {
    OK(0),
    CANCELLED(1),
    UNKNOWN(2),
    INVALID_ARGUMENT(3),
    DEADLINE_EXCEEDED(4),
    NOT_FOUND(5),
    ALREADY_EXISTS(6),
    PERMISSION_DENIED(7),
    UNAUTHENTICATED(16),
    RESOURCE_EXHAUSTED(8),
    FAILED_PRECONDITION(9),
    ABORTED(10),
    OUT_OF_RANGE(11),
    UNIMPLEMENTED(12),
    INTERNAL(13),
    UNAVAILABLE(14),
    DATA_LOSS(15);

    private int code;
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  private static class Response {
    private String rawData;
  }
}
