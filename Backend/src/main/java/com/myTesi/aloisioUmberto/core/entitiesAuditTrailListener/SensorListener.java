package com.myTesi.aloisioUmberto.core.entitiesAuditTrailListener;

import com.myTesi.aloisioUmberto.data.entities.Sensor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.mapping.event.*;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class SensorListener extends AbstractMongoEventListener<Sensor> {

    @Override
    public void onAfterLoad(AfterLoadEvent<Sensor> event) {
        super.onAfterLoad(event);
        Document document = event.getSource();
        System.out.println(document);
        Sensor sensor = new ModelMapper().map(document, Sensor.class);
        sensor.setId(document.getObjectId("_id"));
        log.info("[SENSOR DATA AUDIT] sensor loaded from database: " + sensor.getId());
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Sensor> event) {
        super.onBeforeConvert(event);
        Sensor sensor = event.getSource();
        if (sensor.getId() == null) {
            sensor.setId(new ObjectId());
        }
        log.info("[SENSOR DATA AUDIT] sensor with id: " + sensor.getId() + " is about to be converted");
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<Sensor> event) {
        super.onBeforeSave(event);
        Sensor sensor = event.getSource();
        log.info("[SENSOR DATA AUDIT] sensor with id: " + sensor.getId() + " is about to be saved");
    }

    @Override
    public void onBeforeDelete(BeforeDeleteEvent<Sensor> event) {
        super.onBeforeDelete(event);
        ObjectId id = event.getSource().getObjectId("_id");
        log.info("[SENSOR DATA AUDIT] sensor with id: " + id + " is about to be deleted");
    }

}
