package com.network.nio.async.client;

import com.network.nio.async.server.NIOServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Created by icastillejos on 4/24/16.
 */
public class NIOClient {
    public static void main(String[] args) {
        try {
            SocketAddress address = new InetSocketAddress("localhost", NIOServer.PORT);
            SocketChannel client = SocketChannel.open(address);
            ByteBuffer byteBuffer = ByteBuffer.allocate(74);

            WritableByteChannel out = Channels.newChannel(System.out);

            while (client.read(byteBuffer) != -1) {
                byteBuffer.flip();
                out.write(byteBuffer);
                byteBuffer.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
