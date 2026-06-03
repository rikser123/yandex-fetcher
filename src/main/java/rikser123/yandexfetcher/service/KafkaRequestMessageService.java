package rikser123.yandexfetcher.service;

import rikser123.bundle.exception.StatusChangeException;
import rikser123.yandexfetcher.dto.request.KafkaMessageRequestResultDto;
import rikser123.yandexfetcher.repository.entity.KafkaEntityStatus;
import rikser123.yandexfetcher.repository.entity.KafkaRequestMessage;

import java.util.List;

/**
 * Сервис для управления сообщениями Kafka в БД.
 * <p>
 * Предоставляет операции по сохранению и выборке сообщений,
 * используемых для обмена данными через Kafka.
 */
public interface KafkaRequestMessageService {

  /**
   * Сохраняет DTO сообщения в базу данных со статусом CREATED.
   *
   * @param messages данные сообщения для сохранения
   * @return сохраненная сущность с присвоенным ID и статусом CREATED
   */
  List<KafkaRequestMessage> saveAll(List<KafkaMessageRequestResultDto> messages);

  /**
   * Возвращает список сообщений с указанным статусом.
   * <p>
   * Размер возвращаемого списка ограничен настройкой {@code kafka.max-fetch-limit}
   * из конфигурации приложения.
   *
   * @param status статус сообщений для фильтрации
   * @return список сообщений (не более максимального лимита)
   */
  List<KafkaRequestMessage> findAllByStatus(KafkaEntityStatus status);

  /**
   * Изменение статуса сообщения>
   *
   * @param message    запрос в кафку
   * @param status новый статус запроса
   * @return новый запрос
   * @throws StatusChangeException если статус невозможно изменить
   */
  KafkaRequestMessage changeStatus(KafkaRequestMessage message, KafkaEntityStatus status);
}