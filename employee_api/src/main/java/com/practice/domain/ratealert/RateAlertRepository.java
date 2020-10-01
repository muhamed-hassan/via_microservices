package com.practice.domain.ratealert;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RateAlertRepository extends JpaRepository<RateAlert, Long> {

    @Query("SELECT DISTINCT ra.base FROM RateAlert ra")
    List<String> findAllDistinctBases();

}
