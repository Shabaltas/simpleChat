package transmissionControl;

public interface ConnectionListener {

    void connect(Connection connection);
    void recv(Connection connection, String value);
    void shutdown(Connection connection);
    void onException(Connection connection, Exception e);
}
