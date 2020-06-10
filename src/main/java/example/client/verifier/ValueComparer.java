package example.client.verifier;

public interface ValueComparer {

  default boolean compare(Object value1, Object value2) {
    return value1.equals(value2);
  }
}
