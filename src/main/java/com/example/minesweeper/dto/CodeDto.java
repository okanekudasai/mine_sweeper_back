package com.example.minesweeper.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CodeDto {
    String client_id;
    String client_secret;
    String code;
}
