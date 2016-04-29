package com.network.nio.async.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Created by icastillejos on 4/28/16.
 */
public class MyHttpServer {
    private ByteBuffer contentBuffer;
    private int port = 4000;


    public MyHttpServer(ByteBuffer data, String encoding, String MIMEType, int port) {
        this.port = port;
        String header = "HTTP/1.0 200 OK\r\n" + "Server: NonblockingSingleFileHTTPServer\r\n" + "Content-length: " + data.limit() + "Content-type: " + MIMEType + "\r\n\r\n";
        byte[] headerData = header.getBytes(Charset.forName("US-ASCII"));

        ByteBuffer buffer = ByteBuffer.allocate(data.limit() + headerData.length);
        buffer.put(headerData);
        buffer.put(data);
        buffer.flip();
        this.contentBuffer = buffer;
    }

    public void run() throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverChannel.socket();
        Selector selector = Selector.open();
        InetSocketAddress address = new InetSocketAddress("localhost", 4000);
        serverSocket.bind(address);
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();
                try {
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel channel = server.accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        if (buffer.hasRemaining()) {
                            channel.write(buffer);
                        } else {
                            channel.close();
                        }
                    } else if (key.isReadable()) {
                        //Don't try to parse the HTTP header
                        //Just read something...
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
                        channel.read(byteBuffer);
                        //Switch channel to write-only mode
                        key.interestOps(SelectionKey.OP_WRITE);
                        key.attach(contentBuffer.duplicate());

                    }

                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException ioex) {

                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        String filePath = "/Users/icaste/gitext/mydemo/pom.xml";
        String contentType = URLConnection.getFileNameMap().getContentTypeFor(filePath);
        Path file = FileSystems.getDefault().getPath(filePath);

        String encoding = "UTF-8";
        int port = 4000;

        try {
            byte[] data = Files.readAllBytes(file);
            ByteBuffer input = ByteBuffer.wrap(data);
            MyHttpServer httpServer = new MyHttpServer(input, encoding, contentType, port);
            httpServer.run();
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
