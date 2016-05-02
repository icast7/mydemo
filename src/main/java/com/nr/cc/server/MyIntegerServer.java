package com.nr.cc.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by icastillejos on 4/28/16.
 */
public class MyIntegerServer {
    public final static int PORT = 4000;
    public final static String HOST = "localhost";
    public static final char SERVER_LINE_SEPARATOR = System.lineSeparator().charAt(0);
    //Server terminate command
    public static final String TERMINATE_COMMAND = "terminate";

    //Server address
    private final static InetSocketAddress ADDRESS = new InetSocketAddress(HOST, PORT);
    //Server operations log
    private static final Logger logger = Logger.getLogger(MyIntegerServer.class.getCanonicalName());
    //Path to numbers.log file
    private final static Path NUMBERS_FILE_PATH = Paths.get("./numbers.log");
    //Length of numeric message
    private static final int MESSAGE_LENGTH = 9;
    //Valid message regex: message line contains MESSAGE_LENGTH digits
    private static final String validMessageRegex = "^\\d{" + MESSAGE_LENGTH + "}$";

    private Selector selector;

    public static void main(String[] args) throws IOException {
        MyIntegerServer server = new MyIntegerServer();
        server.runServer();
    }

    void runServer() throws IOException {
        //Clear numbers file
        Files.deleteIfExists(NUMBERS_FILE_PATH);
        Files.write(NUMBERS_FILE_PATH,"".getBytes());

        selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        serverChannel.socket().bind(ADDRESS);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Listening for connections on port" + PORT);

        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (!key.isValid()) {
                    continue;
                }
                if (key.isAcceptable()) {
                    acceptKey(key);
                }
                if (key.isReadable()) {
                    readKey(key);
                }
            }
        }
    }

    private void readKey(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();

        //Read char by char to find line separator
        ByteBuffer buffer = ByteBuffer.allocate(1);
        StringBuffer line = new StringBuffer();
        while(client.read(buffer) > 0)
        {
            buffer.flip();
            char ch = ((char) buffer.get());
            if(ch == SERVER_LINE_SEPARATOR){
                String lineString = line.toString();
                logger.log(Level.INFO, "Received  line: " + lineString);
                if (lineString.matches(validMessageRegex)) {
                    Files.write(NUMBERS_FILE_PATH, lineString.getBytes(), StandardOpenOption.APPEND);
                }

                //Create new line
                line = new StringBuffer();
            } else {
                //Append character to line
                line.append(ch);
            }
            //Clear buffer
            buffer.clear();
        }
        //Close socket channel when no more data is received
        client.close();
        key.cancel();
        return;
    }

    private void acceptKey(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();

        client.configureBlocking(false);
        System.out.println("Accepted connection from " + client);

        SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ);
        ByteBuffer buffer = ByteBuffer.allocate(10);
        clientKey.attach(buffer);
    }
}
