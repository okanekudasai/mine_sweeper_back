package com.example.minesweeper.handler;

import com.example.minesweeper.others.BoardUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class SocketHandler extends TextWebSocketHandler {
    private Logger log = LoggerFactory.getLogger(SocketHandler.class);

    // 크기는 가로30 * 세로32 지뢰수 198개로 한다.
    static int r = 30, c= 32, mine_count = 99;
    static BoardUnit[] [] mine_board;
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
        log.info("session:" + session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        log.info("누군가가 소켓에서 끊어졌어요");
        log.info("session:" + session);
        log.info("status:" + status);
    }
}
