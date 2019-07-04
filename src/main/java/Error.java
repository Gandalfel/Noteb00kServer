import java.io.Serializable;

public enum Error implements Serializable {
    DONE,
    ERROR,
    ACCOUNT_DOES_NOT_EXIST,
    LOGIN_OR_PASSWORD_INCORRECT,
    NAME_IS_BUSY
}
