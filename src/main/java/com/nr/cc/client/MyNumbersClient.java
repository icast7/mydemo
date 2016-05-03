package com.nr.cc.client;

import com.nr.cc.server.MyNumbersServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;

/**
 * Created by icastillejos on 4/28/16.
 */
public class MyNumbersClient {

    public static void main(String[] args) {
        MyNumbersClient myIntegerClient = new MyNumbersClient();
        myIntegerClient.runClientWrite();
    }

    void runClientWrite () {

        try {
            SocketAddress address = new InetSocketAddress(MyNumbersServer.HOST, MyNumbersServer.PORT);
            SocketChannel client = SocketChannel.open(address);

            System.out.println("Client started...");

            List<String> msgList = Arrays.asList("222333444555000000111","A11100000001");

            for (String aaa : msgList){
                String formatted = aaa + MyNumbersServer.SERVER_LINE_SEPARATOR;
                byte[] msg = String.valueOf(formatted).getBytes();
                ByteBuffer buffer1 = ByteBuffer.wrap(msg);
                client.write(buffer1);
                buffer1.clear();
                System.out.println(formatted);
                Thread.sleep(10);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
