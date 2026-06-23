package com.myTesi.aloisioUmberto.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Id;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserPreferenceDto {
    @Id
    private String id;
    private String userId;

    private UserPreferenceDto.UISettingsDto uiSettings;
    private UserPreferenceDto.MapSettingsDto mapSettings;
    private UserPreferenceDto.AnalyticsSettingsDto analyticsSettings;
    private List<String> favoriteAreaIds;


    // --- DTO Nidificati ---

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UISettingsDto {
        private String theme; // "dark", "light", "glass"
        private boolean sidebarCollapsed;
        private String language; // "it", "en"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MapSettingsDto {
        private double[] center; // [lat, lng]
        private int zoomLevel;
        private String dataMode; // "REAL_TIME" o "LATEST"
        private int refreshIntervalMinutes;
        private List<String> activeSensorTypes;
        private boolean showOverlayLayers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnalyticsSettingsDto {
        private boolean showSmoothTrend;
        private boolean highlightAnomalies;
        private String forecastWindow; // "+1h", "+12h", etc.
    }
}