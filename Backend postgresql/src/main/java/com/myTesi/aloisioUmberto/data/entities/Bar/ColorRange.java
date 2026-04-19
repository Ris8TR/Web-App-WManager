package com.myTesi.aloisioUmberto.data.entities.Bar;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable // Fondamentale per essere usata come ElementCollection in ColorBar
public class ColorRange {
    private Double minValue;
    private Double maxValue;
    private String colorCode;
    private String label;
}