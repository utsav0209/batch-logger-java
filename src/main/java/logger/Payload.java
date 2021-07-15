package logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Payload {

  @JsonProperty("user_id")
  private Long userId;
  private Double total;
  private String title;
  private Meta meta;
  private Boolean completed;

}

@Getter
@Setter
class Meta {

  private List<Login> logins;
  @JsonProperty("phone_numbers")
  private PhoneNumbers phoneNumbers;

}

@Getter
@Setter
class PhoneNumbers {

  private String home;
  private String mobile;
}

@Getter
@Setter
class Login {

  private String ip;
  private String time;

}
