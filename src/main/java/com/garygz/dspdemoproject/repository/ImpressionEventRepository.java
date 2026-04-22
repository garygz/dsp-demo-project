package com.garygz.dspdemoproject.repository;

import com.garygz.dspdemoproject.entity.ImpressionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ImpressionEventRepository extends JpaRepository<ImpressionEvent, UUID> {

    @Query("""
            SELECT i.occurredOn, COUNT(i)
            FROM ImpressionEvent i
            WHERE i.campaignId = :campaignId
              AND i.occurredOn BETWEEN :from AND :to
            GROUP BY i.occurredOn
            ORDER BY i.occurredOn
            """)
    List<Object[]> countPerDay(@Param("campaignId") UUID campaignId,
                               @Param("from") LocalDate from,
                               @Param("to") LocalDate to);
}
