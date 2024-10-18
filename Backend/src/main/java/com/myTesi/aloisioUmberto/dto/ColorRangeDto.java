package com.myTesi.aloisioUmberto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColorRangeDto {
    private double min;
    private double max;
    private String color;  // Colore in formato esadecimale
}

