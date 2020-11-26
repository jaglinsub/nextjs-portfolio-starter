package com.zuora.poc.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zuora.model.Order;
import com.zuora.model.POSTOrderRequestType;
import com.zuora.model.PostOrderResponseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    AuthTokenController authTokenController;
    @Autowired
    private Environment env;

    @PostMapping("createOrder")
    @ResponseStatus(HttpStatus.CREATED)
    PostOrderResponseType createOrder(@RequestBody POSTOrderRequestType order) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        logMessages(mapper, order, (POSTOrderRequestType.class.getName() + ":Request message"));
        String url = env.getProperty("baseURL") + "/v1/orders";
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, converter );

        PostOrderResponseType responseEntity = null;

        responseEntity = restTemplate.exchange(url, HttpMethod.POST,
                createOrderRequestHeaders(order), PostOrderResponseType.class).getBody();
        logMessages(mapper, responseEntity, (PostOrderResponseType.class.getName() + ":Response message"));
        return responseEntity;
    }

    private HttpEntity createOrderRequestHeaders(POSTOrderRequestType order) {
        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + authTokenController.getBearerToken());

        // create request
        HttpEntity<POSTOrderRequestType> requestHeaders = new HttpEntity<POSTOrderRequestType>(order, headers);
        return  requestHeaders;
    }

    private <T> void logMessages(ObjectMapper mapper, Object objClass, String message) {
        String jsonString = null;
        if(mapper == null) {
         mapper = new ObjectMapper();
        }
        try {
            jsonString = mapper.writeValueAsString(objClass);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println(message);
        System.out.println(jsonString);
    }
}
