package org.motechproject.http.client.domain;

import org.springframework.web.client.RestTemplate;

public enum Method {
    POST {
        @Override
        public void execute(RestTemplate restTemplate, String url, Object request) {
            restTemplate.postForLocation(url, request);
        }
    },

    PUT {
        @Override
        public void execute(RestTemplate restTemplate, String url, Object request) {
            restTemplate.put(url, request);
        }
    };

    public abstract void execute(RestTemplate restTemplate, String url, Object request);
}
