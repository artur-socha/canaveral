package com.codewise.canaveral2.mock.http;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class Recorder
{

    private static final Logger log = LoggerFactory.getLogger(Recorder.class);

    private final List<HttpRawRequest> recordedRequests = new ArrayList<>();

    void add(HttpRawRequest rawRequest)
    {
        log.trace("Recording new request {}. Recorded so far {}.", rawRequest, recordedRequests.size());
        recordedRequests.add(rawRequest);
    }

    List<HttpRawRequest> getLastRequests()
    {
        return ImmutableList.copyOf(recordedRequests);
    }

    void reset()
    {
        recordedRequests.clear();
    }
}
