package com.myTesi.aloisioUmberto.data.entities;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Id;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "UserPreference")
@Data
public class UserPreference {
    @Id
    private ObjectId id;

    @NotNull
    @Field
    private ObjectId userId;

    private UISettings uiSettings;

    private MapSettings mapSettings;

    private AnalyticsSettings analyticsSettings;

    private List<ObjectId> favoriteAreaIds;
}

@Data
class UISettings {
    private String theme; // "dark", "light", "glass"
    private boolean sidebarCollapsed;
    private String language; // "it", "en"
}

@Data
class MapSettings {
    private double[] center; // [lat, lng]
    private int zoomLevel;
    private String dataMode; // "REAL_TIME" o "LATEST"
    private int refreshIntervalMinutes; // 5, 10, 15...
    private List<String> activeSensorTypes; // ["Temperature", "Humidity", etc.]
    private boolean showOverlayLayers;
}

@Data
class AnalyticsSettings {
    private boolean showSmoothTrend;
    private boolean highlightAnomalies;
    private String forecastWindow; // "+1h", "+12h", etc.
}