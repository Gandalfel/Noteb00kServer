import java.io.Serializable;

public class KV implements Serializable {

    private String key;
    private Object value;
    private SecondQuery secondQuery;
    private String username;

    public KV(SecondQuery secondQuery, String key, Object value, String username) {
        this.key = key;
        this.value = value;
        this.secondQuery = secondQuery;
        this.username = username;
    }

    public SecondQuery getSecondQuery() {
        return secondQuery;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public String getUsername() {
        return username;
    }
}
