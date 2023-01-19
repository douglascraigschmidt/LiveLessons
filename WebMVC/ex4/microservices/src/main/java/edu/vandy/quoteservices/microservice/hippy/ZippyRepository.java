package edu.vandy.quoteservices.microservice.hippy;

import edu.vandy.quoteservices.common.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 */
@Repository
public interface ZippyRepository
       extends JpaRepository<Quote, Long> {
}
