import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class FirstQueryHandler {

    public FirstQueryHandler() {
        try {
            SQLConnector sqlConnector = GlobalObjects.getSqlConnector();

            ServerSocket serverSocket = new ServerSocket(8080);

            while (!serverSocket.isClosed()) {
                Socket client = serverSocket.accept();
                System.out.println("connected");

                ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());

                ArrayList arrayList = (ArrayList) in.readObject();

                if (arrayList.get(0) == FirstQuery.SIGN_IN) {
                    out.writeObject((sqlConnector.signIn((String) arrayList.get(1), (String) arrayList.get(2))));
                    new SecondQueryHandler(in, out, client);
                } else if (arrayList.get(0) == FirstQuery.SIGN_UP) {
                    out.writeObject(sqlConnector.signUp((String) arrayList.get(1), (String) arrayList.get(2)));
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new FirstQueryHandler();
    }
}
