package org.example.dockernetwork.Handler;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

public class MyWebSocketHandler extends TextWebSocketHandler {

    private static WebSocketSession session; // 保存连接的 session，后面发消息用

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        MyWebSocketHandler.session = session;
    }

    public static void sendMessage(String message) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
