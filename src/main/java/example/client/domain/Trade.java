package example.client.domain;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

@Data
@ToString
public class Trade {

  @Id
  @NonNull
  private final String id;

  @NonNull
  private final String cusip;

  private final int shares;

  @NonNull
  private final BigDecimal price;
}