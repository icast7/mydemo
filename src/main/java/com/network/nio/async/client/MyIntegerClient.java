package com.network.nio.async.client;

import com.network.nio.async.server.MyIntegerServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by icastillejos on 4/28/16.
 */
public class MyIntegerClient {

    public static void main(String[] args) {
        MyIntegerClient myIntegerClient = new MyIntegerClient();
        myIntegerClient.run();
    }

    void run () {
        try {
            SocketAddress address = new InetSocketAddress(MyIntegerServer.HOST, MyIntegerServer.PORT);
            SocketChannel client = SocketChannel.open(address);
            ByteBuffer  buffer = ByteBuffer.allocate(4);
            IntBuffer view = buffer.asIntBuffer();
            for (int expected = 0;; expected++){
                client.read(buffer);
                int actual = view.get();
                buffer.clear();
                view.rewind();

                if (actual != expected) {
                    System.err.println("Expected " +  expected + "; was " + actual);
                    break;
                }
                System.out.println(actual);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
