package com.garygz.dspdemoproject.service.impl;

import com.garygz.dspdemoproject.entity.Click;
import com.garygz.dspdemoproject.entity.Impression;
import com.garygz.dspdemoproject.repository.ClickRepository;
import com.garygz.dspdemoproject.repository.ImpressionRepository;
import com.garygz.dspdemoproject.service.DataIngestionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataIngestionServiceImpl implements DataIngestionService {

    private final ImpressionRepository impressionRepository;
    private final ClickRepository clickRepository;

    public DataIngestionServiceImpl(ImpressionRepository impressionRepository,
                                    ClickRepository clickRepository) {
        this.impressionRepository = impressionRepository;
        this.clickRepository = clickRepository;
    }

    @Override
    public void addClick(Click click) {
        clickRepository.save(click);
    }

    @Override
    public void addImpression(Impression impression) {
        impressionRepository.save(impression);
    }

    // DataIngestionServiceImpl.java
    @Override
    public void addImpressions(List<Impression> impressions) {
        impressionRepository.saveAll(impressions);
    }
}
