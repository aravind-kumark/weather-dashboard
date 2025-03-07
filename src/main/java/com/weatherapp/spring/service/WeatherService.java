package com.weatherapp.spring.service;

import com.weatherapp.spring.model.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    @Value("${weather.api.url}")
    private String apiBaseUrl;

    @Value("${weather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public WeatherResponse getWeather(String city) {
        try {
            String url = String.format("%s/current.json?key=%s&q=%s", apiBaseUrl, apiKey, city);
            logger.info("Fetching weather data from: {}", url.replace(apiKey, "API_KEY_HIDDEN"));

            WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);
            if (response == null) {
                logger.error("Received null response from weather API");
                return null;
            }

            logger.debug("Weather data processed successfully for city: {}", city);
            return response;

        } catch (RestClientException e) {
            logger.error("Error fetching weather data: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error processing weather data: {}", e.getMessage());
            return null;
        }
    }

    public List<Map<String, Object>> getForecast(String city, int days) {
        try {
            String url = String.format("%s/forecast.json?key=%s&q=%s&days=%d", 
                    apiBaseUrl.replace("/current.json", ""), 
                    apiKey, 
                    city, 
                    days);

            logger.info("Fetching forecast data from: {}", url.replace(apiKey, "API_KEY_HIDDEN"));

            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null) {
                logger.error("Received null response from forecast API");
                return null;
            }

            // Parse the JSON response
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode forecastDays = rootNode.path("forecast").path("forecastday");

            List<Map<String, Object>> forecastList = new ArrayList<>();

            // Process each forecast day
            for (JsonNode dayNode : forecastDays) {
                Map<String, Object> dayForecast = new HashMap<>();

                // Get the date
                String date = dayNode.path("date").asText();
                dayForecast.put("date", date);

                // Get the day of week
                dayForecast.put("day_of_week", getDayOfWeek(date));

                // Get the day information
                JsonNode dayInfo = dayNode.path("day");

                dayForecast.put("max_temp", dayInfo.path("maxtemp_c").asDouble());
                dayForecast.put("min_temp", dayInfo.path("mintemp_c").asDouble());
                dayForecast.put("avg_temp", dayInfo.path("avgtemp_c").asDouble());

                // Get condition
                JsonNode condition = dayInfo.path("condition");
                dayForecast.put("condition_text", condition.path("text").asText());
                dayForecast.put("condition_icon", condition.path("icon").asText());

                // Add additional data as needed
                dayForecast.put("max_wind_kph", dayInfo.path("maxwind_kph").asDouble());
                dayForecast.put("avg_humidity", dayInfo.path("avghumidity").asDouble());
                dayForecast.put("chance_of_rain", dayInfo.path("daily_chance_of_rain").asInt());

                forecastList.add(dayForecast);
            }

            logger.debug("Forecast data processed successfully for city: {}", city);
            return forecastList;

        } catch (RestClientException e) {
            logger.error("Error fetching forecast data: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error processing forecast data: {}", e.getMessage());
            return null;
        }
    }

    // Simple method to get day of week from date
    private String getDayOfWeek(String dateStr) {
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            return date.getDayOfWeek().toString().substring(0, 3);
        } catch (Exception e) {
            return "";
        }
    }
}