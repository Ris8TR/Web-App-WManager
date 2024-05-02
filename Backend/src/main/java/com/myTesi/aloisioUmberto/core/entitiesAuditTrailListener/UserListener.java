package com.myTesi.aloisioUmberto.core.entitiesAuditTrailListener;

import com.myTesi.aloisioUmberto.data.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.mapping.event.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserListener extends AbstractMongoEventListener<User> {
    private final ModelMapper modelMapper = new ModelMapper();


    @Override
    public void onAfterLoad(AfterLoadEvent<User> event) {
        super.onAfterLoad(event);
        Document document = event.getSource();
        System.out.println(document);
        User user = modelMapper.map(document, User.class);
        user.setId(document.getObjectId("_id"));
        log.info("[USER AUDIT] user loaded from database: " + user.getId());
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<User> event) {
        super.onBeforeConvert(event);
        User user = event.getSource();
        if (user.getId() == null) {
            user.setId(new ObjectId());
        }
        log.info("[USER AUDIT] user with id: " + user.getId() + " is about to be converted");
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<User> event) {
        super.onBeforeSave(event);
        User user = event.getSource();
        log.info("[USER AUDIT] user with id: " + user.getId() + " is about to be saved");
    }

    @Override
    public void onBeforeDelete(BeforeDeleteEvent<User> event) {
        super.onBeforeDelete(event);
        ObjectId id = event.getSource().getObjectId("_id");
        log.info("[USER AUDIT] user with id: " + id + " is about to be deleted");
    }

}
