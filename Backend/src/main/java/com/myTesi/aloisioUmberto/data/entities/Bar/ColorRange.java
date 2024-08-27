package com.myTesi.aloisioUmberto.data.entities.Bar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ColorRange {
    private double min;
    private double max;
    private String color;  // Il colore in formato esadecimale (es. "#000000" per nero)
}