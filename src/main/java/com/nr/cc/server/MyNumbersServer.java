package com.nr.cc.server;

import com.nr.cc.server.domain.ServerStatus;

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
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by icastillejos on 4/28/16.
 */
public class MyNumbersServer {
    public final static int PORT = 4000;
    public final static String HOST = "localhost";
    public static final char SERVER_LINE_SEPARATOR = System.lineSeparator().charAt(0);
    //Server terminate command
    public static final String TERMINATE_COMMAND = "terminate";

    //Server address
    private final static InetSocketAddress ADDRESS = new InetSocketAddress(HOST, PORT);
    //Server operations log
    private static final Logger logger = Logger.getLogger(MyNumbersServer.class.getCanonicalName());
    //Path to numbers.log file
    private final static Path NUMBERS_FILE_PATH = Paths.get("./numbers.log");
    //Length of numeric message
    private static final int MESSAGE_LENGTH = 9;
    //Valid message regex: message line contains MESSAGE_LENGTH digits
    private static final String validMessageRegex = "^\\d{" + MESSAGE_LENGTH + "}$";
    //Define status object
    volatile ServerStatus currentStatus = new ServerStatus();
    //ScheduledExecutorService used to post status updates
    private ScheduledExecutorService scheduledExecutorService;
    //Initialize set to limit concurrency issues with duplicates
    private Set<String> syncSet = Collections.synchronizedSet(new HashSet<String>());
    //Number of connections
    public static final int NUMBER_OF_CONNECTIONS = 5;
    //Keep list of channels
    private List<SocketChannel> socketList = new ArrayList<>(NUMBER_OF_CONNECTIONS);

    private Selector selector;

    public static void main(String[] args) throws IOException {
        MyNumbersServer server = new MyNumbersServer();
        server.runServer();
    }

    void runServer() throws IOException {
        //Clear numbers file
        Files.deleteIfExists(NUMBERS_FILE_PATH);
        Files.write(NUMBERS_FILE_PATH,"".getBytes());

        //Start scheduled reports
        //Set scheduled service to manage printing status to the logger
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        //Start scheduled task to print status
        scheduledExecutorService.scheduleAtFixedRate(new UpdateStatus(currentStatus, logger), 0, 10, TimeUnit.SECONDS);

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
            buffer.clear();

            if(ch == SERVER_LINE_SEPARATOR){
                String lineString = line.toString();
                //Add to set and write to file
                if (lineString.matches(validMessageRegex)) {
                    //If number is valid add it to the Set
                    if (syncSet.add(lineString)) {
                        //If the number was added write it to the numbers file
                        currentStatus.incrementTotalUniqueNumbers();
                        currentStatus.incrementUniqueNumbersSinceLastReport();
                        //TODO Implement FileChannel to allow for thread safe operations
                        Files.write(NUMBERS_FILE_PATH, lineString.getBytes(), StandardOpenOption.APPEND);
                    } else {
                        currentStatus.incrementDuplicateNumbersSinceLastReport();
                    }
                } else if (lineString.equals(TERMINATE_COMMAND)) {
                    //Close all connections and return
                    closeAllConnections();
                    return;
                } else {
                    //Invalid msg, stop reading
                    break;
                }
                //Create new line
                line = new StringBuffer();
            } else {
                if (line.length() >= MESSAGE_LENGTH) {
                    //Invalid msg, stop reading
                    break;
                }
                //Append character to line IF string is shorter than expected
                line.append(ch);
            }
        }
        //Close socket channel when no more data is received
        socketList.remove(client);
        client.close();
        key.cancel();
        return;
    }

    private void acceptKey(SelectionKey key) throws IOException {
        if (socketList.size() < NUMBER_OF_CONNECTIONS) {
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel client = server.accept();

            //Add socket channel
            socketList.add(client);

            client.configureBlocking(false);
            System.out.println(String.format("Accepted connection (%d) from %s.", socketList.size(), client));

            SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ);
            ByteBuffer buffer = ByteBuffer.allocate(10);
            clientKey.attach(buffer);
        }
    }

    /**
     * This methods closes all SocketChannel connections
     */
    private void closeAllConnections() {
        for (SocketChannel client :  socketList) {
            SelectionKey key = client.keyFor(selector);
            try {
                client.close();
            } catch (IOException ex) {
                //noop
            } finally {
                key.cancel();
            }
        }
        socketList.clear();
    }

    /**
     * This class represents a runnable that appends the server status to the logger
     */
    class UpdateStatus implements Runnable {
        private final ServerStatus serverStatus;
        private final Logger logger;
        //Log message
        private static final String logMsg = "Received %d unique numbers, %d duplicates. Unique total: %d";

        /**
         * This is the runnable constructor
         *
         * @param serverStatus ServerStatus object
         */
        public UpdateStatus(ServerStatus serverStatus, Logger logger) {
            this.serverStatus = serverStatus;
            this.logger = logger;
        }

        /**
         * The run method simply appends the current status to the log
         */
        @Override
        public void run() {
            logger.log(Level.INFO, String.format(logMsg, serverStatus.getUniqueNumbersSinceLastReport(),
                    serverStatus.getDuplicateNumbersSinceLastReport(), serverStatus.getTotalUniqueNumbers()));
            serverStatus.clearSinceLastReportCounters();
        }
    }
}

