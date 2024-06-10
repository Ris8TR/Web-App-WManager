package com.myTesi.aloisioUmberto.core.entitiesAuditTrailListener;

import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.mapping.event.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InterestAreaListener extends AbstractMongoEventListener<InterestArea> {

    @Override
    public void onAfterLoad(AfterLoadEvent<InterestArea> event) {
        super.onAfterLoad(event);
        Document document = event.getSource();
        System.out.println(document);
        InterestArea interestArea = new ModelMapper().map(document, InterestArea.class);
        interestArea.setId(document.getObjectId("_id"));
        log.info("[INTEREST AREA AUDIT] interest area loaded from database: " + interestArea.getId());
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<InterestArea> event) {
        super.onBeforeConvert(event);
        InterestArea interestArea = event.getSource();
        if (interestArea.getId() == null) {
            interestArea.setId(new ObjectId());
        }
        log.info("[INTEREST AREA AUDIT] interest area with id: " + interestArea.getId() + " is about to be converted");
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<InterestArea> event) {
        super.onBeforeSave(event);
        InterestArea interestArea = event.getSource();
        log.info("[INTEREST AREA AUDIT] interest area with id: " + interestArea.getId() + " is about to be saved");
    }

    @Override
    public void onBeforeDelete(BeforeDeleteEvent<InterestArea> event) {
        super.onBeforeDelete(event);
        ObjectId id = event.getSource().getObjectId("_id");
        log.info("[INTEREST AREA AUDIT] interest area with id: " + id + " is about to be deleted");
    }
}
