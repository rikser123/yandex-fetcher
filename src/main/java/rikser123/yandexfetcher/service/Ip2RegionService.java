package rikser123.yandexfetcher.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lionsoul.ip2region.service.Config;
import org.lionsoul.ip2region.service.InvalidConfigException;
import org.lionsoul.ip2region.service.Ip2Region;
import org.lionsoul.ip2region.xdb.XdbException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class Ip2RegionService {
  private Ip2Region ip2Region;

  @PostConstruct
  void init() throws IOException, InvalidConfigException, XdbException {
    var config = Config.custom()
      .setCachePolicy(Config.BufferCache)
      .setXdbInputStream(Ip2RegionService.class.getResourceAsStream("/ip2region/ip2region_v4.xdb"))
      .setSearchers(15)
      .asV4();

    ip2Region = Ip2Region.create(config, null);
  }

  public String getCountryCode(String ip) {
    var parts = getRegion(ip).split("\\|");
    return parts.length >= 0 ? parts[parts.length - 1] : StringUtils.EMPTY;
  }

  private String getRegion(String ip) {
    try {
      return ip2Region.search(ip);
    } catch (Exception e) {
      return StringUtils.EMPTY;
    }
  }


  @PreDestroy
  void close() throws InterruptedException {
    if (!Objects.isNull(ip2Region)) {
      ip2Region.close();
    }
  }
}
