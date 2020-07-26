package com.practice.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.practice.persistence.entities.RateAlertEntity;

@Repository
public interface RateAlertRepository extends JpaRepository<RateAlertEntity, Long>, RateAlertRepositoryCustom {

}
