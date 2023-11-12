package com.example.minesweeper.handler;

import com.example.minesweeper.others.BoardUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@Component
public class SocketHandler extends TextWebSocketHandler {
    private Logger log = LoggerFactory.getLogger(SocketHandler.class);
    ObjectMapper mapper = new ObjectMapper();
    JsonParser parser = new JsonParser();

    // 크기는 가로30 * 세로32 지뢰수 198개로 한다.
    static int r = 32, c= 60, mine_count = 396;
    static BoardUnit[] [] mine_board;

    static HashSet<WebSocketSession> webSocketSessionSet = new HashSet<>();
    SocketHandler() {
        mine_board = new BoardUnit [r] [c];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                mine_board[i][j] = BoardUnit.builder().opened(false).hasMine(false).hasFlag(false).build();
            }
        }
    }

    static boolean all_done = true;

    public BoardUnit [] [] getMine_board () {
        return mine_board;
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        log.info("누군가가 소켓에 연결되었어요!");
        log.info("session:" + session.getId());
        webSocketSessionSet.add(session);
        HashMap<String, Object> dto = new HashMap<>();
        dto.put("code", "my_session_id");
        HashMap<String, Object> value = new HashMap<>();
        dto.put("value", value);
        value.put("my_session_id", session.getId());
        session.sendMessage(new TextMessage(mapper.writeValueAsString(dto)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        JsonElement element = parser.parse(message.getPayload());
        String event = element.getAsJsonObject().get("event").getAsString();
        JsonElement data = element.getAsJsonObject().get("data").getAsJsonObject();
        if (event.equals("pong")) {
            log.info("퐁! : " + data.getAsJsonObject().get("id").getAsString());
        } else if (event.equals("new_player")) {
            String id = data.getAsJsonObject().get("id").getAsString();
            Double r = data.getAsJsonObject().get("r").getAsDouble();
            Double c = data.getAsJsonObject().get("c").getAsDouble();
            HashMap<String, Object> dto = new HashMap<>();
            dto.put("code", "new_player");
            HashMap<String, Object> value = new HashMap<>();
            dto.put("value", value);
            value.put("session_id", id);
            value.put("r", r);
            value.put("c", c);
            for (WebSocketSession wss : webSocketSessionSet) {
                if (wss.equals(session)) continue;
                wss.sendMessage(new TextMessage(mapper.writeValueAsString(dto)));
            }
        } else if (event.equals("welcome_you")) {
            String id = data.getAsJsonObject().get("id").getAsString();
            Double r = data.getAsJsonObject().get("r").getAsDouble();
            Double c = data.getAsJsonObject().get("c").getAsDouble();
            HashMap<String, Object> dto = new HashMap<>();
            dto.put("code", "take_position");
            HashMap<String, Object> value = new HashMap<>();
            dto.put("value", value);
            value.put("session_id", session.getId());
            value.put("r", r);
            value.put("c", c);
            for (WebSocketSession wss : webSocketSessionSet) {
                if (wss.getId().equals(id)) {
                    wss.sendMessage(new TextMessage(mapper.writeValueAsString(dto)));
                    break;
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        log.info("누군가가 소켓에서 끊어졌어요");
        log.info("session:" + session);
        log.info("status:" + status);
        webSocketSessionSet.remove(session);
    }
    @Scheduled(fixedDelay = 30000)
    public void ping() throws Exception {
        HashMap<String, Object> dto = new HashMap<>();
        dto.put("code", "ping");
        for (WebSocketSession wss : webSocketSessionSet) {
            wss.sendMessage(new TextMessage(mapper.writeValueAsString(dto)));
        }
    }
}
