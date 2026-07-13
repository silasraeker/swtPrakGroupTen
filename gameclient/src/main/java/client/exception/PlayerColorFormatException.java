package client.exception;



public class PlayerColorFormatException extends RuntimeException {

  private final String toFormat;

  public PlayerColorFormatException(String toFormat, String reason) {
    super(reason);
    this.toFormat = toFormat;
  }

  public PlayerColorFormatException(String toFormat, String reason, Throwable cause) {
    super(reason, cause);
    this.toFormat = toFormat;
  }

  public String getToFormat() {
    return toFormat;
  }

  @Override
  public String toString() {
    return super.getMessage();
  }
}