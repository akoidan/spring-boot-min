package com.example.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseVersionService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public String getPostgresVersion() {
        Object result = entityManager.createNativeQuery("select version()")
                .getSingleResult();
        return result == null ? "unknown" : result.toString();
    }
}
