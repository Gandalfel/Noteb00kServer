import java.io.Serializable;

public class Note implements Serializable {

    String title;
    String description;
    String tag;
    String id;

    public Note(String title, String description, String tag, String id) {
        this.title = title;
        this.description = description;
        this.tag = tag;
        this.id = id;
    }
}
