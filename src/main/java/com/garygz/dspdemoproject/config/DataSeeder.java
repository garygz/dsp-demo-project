package com.garygz.dspdemoproject.config;

import com.garygz.dspdemoproject.entity.Advertiser;
import com.garygz.dspdemoproject.entity.Campaign;
import com.garygz.dspdemoproject.repository.AdvertiserRepository;
import com.garygz.dspdemoproject.repository.CampaignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Seeds reference data on first startup in production.
 * Runs after the application context is fully initialized.
 * Idempotent — checks for existing data before inserting.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AdvertiserRepository advertiserRepository;
    private final CampaignRepository campaignRepository;

    public DataSeeder(AdvertiserRepository advertiserRepository,
                      CampaignRepository campaignRepository) {
        this.advertiserRepository = advertiserRepository;
        this.campaignRepository = campaignRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (advertiserRepository.existsAdvertiserByName("Acme Corp")) {
            log.info("DataSeeder: data already present, skipping.");
            return;
        }

        log.info("DataSeeder: seeding reference data...");

        Advertiser acme = new Advertiser();
        acme.setName("Acme Corp");
        acme = advertiserRepository.save(acme);

        List<String[]> campaigns = List.of(
            new String[]{"Summer Sale 2025",               "https://acme.com/summer-sale",         "-60"},
            new String[]{"Back to School",                 "https://acme.com/back-to-school",       "-55"},
            new String[]{"Black Friday Deals",             "https://acme.com/black-friday",         "-50"},
            new String[]{"Cyber Monday Blowout",           "https://acme.com/cyber-monday",         "-45"},
            new String[]{"Holiday Gift Guide",             "https://acme.com/holiday-gifts",        "-40"},
            new String[]{"New Year New You",               "https://acme.com/new-year",             "-35"},
            new String[]{"Spring Collection Launch",       "https://acme.com/spring-collection",    "-30"},
            new String[]{"Valentine's Day Special",        "https://acme.com/valentines",           "-25"},
            new String[]{"Flash Sale Weekend",             "https://acme.com/flash-sale",           "-20"},
            new String[]{"Loyalty Rewards Program",        "https://acme.com/loyalty",              "-18"},
            new String[]{"Product Launch Q2",              "https://acme.com/product-launch-q2",    "-15"},
            new String[]{"Referral Bonus Campaign",        "https://acme.com/referral",             "-12"},
            new String[]{"App Download Push",              "https://acme.com/app",                  "-10"},
            new String[]{"Brand Awareness Q1",             "https://acme.com/brand-q1",             "-8"},
            new String[]{"Retargeting - Cart Abandonment", "https://acme.com/retarget-cart",        "-7"},
            new String[]{"Email Signup Incentive",         "https://acme.com/email-signup",         "-5"},
            new String[]{"Social Media Boost",             "https://acme.com/social",               "-4"},
            new String[]{"Influencer Collab Drop",         "https://acme.com/influencer-collab",    "-3"},
            new String[]{"Clearance Event",                "https://acme.com/clearance",            "-2"},
            new String[]{"Premium Membership Drive",       "https://acme.com/premium-membership",   "-1"}
        );

        for (String[] row : campaigns) {
            Campaign campaign = new Campaign();
            campaign.setName(row[0]);
            campaign.setLandingPage(row[1]);
            campaign.setCreatedAt(OffsetDateTime.now().plusDays(Long.parseLong(row[2])));
            campaign.setAdvertiser(acme);
            campaignRepository.save(campaign);
        }

        log.info("DataSeeder: seeded 1 advertiser and {} campaigns.", campaigns.size());
    }
}
