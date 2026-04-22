package com.garygz.dspdemoproject.service;

import com.garygz.dspdemoproject.entity.Click;
import com.garygz.dspdemoproject.entity.Impression;

public interface DataIngestionService {

    void addClick(Click click);

    void addImpression(Impression impression);

}
