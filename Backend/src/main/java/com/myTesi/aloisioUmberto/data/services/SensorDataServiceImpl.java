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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SensorDataServiceImpl implements SensorDataService {

    @Autowired
    private SensorDataRepository sensorDataRepository;
    @Autowired
    private SensorRepository sensorRepository;
    private final UserRepository userDao;
    private final SensorDataMapper sensorDataMapper = SensorDataMapper.INSTANCE;
    @Autowired
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    private final JwtAuthConverter jwtAuthConverter;


    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private InterestAreaRepository interestAreaRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    public void SensorDataService(RedisTemplate<String, Object> redisTemplate,
                                  SensorDataRepository sensorDataRepository) {
        this.redisTemplate = redisTemplate;
        this.sensorDataRepository = sensorDataRepository;
        final MongoTemplate mongoTemplate;
    }

    @Override
    public SensorData saveSensorData(NewSensorDataDto newSensorDataDto) {
        // 1. Validazione e Sicurezza (Sostituiti assert con eccezioni reali)
        String userId = isValidToken(newSensorDataDto.getToken());
        if (userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token non valido");

        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        Sensor sensor = sensorRepository.findByIdAndUserId(newSensorDataDto.getSensorId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensore non trovato o non appartenente all'utente"));

        if (!BCrypt.checkpw(newSensorDataDto.getSensorPassword(), user.getSensorPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali sensore errate");
        }

        // 2. Mappatura iniziale (Il payload viene ignorato dal mapper come impostato prima)
        SensorData sensorData = sensorDataMapper.newSensorDataDtoToSensorData(newSensorDataDto);
        sensorData.setSensorId(sensor.getId().toString());
        sensorData.setInterestAreaID(sensor.getInterestAreaID());
        sensorData.setTimestamp(newSensorDataDto.getTimestamp());
        sensorData.setSavedOnTime(Date.from(Instant.now()));

        // 3. Gestione del Payload (CONVERSIONE IN MAPPA)
        // Se il DTO ha un Object, lo convertiamo in Map per MongoDB
        Map<String, Object> payloadMap = new HashMap<>();
        Object rawPayload = newSensorDataDto.getPayload();

        if (rawPayload instanceof Map) {
            // Se è già una mappa (es. inviata come JSON via REST)
            payloadMap = (Map<String, Object>) rawPayload;
        } else if (rawPayload != null) {
            // Se è una stringa o altro formato, usiamo il parsing manuale che avevi fatto
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
                // Loggare l'errore o gestire payload malformati
            }
        }
        sensorData.setPayload(payloadMap);

        // 4. Aggiornamento Storico Posizioni nel Sensore
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

        // 5. Salvataggio finale dello storico dati
        return sensorDataRepository.save(sensorData);
    }


    @Override
    public SensorDataDto getLatestSensorDataBySensorId(String token, String id) {
        String userId = isValidToken(token);
        assert userId != null;
        Sensor sensor = sensorRepository.findByIdAndUserId(id, userId).orElse(null);
        assert sensor != null;
        Optional<SensorData> sensorData = sensorDataRepository.findTopBySensorId(sensor.getId().toString());
        return sensorData.map(data -> modelMapper.map(data, SensorDataDto.class)).orElse(null);
    }



    @Override
    public SensorData save(MultipartFile file, NewSensorDataDto newSensorDataDTO) throws IOException {
        // 1. Recupero e Validazione (Usa if invece di assert per produzione)
        String userId = isValidToken(newSensorDataDTO.getToken());
        if (userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Sensor sensor = sensorRepository.findByIdAndUserId(newSensorDataDTO.getSensorId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor not found"));

        // 2. Controllo Password
        if (!BCrypt.checkpw(newSensorDataDTO.getSensorPassword(), user.getSensorPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // 3. Mappatura Dati
        SensorData data = sensorDataMapper.newSensorDataDtoToSensorData(newSensorDataDTO);
        data.setSensorId(sensor.getId().toString());
        data.setInterestAreaID(sensor.getInterestAreaID());
        data.setTimestamp(newSensorDataDTO.getTimestamp());
        data.setSavedOnTime(Date.from(Instant.now()));

        // 4. Gestione Payload (Ottimizzata per MongoDB)
        if (file != null && !file.isEmpty()) {
            SensorDataHandler handler = getHandlerForType(String.valueOf(sensor.getType()));
            if (handler != null) {
                handler.handle(data, newSensorDataDTO, file);
            }
        } else {
            // EVITIAMO ObjectMapper e la conversione in String.
            // Salviamo direttamente la Map così Mongo crea un oggetto BSON nativo.
            Map<String, Object> payloadMap = new HashMap<>();

            try {
                // Se getPayload() è già una mappa o un oggetto, usalo direttamente.
                // Se è una stringa strana, il tuo parsing manuale va bene ma puliamolo:
                String rawPayload = newSensorDataDTO.getPayload().toString().replaceAll("[{}]", "");
                String[] entries = rawPayload.split(",");
                for (String entry : entries) {
                    String[] keyValue = entry.split("=");
                    if (keyValue.length == 2) {
                        payloadMap.put(keyValue[0].trim(), parseValue(keyValue[1].trim()));
                    }
                }
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload format error");
            }

            data.setPayload(payloadMap);
        }

        SensorData savedData = sensorDataRepository.save(data);


        sensorRepository.save(sensor);

        return savedData;
    }

    @Override
    public SensorDataInterestAreaDto getTopSensorDataBySensorId(String sensorId, String token) {
        String userId = isValidToken(token);
        if (userId == null) return null;

        return sensorRepository.findByIdAndUserId(sensorId, userId)
                .map(sensor -> createSensorDataInterestAreaDto(String.valueOf(sensor.getId()), null, null))
                .orElse(null);
    }


    @Override
    public SensorDataInterestAreaDto getTopSensorDataByInterestAreaIdAndSensorId(String interestAreaId, String sensorId, String token) {
        String userId = isValidToken(token);
        if (userId == null) return null;

        Sensor sensor = sensorRepository.findByIdAndInterestAreaIDAndUserId(sensorId, interestAreaId, userId);
        return sensor != null ? createSensorDataInterestAreaDto(String.valueOf(sensor.getId()), null, null) : null;
    }


    @Override
    public SensorDataInterestAreaDto getTopSensorDataByInterestAreaId(String interestAreaId, String token) {
        String userId = isValidToken(token);
        if (userId == null) return null;

        List<Sensor> sensors = sensorRepository.findAllByInterestAreaIDAndUserId(interestAreaId, userId);
        return createSensorDataInterestAreaDtoForMultipleSensors(sensors, null, null);
    }

    @Override
    public SensorDataInterestAreaDto getTopPublicSensorData() {
        long startTime = System.currentTimeMillis();

        // 1. Recupera i sensori (usa findAllByIsPublic come definito nel tuo repository)
        List<Sensor> sensors = sensorRepository.findAllByIsPublic(true);

        if (sensors == null || sensors.isEmpty()) {
            System.out.println("Nessun sensore pubblico trovato.");
            return new SensorDataInterestAreaDto();
        }

        List<String> publicSensorIds = sensors.stream()
                .map(s -> s.getId().toString())
                .collect(Collectors.toList());

        // 2. Costruzione Aggregation
        Aggregation aggregation = Aggregation.newAggregation(
                // Filtro per ID sensore
                Aggregation.match(Criteria.where("sensorId").in(publicSensorIds)),
                // Ordino per timestamp decrescente (dal più nuovo)
                Aggregation.sort(Sort.Direction.DESC, "timestamp"),
                // Raggruppo per sensore e prendo l'intero documento ($$ROOT) del primo record trovato
                Aggregation.group("sensorId").first("$$ROOT").as("ultimoRecord"),
                // Rendi il documento "ultimoRecord" la radice del risultato
                Aggregation.replaceRoot("ultimoRecord")
        );

        // 3. Esecuzione (Usa SensorData.class per il nome collezione automatico)
        List<SensorData> results = mongoTemplate.aggregate(aggregation, SensorData.class, SensorData.class).getMappedResults();

        // 4. Calcolo tipi area
        HashSet<String> areaTypes = new HashSet<>();
        for (Sensor s : sensors) {
            if (s.getType() != null) {
                areaTypes.add(s.getType().toString());
            }
        }

        SensorDataInterestAreaDto dto = new SensorDataInterestAreaDto();
        dto.setSensorData(results);
        dto.setSensorAreaTypes(areaTypes);

        System.out.println("------------------------------------------");
        System.out.println("REPORT ESECUZIONE (Aggregation):");
        System.out.println("- Tempo impiegato: " + (System.currentTimeMillis() - startTime) + " ms");
        System.out.println("- Record trovati: " + results.size());
        System.out.println("------------------------------------------");

        return dto;
    }

    @Override
    public SensorDataInterestAreaDto getAllPublicSensorDataIn5Min() {
        return getAllPublicSensorDataInTimeFrame(-5);
    }

    @Override
    public SensorDataInterestAreaDto getAllPublicSensorDataIn10Min() {
        return getAllPublicSensorDataInTimeFrame(-10);
    }

    @Override
    public SensorDataInterestAreaDto getAllPublicSensorDataIn15Min() { return getAllPublicSensorDataInTimeFrame(-15);
    }

    public SensorDataInterestAreaDto getAllSensorDataBySensorId5Min(String sensorId) {
        long startTime = System.currentTimeMillis();

        // Calcolo del range temporale
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        // ATTENZIONE: Qui è impostato a -5. Se i dati sono di 10 minuti fa, non li troverà.
        // Se vuoi vedere quelli degli ultimi 15, cambia -5 in -15.
        calendar.add(Calendar.MINUTE, -5);
        Date fiveMinutesAgo = calendar.getTime();

        // DEBUG: Verifichiamo cosa stiamo chiedendo al DB
        System.out.println("--- DEBUG SEARCH ---");
        System.out.println("Sensor ID: " + sensorId);
        System.out.println("Cerca da: " + fiveMinutesAgo);
        System.out.println("Cerca a : " + now);

        // Recuperiamo la lista completa nel range
        List<SensorData> allDataInRange = sensorDataRepository.findAllBySensorIdAndTimestampBetween(sensorId, fiveMinutesAgo, now);

        System.out.println("Record trovati nel database: " + (allDataInRange != null ? allDataInRange.size() : 0));

        // Cerchiamo il più recente tra quelli trovati
        Optional<SensorData> latestSensorData = allDataInRange.stream()
                .max(Comparator.comparing(SensorData::getTimestamp));

        List<SensorData> sensorDataList = new ArrayList<>();
        HashSet<String> uniqueKeys = new HashSet<>();

        if (latestSensorData.isPresent()) {
            SensorData data = latestSensorData.get();
            System.out.println("Dato più recente trovato: " + data.getTimestamp());
            sensorDataList.add(data);
            uniqueKeys.addAll(getSensorKeys(data));
        } else {
            System.out.println("ATTENZIONE: Nessun SensorData trovato nel range degli ultimi 5 minuti.");
        }

        SensorDataInterestAreaDto sensorDataInterestAreaDto = new SensorDataInterestAreaDto();
        sensorDataInterestAreaDto.setSensorData(sensorDataList);
        sensorDataInterestAreaDto.setSensorAreaTypes(uniqueKeys);

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Operazione completata in: " + duration + " ms");
        System.out.println("---------------------");

        return sensorDataInterestAreaDto;
    }


    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaIdAndSensorId5Min(String interestAreaId, String sensorId) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -5);
        Date fiveMinutesAgo = calendar.getTime();

        Optional<SensorData> latestSensorData = sensorDataRepository.findAllByInterestAreaIDAndSensorIdAndTimestampBetween(interestAreaId, sensorId, fiveMinutesAgo, now)
                .stream()
                .max(Comparator.comparing(SensorData::getTimestamp));

        List<SensorData> sensorDataList = new ArrayList<>();
        HashSet<String> uniqueKeys = new HashSet<>();

        latestSensorData.ifPresent(data -> {
            sensorDataList.add(data);
            uniqueKeys.addAll(getSensorKeys(data));
        });

        SensorDataInterestAreaDto sensorDataInterestAreaDto = new SensorDataInterestAreaDto();
        sensorDataInterestAreaDto.setSensorData(sensorDataList);
        sensorDataInterestAreaDto.setSensorAreaTypes(uniqueKeys);

        return sensorDataInterestAreaDto;
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaIdAndSensorId10Min(String interestAreaId, String sensorId) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date tenMinutesAgo = calendar.getTime();

        Optional<SensorData> latestSensorData = sensorDataRepository.findAllByInterestAreaIDAndSensorIdAndTimestampBetween(interestAreaId, sensorId, tenMinutesAgo, now)
                .stream()
                .max(Comparator.comparing(SensorData::getTimestamp));

        List<SensorData> sensorDataList = new ArrayList<>();
        HashSet<String> uniqueKeys = new HashSet<>();

        latestSensorData.ifPresent(data -> {
            sensorDataList.add(data);
            uniqueKeys.addAll(getSensorKeys(data));
        });

        SensorDataInterestAreaDto sensorDataInterestAreaDto = new SensorDataInterestAreaDto();
        sensorDataInterestAreaDto.setSensorData(sensorDataList);
        sensorDataInterestAreaDto.setSensorAreaTypes(uniqueKeys);

        return sensorDataInterestAreaDto;
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataBySensorId5Min(String sensorId, String token) {
        return getAllSensorDataBySensorIdInTimeFrame(sensorId, token, -5);
    }


    @Override
    public SensorDataInterestAreaDto getAllSensorDataBySensorId10Min(String sensorId, String token) {
        return getAllSensorDataBySensorIdInTimeFrame(sensorId, token, -10);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataBySensorId15Min(String sensorId,  String token) {
        return getAllSensorDataBySensorIdInTimeFrame(sensorId, token, -15);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaId5Min(String interestAreaId,  String token) {
        return getAllSensorDataByInterestAreaIdInTimeFrame(interestAreaId, token, -5);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaId10Min(String interestAreaId, String token) {
        return getAllSensorDataByInterestAreaIdInTimeFrame(interestAreaId, token, -10);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaId15Min(String interestAreaId,  String token) {
        return getAllSensorDataByInterestAreaIdInTimeFrame(interestAreaId, token, -15);
    }

    private SensorDataInterestAreaDto getAllSensorDataBySensorIdInTimeFrame(String sensorId, String token, int minutesAgo) {
        String userId = isValidToken(token);
        assert userId != null;

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -minutesAgo);
        Date tenMinutesAgo = calendar.getTime();

        List<Sensor> sensors = sensorRepository.findAllByIdAndUserId(sensorId, userId);
        if (sensors == null || sensors.isEmpty()) {
            return null;
        }

        HashSet<String> uniqueKeys = new HashSet<>();

        SensorDataInterestAreaDto sensorDataInterestAreaDto = new SensorDataInterestAreaDto();
        List<SensorData> sensorDataList = new ArrayList<>();
        for (Sensor sensor : sensors) {
            sensorDataList = sensorDataRepository.findAllByTimestampBetweenAndSensorId(tenMinutesAgo, now, String.valueOf(sensor.getId()));

            // Collect unique keys from payloads
            for (SensorData data : sensorDataList) {
                uniqueKeys.addAll(getSensorKeys(data));
            }

        }

        sensorDataInterestAreaDto.setSensorData(sensorDataList);
        sensorDataInterestAreaDto.setSensorAreaTypes(uniqueKeys);

        System.out.println(sensorDataInterestAreaDto);

        return sensorDataInterestAreaDto;
    }

    private SensorDataInterestAreaDto getAllSensorDataByInterestAreaIdInTimeFrame(String interestAreaId, String token, int minutesAgo) {
        String userId = isValidToken(token);
        assert userId != null;

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -minutesAgo);
        Date tenMinutesAgo = calendar.getTime();

        List<Sensor> sensors = sensorRepository.findAllByInterestAreaIDAndUserId(interestAreaId, userId);
        if (sensors == null || sensors.isEmpty()) {
            return null;
        }

        HashSet<String> uniqueKeys = new HashSet<>();

        SensorDataInterestAreaDto sensorDataInterestAreaDto = new SensorDataInterestAreaDto();
        List<SensorData> sensorDataList = new ArrayList<>();
        for (Sensor sensor : sensors) {
            sensorDataList = sensorDataRepository.findAllByTimestampBetweenAndSensorId(tenMinutesAgo, now, String.valueOf(sensor.getId()));

            // Collect unique keys from payloads
            for (SensorData data : sensorDataList) {
                uniqueKeys.addAll(getSensorKeys(data));
            }

        }

        sensorDataInterestAreaDto.setSensorData(sensorDataList);
        sensorDataInterestAreaDto.setSensorAreaTypes(uniqueKeys);

        System.out.println(sensorDataInterestAreaDto);

        return sensorDataInterestAreaDto;

    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaIdAndSensorId15Min(String interestAreaId, String sensorId) {
        return getAllSensorDataByInterestAreaAndSensorIdInTimeFrame(interestAreaId, sensorId, -15);
    }

    private SensorDataInterestAreaDto getAllSensorDataByInterestAreaAndSensorIdInTimeFrame(String interestAreaId, String sensorId, int minutesAgo) {
        Date fromTime = calculateTimeFromNow(minutesAgo);
        List<SensorData> sensorDataList = new ArrayList<>();
        HashSet<String> uniqueKeys = new HashSet<>();

        sensorDataRepository.findAllByInterestAreaIDAndSensorIdAndTimestampBetween(interestAreaId, sensorId, fromTime, new Date())
                .stream()
                .max(Comparator.comparing(SensorData::getTimestamp))
                .ifPresent(data -> {
                    sensorDataList.add(data);
                    uniqueKeys.addAll(getSensorKeys(data));
                });

        return buildSensorDataInterestAreaDto(sensorDataList, uniqueKeys);
    }

    public SensorDataInterestAreaDto getAllSensorDataBySensorBetweenDate(DateDto dateDto) {
        String userId = isValidToken(dateDto.getToken());
        assert userId != null;

        Date fromDateUTC = adjustToUTC(dateDto.getForm());
        Date toDateUTC = adjustToUTC(dateDto.getTo());

        List<SensorData> sensorDataList = new ArrayList<>();
        HashSet<String> uniqueKeys = new HashSet<>();

        if (dateDto.getSensorId() != null) {
            Sensor sensor = findSensorForUser(dateDto.getSensorId(), dateDto.getInterestAreaId(), userId);
            sensorDataRepository.findAllBySensorIdAndTimestampBetween(String.valueOf(sensor.getId()), fromDateUTC, toDateUTC)
                    .stream()
                    .max(Comparator.comparing(SensorData::getTimestamp))
                    .ifPresent(data -> {
                        sensorDataList.add(data);
                        uniqueKeys.addAll(getSensorKeys(data));
                    });
        } else {
            List<Sensor> sensors = sensorRepository.findAllByInterestAreaIDAndUserId(dateDto.getInterestAreaId(), userId);
            for (Sensor sensor : sensors) {
                sensorDataRepository.findAllBySensorIdAndTimestampBetween(String.valueOf(sensor.getId()), fromDateUTC, toDateUTC)
                        .stream()
                        .max(Comparator.comparing(SensorData::getTimestamp))
                        .ifPresent(data -> {
                            sensorDataList.add(data);
                            uniqueKeys.addAll(getSensorKeys(data));
                        });
            }
        }

        return buildSensorDataInterestAreaDto(sensorDataList, uniqueKeys);
    }

    private Date calculateTimeFromNow(int minutesAgo) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, minutesAgo);
        return calendar.getTime();
    }

    private Date adjustToUTC(Date date) {
        return Date.from(date.toInstant().atZone(ZoneId.of("UTC")).minusHours(2).toInstant());
    }

    private Sensor findSensorForUser(String sensorId, String interestAreaId, String userId) {
        Sensor sensor = sensorRepository.findById(sensorId).orElse(null);
        assert sensor != null;
        return sensorRepository.findByIdAndInterestAreaIDAndUserId(String.valueOf(sensor.getId()), interestAreaId, userId);
    }



    @Override
    public SensorDataDto getSensorDataById(Object id) {
        Optional<SensorData> data = sensorDataRepository.findById(id.toString());
        return data.map(sensorDataMapper::sensorDataToSensorDataDto).orElse(null);
    }

    @Override
    public SensorData update(NewSensorDataDto newSensorDataDto) {
        String userId = isValidToken(newSensorDataDto.getToken());
        assert userId != null;
        SensorData existingSensorData = sensorDataRepository.findById(newSensorDataDto.getId()).orElse(null);
        assert existingSensorData != null;
        if (newSensorDataDto.getSensorId() != existingSensorData.getSensorId()) {
            throw new RuntimeException("Invalid credentials");
        }
        Sensor sensor = sensorRepository.findByIdAndUserId(newSensorDataDto.getSensorId(), userId).orElse(null);
        assert sensor != null;
        existingSensorData.setSensorId(newSensorDataDto.getSensorId());
        existingSensorData.setPayloadType(newSensorDataDto.getPayloadType());
        existingSensorData.setLatitude(newSensorDataDto.getLatitude());
        existingSensorData.setLongitude(newSensorDataDto.getLongitude());
        existingSensorData.setTimestamp(Date.from(Instant.now()));


        return sensorDataRepository.save(existingSensorData);
    }

    @Override
    public void delete(String token, String id) {
        String userId = isValidToken(token);
        assert userId != null;
        Optional<SensorData> sensorDataDto = sensorDataRepository.findById(id);
        assert sensorDataDto.isPresent();
        Optional<Sensor> sensor = sensorRepository.findById(sensorDataDto.get().getSensorId());
        assert sensor.isPresent();
        List<Sensor> sensorList = sensorRepository.findAllByIdAndUserId(String.valueOf(sensor.get().getId()),userId);
        if (sensorList != null && !sensorList.isEmpty()) {
            sensorDataRepository.deleteById(id);
        }else {
            throw new RuntimeException("Sensor not found");
        }
    }

    @Override
    public String getProcessedSensorData(String type) {
        //TODO Implementare seriamente
        //List<Sensor> sensors = sensorRepository.findAllByVisibility(true);
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -30);
        Date tenMinutesAgo = calendar.getTime();

        List<SensorData> sensorDataList = sensorDataRepository.findByTimestampBetweenAndPayloadType(tenMinutesAgo, now, "json");
        // Creare il GeoJSON
        String geoJson = createGeoJson(sensorDataList, type);

        return geoJson;
    }


    @Override
    public SensorDataInterestAreaDto getAllSensorDataProcessedByInterestArea(String interestAreaId, String token) {
        String userId = isValidToken(token);
        assert userId != null;

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date tenMinutesAgo = calendar.getTime();

        List<Sensor> sensors = sensorRepository.findAllByInterestAreaIDAndUserId(interestAreaId, userId);
        if (sensors == null || sensors.isEmpty()) {
            return null;
        }

        HashSet<String> uniqueKeys = new HashSet<>();

        SensorDataInterestAreaDto sensorDataInterestAreaDto = new SensorDataInterestAreaDto();
        List<SensorData> sensorDataList = new ArrayList<>();
        for (Sensor sensor : sensors) {
            sensorDataList = sensorDataRepository.findAllByTimestampBetweenAndSensorId(tenMinutesAgo, now, String.valueOf(sensor.getId()));

            // Collect unique keys from payloads
            for (SensorData data : sensorDataList) {
                uniqueKeys.addAll(getSensorKeys(data));
            }

        }

        sensorDataInterestAreaDto.setSensorData(sensorDataList);
        sensorDataInterestAreaDto.setSensorAreaTypes(uniqueKeys);

        System.out.println(sensorDataInterestAreaDto);

        return sensorDataInterestAreaDto;





    }

    @Override
    public SensorDataInterestAreaDto getTopPublicSensorDataByInterestAreaId(String interestAreaId) {
        List<Sensor> sensors = sensorRepository.findAllByIsPublicAndInterestAreaID(true, interestAreaId);
        if (sensors == null || sensors.isEmpty()) return null;



        return createSensorDataInterestAreaDtoForMultipleSensors(sensors, null, null);
    }


    private String createGeoJson(List<SensorData> sensorDataList, String type) {
        ObjectMapper mapper = new ObjectMapper();
        // Creare la struttura GeoJSON
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
            e.printStackTrace();
            return "{}";
        }
    }

    private double getSensorValue(SensorData data, String type) {
        if (data.getPayload() != null) {
            try {
                // Deserialize the JSON payload into a Map
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> payload = mapper.readValue(data.getPayload().toString(), Map.class);

                // Find the value based on the provided key (type)
                Object value = payload.getOrDefault(type, 0.0);

                // Check if the retrieved value is a number
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                } else {
                    return 0.0;
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return 0.0;
    }

    private List<String> getSensorKeys(SensorData data) {
        List<String> keys = new ArrayList<>();
        if (data.getPayload() != null) {
            try {
                // Deserialize the JSON payload into a Map
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> payload = mapper.readValue(data.getPayload().toString(), Map.class);

                // Extract the keys and add them to the list
                keys.addAll(payload.keySet());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return keys;
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





    private String isValidToken(String token) {
        if (jwtTokenProvider.validateToken(token))
            return jwtTokenProvider.getUserIdFromUserToken(token);
        return null;
    }

    private SensorDataHandler getHandlerForType(String dataType) {
        return switch (dataType.toLowerCase()) {
            case "json" -> new JsonSensorDataHandler();
            case "geojson" -> new GeoJsonSensorDataHandler();
            case "image" -> new ImageSensorDataHandler(new ImageServiceImpl());
            case "shapefile" -> new ShapefileSensorDataHandler(); //Probably not serve
            case "raster" -> new RasterSensorDataHandler(); //TODO
            default -> null;
        };
    }
    private SensorDataInterestAreaDto getAllPublicSensorDataInTimeFrame(int minutesAgo) {
        long startTime = System.currentTimeMillis();

        List<Sensor> sensors = sensorRepository.findAllByIsPublic(true);
        int totalPublicSensors = (sensors != null) ? sensors.size() : 0;

        if (sensors == null || sensors.isEmpty()) {
            return null;
        }

        // Forza il valore a essere positivo per poi sottrarlo
        int positiveMinutes = Math.abs(minutesAgo);

        Calendar calendar = Calendar.getInstance();
        Date toTime = new Date(); // Ora attuale
        calendar.setTime(toTime);
        calendar.add(Calendar.MINUTE, -positiveMinutes); // Sottrae sempre (va nel passato)
        Date fromTime = calendar.getTime();

        // Adesso:
        // fromTime = 11:15 (Passato)
        // toTime   = 11:30 (Presente)

        SensorDataInterestAreaDto result = createSensorDataInterestAreaDtoForMultipleSensors(sensors, fromTime, toTime);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        int dataFoundCount = (result != null && result.getSensorData() != null) ? result.getSensorData().size() : 0;

        System.out.println("------------------------------------------");
        System.out.println("REPORT ESECUZIONE (FIXED):");
        System.out.println("- Tempo impiegato: " + duration + " ms");
        System.out.println("- Range richiesto: " + positiveMinutes + " minuti fa");
        System.out.println("- Query Range: [ DA: " + fromTime + " ] -> [ A: " + toTime + " ]");
        System.out.println("- Sensori processati: " + totalPublicSensors);
        System.out.println("- Record dati ricavati: " + dataFoundCount);
        System.out.println("------------------------------------------");

        return result;
    }


    private SensorDataInterestAreaDto createSensorDataInterestAreaDto(String sensorId, Date fromTime, Date toTime) {
        List<SensorData> sensorDataList = new ArrayList<>();
        HashSet<String> uniqueKeys = new HashSet<>();

        if (fromTime != null && toTime != null) {
            sensorDataRepository.findAllBySensorIdAndTimestampBetween(sensorId, fromTime, toTime)
                    .stream()
                    .max(Comparator.comparing(SensorData::getTimestamp))
                    .ifPresent(data -> {
                        sensorDataList.add(data);
                        uniqueKeys.addAll(getSensorKeys(data));
                    });
            return buildSensorDataInterestAreaDto(sensorDataList, uniqueKeys);
        }else {
            sensorDataRepository.findAllBySensorId(sensorId)
                    .stream()
                    .max(Comparator.comparing(SensorData::getTimestamp))
                    .ifPresent(data -> {
                        sensorDataList.add(data);
                        uniqueKeys.addAll(getSensorKeys(data));
                    });
        }

        return buildSensorDataInterestAreaDto(sensorDataList, uniqueKeys);
    }

    private SensorDataInterestAreaDto createSensorDataInterestAreaDtoForMultipleSensors(List<Sensor> sensors, Date fromTime, Date toTime) {
        if (sensors == null || sensors.isEmpty()) return null;

        List<SensorData> sensorDataList = new ArrayList<>();
        HashSet<String> uniqueKeys = new HashSet<>();

        for (Sensor sensor : sensors) {
            if (fromTime != null && toTime != null) {
                sensorDataRepository.findAllBySensorIdAndTimestampBetween(String.valueOf(sensor.getId()), fromTime, toTime)
                        .stream()
                        .max(Comparator.comparing(SensorData::getTimestamp))
                        .ifPresent(data -> {
                            sensorDataList.add(data);
                            uniqueKeys.addAll(getSensorKeys(data));
                        });
            }else {
                sensorDataRepository.findAllBySensorId(String.valueOf(sensor.getId()))
                        .stream()
                        .max(Comparator.comparing(SensorData::getTimestamp))
                        .ifPresent(data -> {
                            sensorDataList.add(data);
                            uniqueKeys.addAll(getSensorKeys(data));
                        });
            }
        }

        return buildSensorDataInterestAreaDto(sensorDataList, uniqueKeys);
    }

    private SensorDataInterestAreaDto buildSensorDataInterestAreaDto(List<SensorData> sensorDataList, HashSet<String> uniqueKeys) {
        SensorDataInterestAreaDto dto = new SensorDataInterestAreaDto();
        dto.setSensorData(sensorDataList);
        dto.setSensorAreaTypes(uniqueKeys);
        return dto;
    }


}
