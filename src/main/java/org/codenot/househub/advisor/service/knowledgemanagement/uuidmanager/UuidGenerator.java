package org.codenot.househub.advisor.service.knowledgemanagement.uuidmanager;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class UuidGenerator implements IdGenerator {

    @Override
    public UUID generateId() {
        UUID timeOrdered = UuidCreator.getTimeOrdered();
        log.debug("Generated UUID: {}", timeOrdered);
        return timeOrdered;
    }
}
