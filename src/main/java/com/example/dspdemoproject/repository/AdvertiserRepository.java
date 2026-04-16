package com.example.dspdemoproject.repository;

import com.example.dspdemoproject.entity.Advertiser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.InvalidObjectException;
import java.util.UUID;
import java.util.List;

@Repository
@Qualifier(value = "AdvertiserRepository")
public interface AdvertiserRepository extends JpaRepository<Advertiser, UUID> {
    List<Advertiser> findAdvertiserByName(String name);

    boolean existsAdvertiserByName(String name);
}
