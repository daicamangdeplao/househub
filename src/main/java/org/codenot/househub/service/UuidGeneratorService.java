package org.codenot.househub.service;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class UuidGeneratorService implements IdGeneratorService{

    @Value("${app.importer.knowledgeBase.directory}")
    private String knowledgeBaseDirectory;

    @Override
    public UUID generateId() {
        UUID timeOrdered = UuidCreator.getTimeOrdered();
        log.debug("Generated UUID: {}", timeOrdered);
        return timeOrdered;
    }
}
