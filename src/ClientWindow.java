import transmissionControl.Connection;
import transmissionControl.ConnectionListener;
import org.apache.log4j.Logger;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Scanner;

public class ClientWindow extends JFrame implements ActionListener, ConnectionListener {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 695;

    private JTextArea chat = new JTextArea();
    private JTextField nickname = new JTextField();
    private JTextField message = new JTextField();
    private Connection connection;

    private ClientWindow(String IP_addr, int port){

        Font font = new Font(Font.MONOSPACED, Font.BOLD, 16);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH,HEIGHT);
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        nickname.setFont(font);
        nickname.setPreferredSize(new Dimension(WIDTH-50, 30));
        try {
            nickname.setText(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        add(nickname);

        chat.setEditable(false);
        chat.setLineWrap(true);
        chat.setFont(font);

        JScrollPane scrlChat = new JScrollPane();
        scrlChat.setEnabled(false);
        scrlChat.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrlChat.setVisible(true);
        scrlChat.setPreferredSize(new Dimension(WIDTH-50,540));
        add(scrlChat);
        scrlChat.setViewportView(chat);

        message.addActionListener(this);
        message.setFont(font);
        message.setPreferredSize(new Dimension(WIDTH-50, 30));
        add(message);

        setVisible(true);
        try {
            connection = new Connection(this, IP_addr, port);
        } catch (IOException e) {
            printMsg("Connection exception: " + e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = message.getText();
        if (msg.equals("")) return;
        message.setText(null);
        connection.sendMsg(nickname.getText() + ": " + msg);
    }

    @Override
    public void connect(Connection connection) {
        printMsg("Connection is ready...");
    }

    @Override
    public void recv(Connection connection, String value) {
        //текущее время формата "yyyy-MM-dd HH:mm:ss.SSS"
        printMsg(Instant.now().toString().substring(11).replace("T", " ").replace("Z", "") + " " + value);
    }

    @Override
    public void shutdown(Connection connection) {
        printMsg("Disconnect");
    }

    @Override
    public void onException(Connection connection, Exception e) {
        printMsg("Connection exception: " + e);
        Logger.getLogger(ClientWindow.class).error(e);
    }

    private synchronized void printMsg(String msg){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chat.append(msg + "\n");
                chat.setCaretPosition(chat.getDocument().getLength());
            }
        });
    }

    public static void main(String[] args){

        Scanner in = new Scanner(System.in);
        System.out.println("Enter server IP");
        System.out.println("Format: xxx.xxx.xxx.xxx");
        String IP = in.next();
        System.out.println("Enter server port number");
        int port = in.nextInt();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow(IP, port);
            }
        });
    }
}
