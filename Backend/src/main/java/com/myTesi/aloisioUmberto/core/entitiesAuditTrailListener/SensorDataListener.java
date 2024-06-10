package com.myTesi.aloisioUmberto.core.entitiesAuditTrailListener;

import com.myTesi.aloisioUmberto.data.entities.SensorData;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.mapping.event.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SensorDataListener extends AbstractMongoEventListener<SensorData> {

    @Override
    public void onAfterLoad(AfterLoadEvent<SensorData> event) {
        super.onAfterLoad(event);
        Document document = event.getSource();
        System.out.println(document);
        SensorData sensorData = new ModelMapper().map(document, SensorData.class);
        sensorData.setId(document.getObjectId("_id"));
        log.info("[SENSOR DATA AUDIT] sensor data loaded from database: " + sensorData.getId());
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<SensorData> event) {
        super.onBeforeConvert(event);
        SensorData sensorData = event.getSource();
        if (sensorData.getId() == null) {
            sensorData.setId(new ObjectId());
        }
        log.info("[SENSOR DATA AUDIT] sensor data with id: " + sensorData.getId() + " is about to be converted");
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<SensorData> event) {
        super.onBeforeSave(event);
        SensorData sensorData = event.getSource();
        log.info("[SENSOR DATA AUDIT] sensor data with id: " + sensorData.getId() + " is about to be saved");
    }

    @Override
    public void onBeforeDelete(BeforeDeleteEvent<SensorData> event) {
        super.onBeforeDelete(event);
        ObjectId id = event.getSource().getObjectId("_id");
        log.info("[SENSOR DATA AUDIT] sensor data with id: " + id + " is about to be deleted");
    }
}
