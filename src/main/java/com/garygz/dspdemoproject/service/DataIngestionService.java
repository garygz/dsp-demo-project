package com.garygz.dspdemoproject.service;

import com.garygz.dspdemoproject.entity.Advertiser;
import com.garygz.dspdemoproject.entity.Campaign;
import com.garygz.dspdemoproject.entity.Click;
import com.garygz.dspdemoproject.entity.Impression;

import java.util.List;
import java.util.UUID;

public interface DataIngestionService {

    void addClick(Click click);

    void addImporession(Impression impression);

}
