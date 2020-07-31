package com.practice.persistence.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.practice.persistence.entities.RateAlert;

@Repository
public interface RateAlertRepository extends JpaRepository<RateAlert, Long> {

    @Query("SELECT DISTINCT ra.base FROM RateAlert ra")
    List<String> findAllDistinctBases();


}
