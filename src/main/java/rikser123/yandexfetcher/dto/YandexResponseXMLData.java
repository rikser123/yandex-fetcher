package rikser123.yandexfetcher.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class YandexResponseXMLData {
  public Request request;
  public Response response;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request {
    private String query;
    private int page;
    private Sortby sortby;
    private int maxpassages;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Sortby {
      @JacksonXmlProperty(isAttribute = true)
      private String order;

      @JacksonXmlProperty(isAttribute = true)
      private String priority;

      @JacksonXmlProperty(localName = "value")
      private String value;

    }
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response {
    @JacksonXmlProperty(isAttribute = true)
    private String date;

    @JacksonXmlProperty(localName = "found")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Found> foundList;

    @JacksonXmlProperty(localName = "found-human")
    private String foundHuman;

    private Results results;
  }


  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Found {
    @JacksonXmlProperty(isAttribute = true)
    private String priority;

    @JacksonXmlProperty(localName = "value")
    private String value;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Results {
    private Grouping grouping;
  }


  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Grouping {
    @JacksonXmlProperty(isAttribute = true)
    private String attr;

    @JacksonXmlProperty(isAttribute = true)
    private String mode;

    @JacksonXmlProperty(isAttribute = true)
    private int groupsOnPage;

    @JacksonXmlProperty(isAttribute = true)
    private int docsInGroup;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "group")
    private List<Group> groups;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Group {
    private Categ categ;
    private int doccount;
    private Relevance relevance;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "doc")
    private List<Doc> docs;
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class Categ {
    @JacksonXmlProperty(isAttribute = true)
    private String attr;

    @JacksonXmlProperty(isAttribute = true)
    private String name;
  }


  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Relevance {
    @JacksonXmlProperty(isAttribute = true)
    private String priority;
  }


  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Doc {
    @JacksonXmlProperty(isAttribute = true)
    private String id;

    private String url;
    private String domain;
    private String title;
    private String modtime;
    private int size;
    private String charset;

    @JacksonXmlProperty(localName = "mime-type")
    private String mimeType;

    @JacksonXmlProperty(localName = "touchdown")
    private String touchdown;

    private Passages passages;
    private Properties properties;
  }


  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Passages {
    @JacksonXmlProperty(localName = "passage")
    @JacksonXmlText
    private List<String> passages;
  }


  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Properties {
    @JacksonXmlProperty(localName = "_PassagesType")
    private int passagesType;

    private String lang;

    @JacksonXmlProperty(localName = "extended-text")
    private String extendedText;
  }
}





