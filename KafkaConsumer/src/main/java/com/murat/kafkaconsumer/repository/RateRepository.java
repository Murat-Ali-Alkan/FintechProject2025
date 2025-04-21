package com.murat.kafkaconsumer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.murat.kafkaconsumer.model.Rate;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for accessing and managing {@link Rate} entities.
 *
 * <p>Extends {@link JpaRepository} to provide standard CRUD operations,
 * and query method support for the {@code Rate} entity.</p>
 *
 * <p>This interface is a part of the persistence layer and is used to
 * interact with the "rates" table in the database.</p>
 *
 * <p>Spring will automatically generate the implementation at runtime.</p>
 *
 * @see Rate
 * @see org.springframework.data.jpa.repository.JpaRepository
 */

@Repository
public interface RateRepository extends JpaRepository<Rate, Long> {
}
