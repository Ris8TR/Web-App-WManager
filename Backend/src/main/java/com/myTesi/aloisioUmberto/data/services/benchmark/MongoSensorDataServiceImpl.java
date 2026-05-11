package com.myTesi.aloisioUmberto.data.services.benchmark;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service("mongoService") // Il nome che useremo nel @Qualifier
@RequiredArgsConstructor
public class MongoSensorDataServiceImpl  {

    private final MongoTemplate mongoTemplate;
    
    public Double getAverageValueFromDb(String sensorId, String key, Date from, Date to) {
        // 1. Filtro: sensore specifico e intervallo temporale
        Criteria criteria = Criteria.where("sensorId").is(sensorId)
                .and("timestamp").gte(from).lte(to);


        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group().avg("payload." + key).as("avgValue")
        );

        // 3. Esecuzione
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "sensorData", Map.class);
        Map<String, Object> resultMap = results.getUniqueMappedResult();

        return (resultMap != null && resultMap.get("avgValue") != null)
                ? ((Number) resultMap.get("avgValue")).doubleValue() : 0.0;
    }

    public Double getAverageCO2FromDb(String sensorId) {
        // 1. Filtro: solo il sensore specificato (senza limiti di tempo)
        Criteria criteria = Criteria.where("sensorId").is(sensorId);

        // 2. Aggregazione: raggruppa tutti i documenti e calcola la media di payload.CO2
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group().avg("payload.CO2").as("avgValue")
        );

        // 3. Esecuzione
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "sensorData", Map.class);
        Map<String, Object> resultMap = results.getUniqueMappedResult();

        // 4. Restituisce il valore o 0.0 se non trova nulla
        return (resultMap != null && resultMap.get("avgValue") != null)
                ? ((Number) resultMap.get("avgValue")).doubleValue() : 0.0;
    }

}