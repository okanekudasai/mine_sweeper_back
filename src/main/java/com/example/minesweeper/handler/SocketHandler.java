package com.example.minesweeper.handler;

import com.example.minesweeper.others.BoardUnit;
import com.example.minesweeper.others.Node;
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

import java.util.*;

@Component
public class SocketHandler extends TextWebSocketHandler {
    private Logger log = LoggerFactory.getLogger(SocketHandler.class);
    ObjectMapper mapper = new ObjectMapper();
    JsonParser parser = new JsonParser();

    // 크기는 가로30 * 세로32 지뢰수 198개로 한다.
//    static int r = 32, c= 60, mine_count = 396;
    static int r = 20, c= 24, mine_count = 99;
    static BoardUnit[] [] mine_board;

    static HashSet<WebSocketSession> webSocketSessionSet = new HashSet<>();
    SocketHandler() {
        mine_board = new BoardUnit [r] [c];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                mine_board[i][j] = BoardUnit.builder().opened(false).hasMine(false).hasFlag(false).count(0).build();
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
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        JsonElement element = parser.parse(message.getPayload());
        String event = element.getAsJsonObject().get("event").getAsString();
        JsonElement data = element.getAsJsonObject().get("data").getAsJsonObject();
        if (event.equals("pong")) {
            log.info("퐁! : " + data.getAsJsonObject().get("id").getAsString());
        } else if (event.equals("connect_game")) {
            HashMap<String, Object> dto = new HashMap<>();
            dto.put("code", "my_session_id");
            HashMap<String, Object> value = new HashMap<>();
            dto.put("value", value);
            value.put("my_session_id", session.getId());
            session.sendMessage(new TextMessage(mapper.writeValueAsString(dto)));
        } else if (event.equals("new_player")) {
            String id = data.getAsJsonObject().get("id").getAsString();
            Double r = data.getAsJsonObject().get("r").getAsDouble();
            Double c = data.getAsJsonObject().get("c").getAsDouble();
            int rBlock = data.getAsJsonObject().get("rBlock").getAsInt();
            int cBlock = data.getAsJsonObject().get("cBlock").getAsInt();
            HashMap<String, Object> dto = new HashMap<>();
            dto.put("code", "new_player");
            HashMap<String, Object> value = new HashMap<>();
            dto.put("value", value);
            value.put("session_id", id);
            value.put("r", r);
            value.put("c", c);
            value.put("rBlock", rBlock);
            value.put("cBlock", cBlock);
            for (WebSocketSession wss : webSocketSessionSet) {
                if (wss.equals(session)) continue;
                wss.sendMessage(new TextMessage(mapper.writeValueAsString(dto)));
            }
        } else if (event.equals("welcome_you")) {
            String id = data.getAsJsonObject().get("id").getAsString();
            Double r = data.getAsJsonObject().get("r").getAsDouble();
            Double c = data.getAsJsonObject().get("c").getAsDouble();
            int rBlock = data.getAsJsonObject().get("rBlock").getAsInt();
            int cBlock = data.getAsJsonObject().get("cBlock").getAsInt();
            HashMap<String, Object> dto = new HashMap<>();
            dto.put("code", "take_position");
            HashMap<String, Object> value = new HashMap<>();
            dto.put("value", value);
            value.put("session_id", session.getId());
            value.put("r", r);
            value.put("c", c);
            value.put("rBlock", rBlock);
            value.put("cBlock", cBlock);
            for (WebSocketSession wss : webSocketSessionSet) {
                if (wss.getId().equals(id)) {
                    wss.sendMessage(new TextMessage(mapper.writeValueAsString(dto)));
                    break;
                }
            }
        } else if (event.equals("player_move")) {
            String id = data.getAsJsonObject().get("id").getAsString();
            Double r = data.getAsJsonObject().get("r").getAsDouble();
            Double c = data.getAsJsonObject().get("c").getAsDouble();
            int rBlock = data.getAsJsonObject().get("rBlock").getAsInt();
            int cBlock = data.getAsJsonObject().get("cBlock").getAsInt();
            HashMap<String, Object> dto = new HashMap<>();
            dto.put("code", "take_position");
            HashMap<String, Object> value = new HashMap<>();
            dto.put("value", value);
            value.put("session_id", session.getId());
            value.put("r", r);
            value.put("c", c);
            value.put("rBlock", rBlock);
            value.put("cBlock", cBlock);
            for (WebSocketSession wss : webSocketSessionSet) {
                if (wss.getId().equals(id)) continue;
                wss.sendMessage(new TextMessage(mapper.writeValueAsString(dto)));
            }
        } else if (event.equals("spade_work")) {
            String id = data.getAsJsonObject().get("id").getAsString();
            int rBlock = data.getAsJsonObject().get("rBlock").getAsInt();
            int cBlock = data.getAsJsonObject().get("cBlock").getAsInt();

            if (all_done) {
                all_done = false;

                HashSet<Integer> mine_set = new HashSet<>();
                Random random = new Random();
                while (mine_set.size() <= mine_count) {
                    int next = random.nextInt(r*c);
                    if (next / c == cBlock && next % c == rBlock) continue;
                    mine_set.add(next);
                }

                mine_board = new BoardUnit [r] [c];
                for (int i = 0; i < r; i++) {
                    for (int j = 0; j < c; j++) {
                        mine_board[i][j] = BoardUnit.builder().opened(false).hasMine(false).hasFlag(false).count(0).build();
                    }
                }
                int [] [] delta = {{-1,1},{0,1},{1,1},{1,0},{1,-1},{0,-1},{-1,-1},{-1,0}};
                for (int m : mine_set) {
                    mine_board[m / c][m % c].setHasMine(true);
                    for (int [] d : delta) {
                        int nr = m/c + d[0];
                        int nc = m%c + d[1];
                        if (nr < 0 || nr >= r || nc < 0 || nc >= c) continue;
                        mine_board[nr][nc].setCount(mine_board[nr][nc].getCount() + 1);
                    }
                }
            }
            // 한번 마인 위치를 출력해 볼게요
//            for (BoardUnit [] bu : mine_board) {
//                for (BoardUnit b : bu) {
//                    System.out.print((b.isHasMine()) ? "1 " : "0 ");
//                }
//                System.out.println();
//            }
//            System.out.println();

            // 클릭한 자리가 마인이면?
            if (mine_board[cBlock][rBlock].isHasMine()) {

            }
            // 클릭한 자리가 마인이 아니면?
            else {
                // 숫자가 적힌 칸이라면
                if (mine_board[cBlock][rBlock].getCount() > 0) {
                    mine_board[cBlock][rBlock].setOpened(true);
                // 숫자가 0이라면
                } else {
                    int [] [] delta = {{-1,1},{0,1},{1,1},{1,0},{1,-1},{0,-1},{-1,-1},{-1,0}};
//                    int [] [] delta = {{0,1},{1,0},{0,-1},{-1,0}};
                    ArrayDeque<Node> q = new ArrayDeque<>();
                    mine_board[cBlock][rBlock].setOpened(true);
                    q.offerLast(new Node(cBlock, rBlock));
                    while (!q.isEmpty()) {
                        Node node = q.pollFirst();
                        for (int [] d : delta) {
                            int next_r = node.getR() + d[0];
                            int next_c = node.getC() + d[1];
                            if (next_r < 0 || next_r >= r || next_c < 0 || next_c >= c) continue;
                            if (mine_board[next_r][next_c].isOpened()) continue;
                            if (mine_board[next_r][next_c].isHasFlag()) continue;
                            mine_board[next_r][next_c].setOpened(true);
                            if (mine_board[next_r][next_c].getCount() == 0) q.offerLast(new Node(next_r, next_c));
                        }
                    }
                }
                HashMap<String, Object> dto = new HashMap<>();
                dto.put("code", "get_board");
                HashMap<String, Object> value = new HashMap<>();
                dto.put("value", value);
                value.put("board", mine_board);
                for (WebSocketSession wss : webSocketSessionSet) {
                    wss.sendMessage(new TextMessage(mapper.writeValueAsString(dto)));
                }
            }
        } else if (event.equals("set_flag")) {
            int rBlock = data.getAsJsonObject().get("rBlock").getAsInt();
            int cBlock = data.getAsJsonObject().get("cBlock").getAsInt();
            mine_board[cBlock][rBlock].setHasFlag(!mine_board[cBlock][rBlock].isHasFlag());
            HashMap<String, Object> dto = new HashMap<>();
            dto.put("code", "get_board");
            HashMap<String, Object> value = new HashMap<>();
            dto.put("value", value);
            value.put("board", mine_board);
            for (WebSocketSession wss : webSocketSessionSet) {
                wss.sendMessage(new TextMessage(mapper.writeValueAsString(dto)));
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
        HashMap<String, Object> dto = new HashMap<>();
        dto.put("code", "player_disconnect");
        HashMap<String, Object> value = new HashMap<>();
        dto.put("value", value);
        value.put("session_id", session.getId());
        value.put("id", session.getId());
        for (WebSocketSession wss : webSocketSessionSet) {
            wss.sendMessage(new TextMessage(mapper.writeValueAsString(dto)));
        }
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
