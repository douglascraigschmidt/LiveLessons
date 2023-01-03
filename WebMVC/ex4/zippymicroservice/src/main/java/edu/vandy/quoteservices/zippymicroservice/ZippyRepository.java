package edu.vandy.quoteservices.zippymicroservice;

import edu.vandy.quoteservices.zippymicroservice.model.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 */
@Repository
public interface ZippyRepository
       extends JpaRepository<Quote, Integer> {
}
