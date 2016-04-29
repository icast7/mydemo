package com.network.nio.async.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by icastillejos on 4/24/16.
 */
public class NIOServer {
    public static final int PORT = 4000;
    public static void main(String[] args) throws IOException {

        System.out.println("Listening for connections on port "+ NIOServer.PORT);

        byte[] rotation = new byte[95*2];

        for (byte i = ' '; i <= '~'; i++ ) {
            rotation[i - ' '] = i;
            rotation[i + 95 - ' '] = i;
        }

        ServerSocketChannel serverChannel;
        Selector selector;

        try {
            serverChannel = ServerSocketChannel.open();
            ServerSocket ss = serverChannel.socket();
            InetSocketAddress address = new InetSocketAddress("localhost", NIOServer.PORT);
            ss.bind(address);
            serverChannel.configureBlocking(false);
            selector =  Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }


        while(true) {
            try {
                selector.select();
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }

            Set<SelectionKey> readKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readKeys.iterator();
            while(iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                    try
                    {
                        if (key.isAcceptable()) {
                            ServerSocketChannel server = (ServerSocketChannel) key.channel();
                            SocketChannel client = server.accept();
                            System.out.println("Accept connection from " + client);
                            client.configureBlocking(false);

                            SelectionKey key2 = client.register(selector, SelectionKey.OP_WRITE);
                            ByteBuffer buffer = ByteBuffer.allocate(74);
                            buffer.put(rotation, 0, 72);
                            buffer.put((byte) '\r');
                            buffer.put((byte) '\n');
                            buffer.flip();
                            key2.attach(buffer);

                        } else if (key.isWritable()) {

                            SocketChannel client = (SocketChannel) key.channel();

                            ByteBuffer buffer = (ByteBuffer) key.attachment();

                            if (!buffer.hasRemaining()) {
                                // Refill buffer
                                buffer.rewind();
                                // Get the old first character
                                int first = buffer.get();
                                // Get ready to change the data in the buffer
                                buffer.rewind();
                                int position = first - ' ' + 1;
                                buffer.put(rotation, position, 72);
                                buffer.put((byte) '\r');
                                buffer.put((byte) '\n');
                                buffer.flip();
                            }
                            client.write(buffer);
                        }
                    } catch (IOException ex) {
                        key.cancel();
                        try {
                            key.channel().close();
                        }
                        catch (IOException cex) {}
                    }
            }
        }
    }
}
