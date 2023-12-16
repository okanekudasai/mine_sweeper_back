package com.example.minesweeper.others;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Builder
@Data
public class BoardUnit {
    boolean opened;
    boolean hasMine;
    boolean hasFlag;
    int count;
}
