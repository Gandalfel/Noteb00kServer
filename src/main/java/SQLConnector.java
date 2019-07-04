import java.sql.*;
import java.util.ArrayList;

import static java.util.Objects.isNull;

public class SQLConnector {



    public Error signIn(String username, String password) {
        ResultSet resultSet;
        try {
            String sql = "SELECT username, password FROM users WHERE username LIKE '" + username + "';";
            Connector connector = new Connector();
            Statement statement = connector.connect().createStatement();
            resultSet = statement.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return Error.ACCOUNT_DOES_NOT_EXIST;
        }

        try {
            if (resultSet.getString(1).equals(username) &&
            resultSet.getString(2).equals(password)) {
                return Error.DONE;
            } else {
                return Error.LOGIN_OR_PASSWORD_INCORRECT;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Error.ERROR;
    }

    public Error signUp(String username, String password) {
        try {
            String sql = "SELECT username FROM users WHERE username LIKE '" + username + "';";
            Connector connector = new Connector();
            Statement statement = connector.connect().createStatement();
            statement.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return Error.NAME_IS_BUSY;
        }

        try {
            String sql = "CREATE TABLE " + username + " (title text, content text, tag text);";
            Connector connector = new Connector();
            Statement statement = connector.connect().createStatement();
            statement.executeUpdate(sql);
            connector.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
            return Error.ERROR;
        }

        try {
            synchronized (Pen.getPen()) {
                String sql = "INSERT INTO users(username, password, table_for_notes) VALUES(?,?,?);";

                Connector connector = new Connector();

                PreparedStatement ps = connector.connect().prepareStatement(sql);

                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, username);
                ps.executeUpdate();

                return Error.DONE;

            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Error.ERROR;
        }
    }

    public Error syncNotes(String username, ArrayList<Note> fromUser) {
        synchronized (Pen.getPen()) {
            ArrayList<Note> currentNotes = getAllNotes(username);
            ArrayList<Note> updatedNotes = new ArrayList<>();

            try {

                for (int i = 0; i <= currentNotes.size(); i++) {

                    /**
                     * Situation when user created a new note.
                     */
                    if (getNoteById(currentNotes, fromUser.get(i).id) == null) {

                        updatedNotes.add(getNoteById(fromUser, fromUser.get(i).id));
                    }

                    /**
                     * Situation when user deleted the note.
                     */
                    else if (getNoteById(fromUser, currentNotes.get(i).id) == null) {

                        //do nothing
                    }

                    /**
                     * Situation when the note exist.
                     */
                    else if (getNoteById(currentNotes, fromUser.get(i).id) != null &&
                            getNoteById(fromUser, currentNotes.get(i).id) != null) {

                        /**
                         * Situation when one or more parameters (id, title , description, tag) was edited.
                         */
                        if (!getNoteById(currentNotes, fromUser.get(i).id).title.equals(fromUser.get(i).title) ||
                                !getNoteById(currentNotes, fromUser.get(i).id).description.equals(fromUser.get(i).description) ||
                                !getNoteById(currentNotes, fromUser.get(i).id).tag.equals(fromUser.get(i).tag)) {

                            updatedNotes.add(fromUser.get(i));
                        }

                        /**
                         * Situation when note didn't edit.
                         */
                        else if (getNoteById(currentNotes, fromUser.get(i).id).title.equals(fromUser.get(i).title) ||
                                getNoteById(currentNotes, fromUser.get(i).id).description.equals(fromUser.get(i).description) ||
                                getNoteById(currentNotes, fromUser.get(i).id).tag.equals(fromUser.get(i).tag)){

                            //do nothing
                        }
                    }
                }

                if (updatedNotes.size() > 0) {

                    for (Note note : updatedNotes) {

                        Connector connector = new Connector();
                        connector.connect();

                        PreparedStatement update = connector.getConn().prepareStatement(
                                "UPDATE " + username + " SET title = ?, description = ?, tag = ?"
                        );

                        PreparedStatement insert = connector.getConn().prepareStatement(
                                "INSERT INTO " + username + " SET title = ?, description = ?, tag = ?"
                        );

                        Statement checkStmt = connector.getConn().createStatement();




                        ResultSet check;

                        try {
                            check = checkStmt.executeQuery("SELECT title, description, tag FROM " + username + " WHERE id = '" + note.id + "';");

                        } catch (SQLException e) {
                            insert.setString(1, note.title);
                            insert.setString(2, note.description);
                            insert.setString(3, note.tag);
                            insert.execute();
                            continue;
                        }

                        while (check.next()) {
                            String title = check.getString(1);
                            String description = check.getString(2);
                            String tag = check.getString(3);

                            if (isNull(getNoteByTag(currentNotes, note.title))) {

                                insert.setString(1, title);
                                insert.setString(2, description);
                                insert.setString(3, tag);
                                insert.execute();

                                continue;
                            }

                            if (!note.title.equals(getNoteByTitle(currentNotes, note.title))) {

                                update.setString(1, title);
                                update.execute();
                            }

                            if (!note.description.equals(getNoteByDesc(currentNotes, note.description))) {

                                update.setString(2, description);
                                update.execute();
                            }

                            if (!note.tag.equals(getNoteByDesc(currentNotes, note.tag))) {

                                update.setString(3, tag);
                                update.execute();
                            }
                        }

                        connector.disconnect();
                    }
                }

            } catch (NullPointerException | IndexOutOfBoundsException e) {
                e.printStackTrace();
                return Error.ERROR;
            } catch (SQLException e) {
                e.printStackTrace();
                return Error.ERROR;
            }
        }

        return Error.ERROR;
    }

    private Note getNoteByTitle(ArrayList<Note> notes, String title) {

        for (Note n : notes) {
            if (n.id.equals(title)) {
                return n;
            }
        }
        return null;
    }

    private Note getNoteByDesc(ArrayList<Note> notes, String desc) {

        for (Note n : notes) {
            if (n.id.equals(desc)) {
                return n;
            }
        }
        return null;
    }

    private Note getNoteByTag(ArrayList<Note> notes, String tag) {

        for (Note n : notes) {
            if (n.id.equals(tag)) {
                return n;
            }
        }
        return null;
    }

    private Note getNoteById(ArrayList<Note> notes, String id) {

        for (Note n : notes) {
            if (n.id.equals(id)) {
                return n;
            }
        }
        return null;
    }

    public Error newNote(String title, String content, String tag, String id, String username) {
        try {
            String sql = "INSERT INTO " + username + "(title, content, tag) VALUES(?,?,?);";

            Connector connector = new Connector();

            PreparedStatement ps = connector.connect().prepareStatement(sql);

            ps.setString(1, title);
            ps.setString(2, content);
            ps.setString(3, tag);
            ps.setString(4, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return Error.ERROR;
        }
        return Error.ERROR;
    }

    public ArrayList<Note> getAllNotes(String username) {
        try {
            String sql = "SELECT * FROM " + username;
            Connector connector = new Connector();
            Statement statement = connector.connect().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            ArrayList<Note>  arrayList = new ArrayList<>();

            while (resultSet.next()) {
                arrayList.add(new Note(resultSet.getString(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4)));
            }
            return arrayList;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }




    private static class Pen {
        private static Object pen = new Object();

        public static Object getPen() {
            return pen;
        }
    }

    private class Connector {

        private Connection conn;
        private static final String URL = "jdbc:sqlite:/home/ivan/IdeaProjects/Server/src/main/resources/Database";

        public Connection connect() throws SQLException {
            conn = DriverManager.getConnection(URL);
            return conn;
        }

        public Connection getConn() {
            return conn;
        }

        public void disconnect() throws SQLException {
            conn.close();
        }
    }
}