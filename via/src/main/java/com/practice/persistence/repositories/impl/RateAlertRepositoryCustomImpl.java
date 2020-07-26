package com.practice.persistence.repositories.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.practice.persistence.repositories.RateAlertRepositoryCustom;

public class RateAlertRepositoryCustomImpl implements RateAlertRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<String> getBases() {
        return entityManager.createQuery("SELECT DISTINCT ra.base FROM RateAlert ra", String.class)
                            .getResultList();
    }

}
