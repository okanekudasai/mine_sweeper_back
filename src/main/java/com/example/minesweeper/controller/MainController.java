package com.example.minesweeper.controller;

import com.example.minesweeper.handler.SocketHandler;
import com.example.minesweeper.others.BoardUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@CrossOrigin("*")
public class MainController {
    @Autowired
    SocketHandler socket_handler;

    private Logger log = LoggerFactory.getLogger(MainController.class);

    @GetMapping("/test")
    String test () {
        System.out.println("성공");
        return "success!!!";
    }

    @GetMapping("/mineBoard")
    HashMap<String, Object> getMineBoard() {
        HashMap<String, Object> dto = new HashMap<>();
        dto.put("board", socket_handler.getMine_board());
        dto.put("nokori", socket_handler.default_mine_count - socket_handler.getFlag_count());
        return dto;
    }

}
