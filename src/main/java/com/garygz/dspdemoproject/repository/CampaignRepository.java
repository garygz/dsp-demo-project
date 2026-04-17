package com.garygz.dspdemoproject.repository;

import com.garygz.dspdemoproject.entity.Advertiser;
import com.garygz.dspdemoproject.entity.Campaign;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@Qualifier(value = "CampaignRepository")
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {
    boolean existsCampaignByNameAndAdvertiser(String name, Advertiser advertiser);
    List<Campaign> findByAdvertiser(Advertiser advertiser);
}
