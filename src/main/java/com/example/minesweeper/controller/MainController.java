package com.example.minesweeper.controller;

import com.example.minesweeper.handler.SocketHandler;
import com.example.minesweeper.others.BoardUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
    BoardUnit [] [] getMineBoard() {
        return socket_handler.getMine_board();
    }

}
