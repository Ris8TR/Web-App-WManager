package com.myTesi.aloisioUmberto.data.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.myTesi.aloisioUmberto.JwtAuthConverter;
import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.core.modelMapper.SensorDataMapper;
import com.myTesi.aloisioUmberto.data.dao.InterestAreaRepository;
import com.myTesi.aloisioUmberto.data.dao.SensorDataRepository;
import com.myTesi.aloisioUmberto.data.dao.SensorRepository;
import com.myTesi.aloisioUmberto.data.dao.UserRepository;
import com.myTesi.aloisioUmberto.data.entities.Sensor;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.*;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import com.myTesi.aloisioUmberto.dto.DateDto;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataInterestAreaDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensorDataServiceImpl implements SensorDataService {

    private final SensorDataRepository sensorDataRepository;
    private final SensorRepository sensorRepository;
    private final UserRepository userDao;
    private final SensorDataMapper sensorDataMapper = SensorDataMapper.INSTANCE;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthConverter jwtAuthConverter;
    private final RedisTemplate<String, Object> redisTemplate;
    private final InterestAreaRepository interestAreaRepository;
    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;

    // ========================================================================
    //  METODI PUBBLICI (invariati nelle firme)
    // ========================================================================

    @Override
    public SensorData saveSensorData(NewSensorDataDto newSensorDataDto) {
        // 1. Validazione e sicurezza
        String userId = getUserIdFromValidToken(newSensorDataDto.getToken());

        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        Sensor sensor = sensorRepository.findByIdAndUserId(newSensorDataDto.getSensorId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensore non trovato o non appartenente all'utente"));

        if (!BCrypt.checkpw(newSensorDataDto.getSensorPassword(), user.getSensorPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali sensore errate");
        }

        // 2. Mappatura iniziale
        SensorData sensorData = sensorDataMapper.newSensorDataDtoToSensorData(newSensorDataDto);
        sensorData.setSensorId(sensor.getId().toString());
        sensorData.setInterestAreaID(sensor.getInterestAreaID());
        sensorData.setTimestamp(newSensorDataDto.getTimestamp());
        sensorData.setSavedOnTime(Date.from(Instant.now()));

        // 3. Gestione del payload (conversione in Map)
        sensorData.setPayload(extractPayloadMap(newSensorDataDto));

        // 4. Aggiornamento storico posizioni nel sensore
        updateSensorPositionHistory(sensor, sensorData);

        // 5. Salvataggio finale
        return sensorDataRepository.save(sensorData);
    }

    @Override
    public SensorDataDto getLatestSensorDataBySensorId(String token, String id) {
        String userId = getUserIdFromValidToken(token);
        Sensor sensor = sensorRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensore non trovato"));
        Optional<SensorData> sensorData = sensorDataRepository.findTopBySensorId(sensor.getId().toString());
        return sensorData.map(data -> modelMapper.map(data, SensorDataDto.class)).orElse(null);
    }

    @Override
    public SensorData save(MultipartFile file, NewSensorDataDto newSensorDataDTO) throws IOException {
        String userId = getUserIdFromValidToken(newSensorDataDTO.getToken());

        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        Sensor sensor = sensorRepository.findByIdAndUserId(newSensorDataDTO.getSensorId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensore non trovato"));

        if (!BCrypt.checkpw(newSensorDataDTO.getSensorPassword(), user.getSensorPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali sensore errate");
        }

        SensorData data = sensorDataMapper.newSensorDataDtoToSensorData(newSensorDataDTO);
        data.setSensorId(sensor.getId().toString());
        data.setInterestAreaID(sensor.getInterestAreaID());
        data.setTimestamp(newSensorDataDTO.getTimestamp());
        data.setSavedOnTime(Date.from(Instant.now()));

        // Gestione file con handler specifico oppure payload testuale
        if (file != null && !file.isEmpty()) {
            SensorDataHandler handler = getHandlerForType(String.valueOf(sensor.getType()));
            if (handler != null) {
                handler.handle(data, newSensorDataDTO, file);
            }
        } else {
            data.setPayload(extractPayloadMap(newSensorDataDTO));
        }

        SensorData savedData = sensorDataRepository.save(data);
        sensorRepository.save(sensor); // già salvato in updateSensorPositionHistory? Qui no, ma mantengo per coerenza
        return savedData;
    }

    @Override
    public SensorDataInterestAreaDto getTopSensorDataBySensorId(String sensorId, String token) {
        String userId = getUserIdFromValidToken(token);
        return sensorRepository.findByIdAndUserId(sensorId, userId)
                .map(sensor -> getLatestForSensorList(Collections.singletonList(sensor.getId().toString()), null, null))
                .orElse(null);
    }

    @Override
    public SensorDataInterestAreaDto getTopSensorDataByInterestAreaIdAndSensorId(String interestAreaId, String sensorId, String token) {
        String userId = getUserIdFromValidToken(token);
        Sensor sensor = sensorRepository.findByIdAndInterestAreaIDAndUserId(sensorId, interestAreaId, userId);
        return sensor != null ? getLatestForSensorList(Collections.singletonList(sensor.getId().toString()), null, null) : null;
    }

    @Override
    public SensorDataInterestAreaDto getTopSensorDataByInterestAreaId(String interestAreaId, String token) {
        String userId = getUserIdFromValidToken(token);
        List<Sensor> sensors = sensorRepository.findAllByInterestAreaIDAndUserId(interestAreaId, userId);
        if (sensors.isEmpty()) return null;
        List<String> ids = sensors.stream().map(s -> s.getId().toString()).collect(Collectors.toList());
        return getLatestForSensorList(ids, null, null);
    }

    @Override
    public SensorDataInterestAreaDto getTopPublicSensorData() {
        return getPublicSensorDataInTimeRange(null); // senza limite temporale
    }

    @Override
    public SensorDataInterestAreaDto getAllPublicSensorDataIn5Min() {
        return getPublicSensorDataInTimeRange(-5);
    }

    @Override
    public SensorDataInterestAreaDto getAllPublicSensorDataIn10Min() {
        return getPublicSensorDataInTimeRange(-10);
    }

    @Override
    public SensorDataInterestAreaDto getAllPublicSensorDataIn15Min() {
        return getPublicSensorDataInTimeRange(-15);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataBySensorId5Min(String sensorId) {
        return getSensorDataInTimeRange(Collections.singletonList(sensorId), -5);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataBySensorId5Min(String sensorId, String token) {
        return getAuthorizedSensorDataInTimeRange(sensorId, token, -5);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataBySensorId10Min(String sensorId, String token) {
        return getAuthorizedSensorDataInTimeRange(sensorId, token, -10);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataBySensorId15Min(String sensorId, String token) {
        return getAuthorizedSensorDataInTimeRange(sensorId, token, -15);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaId5Min(String interestAreaId, String token) {
        return getAuthorizedAreaDataInTimeRange(interestAreaId, token, -5);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaId10Min(String interestAreaId, String token) {
        return getAuthorizedAreaDataInTimeRange(interestAreaId, token, -10);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaId15Min(String interestAreaId, String token) {
        return getAuthorizedAreaDataInTimeRange(interestAreaId, token, -15);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaIdAndSensorId5Min(String interestAreaId, String sensorId) {
        return getDataForSingleSensorInArea(interestAreaId, sensorId, -5);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaIdAndSensorId10Min(String interestAreaId, String sensorId) {
        return getDataForSingleSensorInArea(interestAreaId, sensorId, -10);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaIdAndSensorId15Min(String interestAreaId, String sensorId) {
        return getDataForSingleSensorInArea(interestAreaId, sensorId, -15);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataBySensorBetweenDate(DateDto dateDto) {

        Date fromDateUTC = adjustToUTC(dateDto.getForm());
        Date toDateUTC = adjustToUTC(dateDto.getTo());

        List<String> sensorIds = new ArrayList<>();
        if (dateDto.getSensorId() != null) {
            Optional<Sensor> sensor = sensorRepository.findById(dateDto.getSensorId());
            sensorIds.add(sensor.get().getId().toString());
        } else {
            List<Sensor> sensors = sensorRepository.findAllByInterestAreaIDAndIsPublicTrue(dateDto.getInterestAreaId());
            sensorIds = sensors.stream().map(s -> s.getId().toString()).collect(Collectors.toList());
        }

        return getLatestForSensorList(sensorIds, fromDateUTC, toDateUTC);
    }

    @Override
    public SensorDataDto getSensorDataById(Object id) {
        return sensorDataRepository.findById(id.toString())
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .orElse(null);
    }

    @Override
    public SensorData update(NewSensorDataDto newSensorDataDto) {
        String userId = getUserIdFromValidToken(newSensorDataDto.getToken());
        SensorData existingSensorData = sensorDataRepository.findById(newSensorDataDto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dato non trovato"));
        if (!Objects.equals(newSensorDataDto.getSensorId(), existingSensorData.getSensorId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID sensore non corrispondente");
        }
        sensorRepository.findByIdAndUserId(newSensorDataDto.getSensorId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensore non trovato"));

        existingSensorData.setSensorId(newSensorDataDto.getSensorId());
        existingSensorData.setPayloadType(newSensorDataDto.getPayloadType());
        existingSensorData.setLatitude(newSensorDataDto.getLatitude());
        existingSensorData.setLongitude(newSensorDataDto.getLongitude());
        existingSensorData.setTimestamp(Date.from(Instant.now()));
        return sensorDataRepository.save(existingSensorData);
    }

    @Override
    public void delete(String token, String id) {
        String userId = getUserIdFromValidToken(token);
        SensorData sensorData = sensorDataRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dato non trovato"));
        Sensor sensor = sensorRepository.findById(sensorData.getSensorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensore non trovato"));
        List<Sensor> sensorList = sensorRepository.findAllByIdAndUserId(String.valueOf(sensor.getId()), userId);
        if (sensorList == null || sensorList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operazione non consentita");
        }
        sensorDataRepository.deleteById(id);
    }

    @Override
    public String getProcessedSensorData(String type) {
        Date now = new Date();
        Date tenMinutesAgo = getStartTime(-30); // teneva -30?
        List<SensorData> sensorDataList = sensorDataRepository.findByTimestampBetweenAndPayloadType(tenMinutesAgo, now, "json");
        return createGeoJson(sensorDataList, type);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataProcessedByInterestArea(String interestAreaId, String token) {
        String userId = getUserIdFromValidToken(token);
        List<Sensor> sensors = sensorRepository.findAllByInterestAreaIDAndUserId(interestAreaId, userId);
        if (sensors.isEmpty()) return null;
        List<String> sensorIds = sensors.stream().map(s -> s.getId().toString()).collect(Collectors.toList());
        // Qui restituisce tutti i dati negli ultimi 10 minuti?
        Date now = new Date();
        Date tenMinutesAgo = getStartTime(-10);
        List<SensorData> allData = sensorDataRepository.findAllBySensorIdInAndTimestampBetween(sensorIds, tenMinutesAgo, now);
        return buildDtoFromList(allData);
    }

    @Override
    public SensorDataInterestAreaDto getTopPublicSensorDataByInterestAreaId(String interestAreaId) {
        List<Sensor> sensors = sensorRepository.findAllByIsPublicAndInterestAreaID(true, interestAreaId);
        if (sensors.isEmpty()) return null;
        List<String> ids = sensors.stream().map(s -> s.getId().toString()).collect(Collectors.toList());
        return getLatestForSensorList(ids, null, null);
    }

    // ========================================================================
    //  METODI PRIVATI OTTIMIZZATI
    // ========================================================================

    /**
     * Recupera l'ultimo SensorData per una lista di sensori in un intervallo temporale (opzionale)
     * usando un'unica aggregation pipeline MongoDB.
     */
    private SensorDataInterestAreaDto getLatestForSensorList(List<String> sensorIds, Date from, Date to) {
        if (sensorIds.isEmpty()) {
            return emptyDto();
        }

        Criteria criteria = Criteria.where("sensorId").in(sensorIds);
        if (from != null && to != null) {
            criteria = criteria.and("timestamp").gte(from).lte(to);
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.sort(Sort.Direction.DESC, "timestamp"),
                Aggregation.group("sensorId").first("$$ROOT").as("latest"),
                Aggregation.replaceRoot("latest")
        );

        AggregationResults<SensorData> results = mongoTemplate.aggregate(aggregation, SensorData.class, SensorData.class);
        List<SensorData> dataList = results.getMappedResults();

        return buildDtoFromList(dataList);
    }

    @Override
    public SensorDataInterestAreaDto getAllPrivateSensorDataBySensorBetweenDate(DateDto dateDto,  String token) {
        String userId = getUserIdFromValidToken(token);

        Date fromDateUTC = adjustToUTC(dateDto.getForm());
        Date toDateUTC = adjustToUTC(dateDto.getTo());

        List<String> sensorIds = new ArrayList<>();
        if (dateDto.getSensorId() != null) {
            Sensor sensor = findSensorForUser(dateDto.getSensorId(), dateDto.getInterestAreaId(), userId);
            sensorIds.add(sensor.getId().toString());
        } else {
            List<Sensor> sensors = sensorRepository.findAllByInterestAreaIDAndUserId(dateDto.getInterestAreaId(), userId);
            sensorIds = sensors.stream().map(s -> s.getId().toString()).collect(Collectors.toList());
        }

        return getLatestForSensorList(sensorIds, fromDateUTC, toDateUTC);
    }


    /**
     * Recupera tutti i SensorData (non solo l'ultimo) per una lista di sensori e intervallo.
     * Utilizzato dove il metodo originale restituiva tutti i dati in teoria (es. getAllSensorDataProcessedByInterestArea).
     */
    private List<SensorData> getAllDataForSensors(List<String> sensorIds, Date from, Date to) {
        if (sensorIds.isEmpty() || from == null || to == null) return Collections.emptyList();
        return sensorDataRepository.findAllBySensorIdInAndTimestampBetween(sensorIds, from, to);
    }

    /**
     * Costruisce un DTO con i dati passati e l'insieme delle chiavi dei payload.
     */
    private SensorDataInterestAreaDto buildDtoFromList(List<SensorData> dataList) {
        HashSet<String> keys = new HashSet<>();
        for (SensorData data : dataList) {
            keys.addAll(getSensorKeys(data));
        }
        SensorDataInterestAreaDto dto = new SensorDataInterestAreaDto();
        dto.setSensorData(dataList);
        dto.setSensorAreaTypes(keys);
        return dto;
    }

    private SensorDataInterestAreaDto emptyDto() {
        return new SensorDataInterestAreaDto();
    }

    // ------------------------------------------------------------------------
    //  Query pubbliche generiche con range temporale
    // ------------------------------------------------------------------------
    private SensorDataInterestAreaDto getPublicSensorDataInTimeRange(Integer minutes) {
        List<Sensor> sensors = sensorRepository.findAllByIsPublic(true);
        if (sensors.isEmpty()) return null;
        List<String> ids = sensors.stream().map(s -> s.getId().toString()).collect(Collectors.toList());
        Date from = (minutes != null) ? getStartTime(minutes) : null;
        Date to = (minutes != null) ? new Date() : null;
        return getLatestForSensorList(ids, from, to);
    }

    private SensorDataInterestAreaDto getSensorDataInTimeRange(List<String> sensorIds, int minutes) {
        Date from = getStartTime(minutes);
        Date to = new Date();
        return getLatestForSensorList(sensorIds, from, to);
    }


    @Override
    public List<SensorData> getRawDataForSensor(String sensorId, int minutesAgo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -minutesAgo);
        Date startDate = cal.getTime();


        return sensorDataRepository.findAllBySensorIdAndTimestampAfterOrderByTimestampAsc(sensorId, startDate);
    }

    // ------------------------------------------------------------------------
    //  Metodi autorizzati che verificano token e recuperano sensori
    // ------------------------------------------------------------------------
    private SensorDataInterestAreaDto getAuthorizedSensorDataInTimeRange(String sensorId, String token, int minutes) {
        String userId = getUserIdFromValidToken(token);
        List<Sensor> sensors = sensorRepository.findAllByIdAndUserId(sensorId, userId);
        if (sensors.isEmpty()) return null;
        List<String> sensorIds = sensors.stream().map(s -> s.getId().toString()).collect(Collectors.toList());
        return getSensorDataInTimeRange(sensorIds, minutes);
    }

    private SensorDataInterestAreaDto getAuthorizedAreaDataInTimeRange(String interestAreaId, String token, int minutes) {
        String userId = getUserIdFromValidToken(token);
        List<Sensor> sensors = sensorRepository.findAllByInterestAreaIDAndUserId(interestAreaId, userId);
        if (sensors.isEmpty()) return null;
        List<String> sensorIds = sensors.stream().map(s -> s.getId().toString()).collect(Collectors.toList());
        return getSensorDataInTimeRange(sensorIds, minutes);
    }

    private SensorDataInterestAreaDto getDataForSingleSensorInArea(String interestAreaId, String sensorId, int minutes) {
        // Metodo senza token, accesso diretto
        Date from = getStartTime(minutes);
        List<String> ids = Collections.singletonList(sensorId);
        return getLatestForSensorList(ids, from, new Date());
    }

    // ------------------------------------------------------------------------
    //  Helpers generici
    // ------------------------------------------------------------------------
    private String getUserIdFromValidToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token non valido");
        }
        String userId = jwtTokenProvider.getUserIdFromUserToken(token);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Impossibile estrarre utente dal token");
        }
        return userId;
    }

    private Date getStartTime(int minutesAgo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, minutesAgo);
        return cal.getTime();
    }

    private void updateSensorPositionHistory(Sensor sensor, SensorData sensorData) {
        try {
            if (sensor.getLongitude() != null && !sensor.getLongitude().isEmpty() &&
                    sensor.getLatitude() != null && !sensor.getLatitude().isEmpty()) {

                Double currentLongitude = sensor.getLongitude().getFirst();
                Double currentLatitude = sensor.getLatitude().getFirst();

                if (!currentLongitude.equals(sensorData.getLongitude()) ||
                        !currentLatitude.equals(sensorData.getLatitude())) {
                    sensor.getLongitude().addFirst(sensorData.getLongitude());
                    sensor.getLatitude().addFirst(sensorData.getLatitude());
                }
            } else {
                sensor.setLongitude(new ArrayList<>(List.of(sensorData.getLongitude())));
                sensor.setLatitude(new ArrayList<>(List.of(sensorData.getLatitude())));
            }
            sensorRepository.save(sensor);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Errore integrità dati", e);
        }
    }

    /**
     * Converte il payload del DTO in una mappa utilizzabile per MongoDB,
     * senza serializzazioni intermedie.
     */
    private Map<String, Object> extractPayloadMap(NewSensorDataDto dto) {
        Object rawPayload = dto.getPayload();
        if (rawPayload instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) rawPayload;
            return map;
        }
        // Altrimenti prova a fare parsing del formato stringa "key=value,key=value"
        Map<String, Object> payloadMap = new HashMap<>();
        if (rawPayload != null) {
            try {
                String payloadString = rawPayload.toString().replaceAll("[{}]", "");
                String[] entries = payloadString.split(",");
                for (String entry : entries) {
                    String[] keyValue = entry.split("=");
                    if (keyValue.length == 2) {
                        payloadMap.put(keyValue[0].trim(), parseValue(keyValue[1].trim()));
                    }
                }
            } catch (Exception e) {
                // payload malformato, si lascia vuoto
            }
        }
        return payloadMap;
    }

    private Object parseValue(String value) {
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            return value;
        }
    }

    // ------------------------------------------------------------------------
    //  Ottimizzazione: accesso diretto alla mappa del payload
    // ------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private Map<String, Object> getPayloadMap(SensorData data) {
        if (data.getPayload() instanceof Map) {
            return (Map<String, Object>) data.getPayload();
        }
        return Collections.emptyMap();
    }

    private List<String> getSensorKeys(SensorData data) {
        return new ArrayList<>(getPayloadMap(data).keySet());
    }

    private double getSensorValue(SensorData data, String key) {
        Object value = getPayloadMap(data).get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    // ------------------------------------------------------------------------
    //  GeoJSON (invariato ma ottimizzato nell'accesso al payload)
    // ------------------------------------------------------------------------
    private String createGeoJson(List<SensorData> sensorDataList, String type) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode geoJson = mapper.createObjectNode();
        geoJson.put("type", "FeatureCollection");
        ArrayNode features = geoJson.putArray("features");

        for (SensorData data : sensorDataList) {
            ObjectNode feature = mapper.createObjectNode();
            feature.put("type", "Feature");

            ObjectNode geometry = mapper.createObjectNode();
            geometry.put("type", "Point");
            ArrayNode coordinates = geometry.putArray("coordinates");
            coordinates.add(data.getLongitude());
            coordinates.add(data.getLatitude());

            ObjectNode properties = mapper.createObjectNode();
            properties.put("value", getSensorValue(data, type));

            feature.set("geometry", geometry);
            feature.set("properties", properties);
            features.add(feature);
        }

        try {
            return mapper.writeValueAsString(geoJson);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    // ------------------------------------------------------------------------
    //  Handler per tipo di file
    // ------------------------------------------------------------------------
    private SensorDataHandler getHandlerForType(String dataType) {
        return switch (dataType.toLowerCase()) {
            case "json" -> new JsonSensorDataHandler();
            case "geojson" -> new GeoJsonSensorDataHandler();
            case "image" -> new ImageSensorDataHandler(new ImageServiceImpl());
            case "shapefile" -> new ShapefileSensorDataHandler();
            case "raster" -> new RasterSensorDataHandler();
            default -> null;
        };
    }

    // ------------------------------------------------------------------------
    //  Supporto DateDto
    // ------------------------------------------------------------------------
    private Date adjustToUTC(Date date) {
        // TODO: verificare se la sottrazione di 2 ore è ancora necessaria
        return Date.from(date.toInstant().atZone(java.time.ZoneId.of("UTC")).minusHours(2).toInstant());
    }

    private Sensor findSensorForUser(String sensorId, String interestAreaId, String userId) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensore non trovato"));
        return sensorRepository.findByIdAndInterestAreaIDAndUserId(String.valueOf(sensor.getId()), interestAreaId, userId);
    }
}