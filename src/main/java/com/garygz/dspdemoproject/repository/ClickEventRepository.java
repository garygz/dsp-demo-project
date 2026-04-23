package com.garygz.dspdemoproject.repository;

import com.garygz.dspdemoproject.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import com.garygz.dspdemoproject.entity.ClickEventId;
import java.util.UUID;

public interface ClickEventRepository extends JpaRepository<ClickEvent, ClickEventId> {

    @Query("""
            SELECT CAST(c.occurredAt AS LocalDate), SUM(c.count)
            FROM ClickEvent c
            WHERE c.campaignId = :campaignId
              AND CAST(c.occurredAt AS LocalDate) BETWEEN :from AND :to
            GROUP BY CAST(c.occurredAt AS LocalDate)
            ORDER BY CAST(c.occurredAt AS LocalDate)
            """)
    List<Object[]> countPerDay(@Param("campaignId") UUID campaignId,
                               @Param("from") LocalDate from,
                               @Param("to") LocalDate to);
}
