package com.garygz.dspdemoproject.repository;

import com.garygz.dspdemoproject.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ClickEventRepository extends JpaRepository<ClickEvent, UUID> {

    @Query("""
            SELECT c.occurredOn, COUNT(c)
            FROM ClickEvent c
            WHERE c.campaignId = :campaignId
              AND c.occurredOn BETWEEN :from AND :to
            GROUP BY c.occurredOn
            ORDER BY c.occurredOn
            """)
    List<Object[]> countPerDay(@Param("campaignId") UUID campaignId,
                               @Param("from") LocalDate from,
                               @Param("to") LocalDate to);
}
