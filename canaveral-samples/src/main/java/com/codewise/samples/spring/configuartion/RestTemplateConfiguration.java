package com.codewise.samples.spring.configuartion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

    @Bean
    public RestTemplate outboundRestTemplate(
            @Value("${com.codewise.rest.client.default.timeout:100}") int restHttpReadTimeoutMillis,
            MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        return createRestTemplate(mappingJackson2HttpMessageConverter, restHttpReadTimeoutMillis);
    }

    private RestTemplate createRestTemplate(
            MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter,
            int restHttpReadTimeoutMillis) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(restHttpReadTimeoutMillis);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getMessageConverters().clear();
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);
        return restTemplate;
    }
}
