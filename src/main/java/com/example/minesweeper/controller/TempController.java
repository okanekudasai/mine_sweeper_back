package com.example.minesweeper.controller;

import com.example.minesweeper.dto.CodeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;

@RestController
@CrossOrigin("*")
public class TempController {
    ObjectMapper mapper = new ObjectMapper();
    JsonParser parser = new JsonParser();

    @Value("${leet.secret.client_id}")
    private String clientId;
    @Value("${leet.secret.client_secret}")
    private String clientSecret;
    @GetMapping("/codeToToken/{code}")
    String codeToToken(@PathVariable("code") String code) {
        WebClient webClient = WebClient.builder().build();
        String url = "https://github.com/login/oauth/access_token";
        CodeDto request_body = CodeDto.builder()
                .client_id(clientId)
                .client_secret(clientSecret)
                .code(code).build();

        String res = webClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .bodyValue(request_body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        String [] r = res.split("[=&]");
        if (r[0].equals("access_token")) {
            return r[1];
        }
        return "error";
    }
    @GetMapping("/BOJSubmit/{username}/{pNum}")
    HashMap<String, String> BOJSubmit(@PathVariable("username") String username, @PathVariable("pNum") String p_num) {
        WebClient webClient = WebClient.builder().build();

        String url = "https://www.acmicpc.net/status?user_id="+username+"&problem_id=+"+p_num;
        String html = webClient.get().uri(url).retrieve().bodyToMono(String.class).block();

        HashMap<String, String> dto = new HashMap<>();
        Document document = Jsoup.parse(html);
        dto.put("id", document.select("td").first().text());
        dto.put("result", document.select("td.result").first().text());
        dto.put("memory", document.select("td.memory").first().text());
        dto.put("time", document.select("td.time").first().text());

        return dto;
    }
}
