package com.garygz.dspdemoproject.service.impl;

import com.garygz.dspdemoproject.entity.Click;
import com.garygz.dspdemoproject.entity.Impression;
import com.garygz.dspdemoproject.repository.ClickRepository;
import com.garygz.dspdemoproject.repository.ImpressionRepository;
import com.garygz.dspdemoproject.service.DataIngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataIngestionServiceImpl implements DataIngestionService {

    @Autowired
    ImpressionRepository impressionRepository;

    @Autowired
    ClickRepository clickRepository;

    @Override
    public void addClick(Click click) {
        clickRepository.save(click);
    }

    @Override
    public void addImpression(Impression impression) {
        impressionRepository.save(impression);
    }
}
