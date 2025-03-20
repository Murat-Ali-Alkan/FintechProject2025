package com.murat.kafkaconsumer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.murat.kafkaconsumer.model.Rate;
import org.springframework.stereotype.Repository;

@Repository
public interface RateRepository extends JpaRepository<Rate, Long> {
}
