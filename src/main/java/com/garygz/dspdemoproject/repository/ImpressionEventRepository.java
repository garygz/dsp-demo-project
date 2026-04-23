package com.garygz.dspdemoproject.repository;

import com.garygz.dspdemoproject.entity.ImpressionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import com.garygz.dspdemoproject.entity.ImpressionEventId;
import java.util.UUID;

public interface ImpressionEventRepository extends JpaRepository<ImpressionEvent, ImpressionEventId> {

    @Query("""
            SELECT CAST(i.occurredAt AS LocalDate), SUM(i.count)
            FROM ImpressionEvent i
            WHERE i.campaignId = :campaignId
              AND CAST(i.occurredAt AS LocalDate) BETWEEN :from AND :to
            GROUP BY CAST(i.occurredAt AS LocalDate)
            ORDER BY CAST(i.occurredAt AS LocalDate)
            """)
    List<Object[]> countPerDay(@Param("campaignId") UUID campaignId,
                               @Param("from") LocalDate from,
                               @Param("to") LocalDate to);
}
