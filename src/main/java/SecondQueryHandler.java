import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class SecondQueryHandler {

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket;

    public SecondQueryHandler(ObjectInputStream in, ObjectOutputStream out, Socket socket) {
        this.in = in;
        this.out = out;
        this.socket = socket;
        handler();
    }

    private void handler() {
        try {
            SQLConnector sqlConnector = GlobalObjects.getSqlConnector();

            while (!socket.isClosed()) {
                KV kv = (KV) in.readObject();

                if (kv.getSecondQuery() == SecondQuery.GET_ALL_NOTES) {

                } else if (kv.getSecondQuery() == SecondQuery.SYNC_NOTES) {
                    sqlConnector.syncNotes(kv.getUsername(), (ArrayList<Note>) kv.getValue());

                    out.writeUTF(kv.getKey());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
