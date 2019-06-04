package transmissionControl;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class Connection {

    private final Socket socket;
    private final Thread rxThread;
    private final ConnectionListener eventListener;
    private final BufferedReader in;
    private final BufferedWriter out;

    public Connection(ConnectionListener eventListener, String IP_addr, int port) throws IOException{
        this(eventListener, new Socket(IP_addr, port));
    }

    public Connection(ConnectionListener eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.connect(Connection.this);
                    while (!rxThread.isInterrupted()) {
                        eventListener.recv(Connection.this, in.readLine());
                    }
                } catch (IOException e) {
                    eventListener.onException(Connection.this, e);
                } finally {
                    eventListener.shutdown(Connection.this);
                    try {
                        socket.close();
                        in.close();
                        out.close();
                    } catch (IOException e) {
                       Logger.getLogger(Connection.class).error(e);
                    }
                }
            }
        });
        rxThread.start();
    }

    public synchronized void sendMsg(String value){
        try {
            out.write(value + "\r\n");//запись в буфер, символы конца строки
            out.flush();//сброс буфера и отправка
        } catch (IOException e) {
            eventListener.onException(Connection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect(){
        rxThread.interrupt();
    }

    @Override
    public String toString(){
        return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }
}
