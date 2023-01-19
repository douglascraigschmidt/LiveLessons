package edu.vandy.quoteservices.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 */
@Repository
public interface QuoteRepository
       extends JpaRepository<Quote, Long> {
}
