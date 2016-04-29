package com.network.nio.async.client;

import com.network.nio.async.server.NIOServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by icastillejos on 4/24/16.
 */
public class NIOClient {
    public static void main(String[] args){
        try {
            SocketAddress address = new InetSocketAddress("localhost", NIOServer.PORT);
            SocketChannel client = SocketChannel.open(address);
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            IntBuffer view = byteBuffer.asIntBuffer();

            for (int expected =0; ;expected++){
                client.read(byteBuffer);
                int actual = view.get();
                byteBuffer.clear();
                view.rewind();
                if (actual != expected) {
                    System.err.println("Expected " + expected + "; was " + actual);
                    break;
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
