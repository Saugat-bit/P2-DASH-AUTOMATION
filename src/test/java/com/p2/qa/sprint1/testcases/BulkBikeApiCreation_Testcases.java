package com.p2.qa.sprint1.testcases;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.p2.automationbase.Base;
import com.p2.automationbase.Login;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import utils.ConfigReader;
import utils.TestDataGenerator;

public class BulkBikeApiCreation_Testcases extends Base {
    private static final String API_BASE_PATH = "/api";

    private WebDriver driver;
    private Map<String, String> cookies;
    private int bikeCount;
    private int startIndex;

    @BeforeClass
    public void setUp() {
        if (!Boolean.parseBoolean(getProperty("bulk.bike.api.confirm", "false"))) {
            throw new SkipException(
                "API bulk bike creation is guarded. Run with -Dbulk.bike.api.confirm=true."
            );
        }

        bikeCount = getIntProperty("bulk.bike.count", 1000);
        startIndex = getIntProperty("bulk.bike.start.index", 1);

        driver = initializeBrowserAndOpenApplication();
        driver.manage().deleteAllCookies();

        Login loginPage = new Login();
        driver = loginPage.loginAs(driver, "admin");

        cookies = readSeleniumCookies();
        RestAssured.baseURI = ConfigReader.get("base.url").replaceAll("/+$", "");
    }

    @Test(description = "Create bikes directly through dashboard API instead of slow UI form filling")
    public void createBulkBikesThroughApi() {
        int requiredParts = bikeCount;
        List<Integer> fixedBatteryIds = getIds("/battery?q=&limit=" + requiredParts + "&is_assigned=false&battery_type=fixed");
        List<Integer> displayIds = getIds("/display?q=&limit=" + requiredParts + "&is_assigned=false");
        List<Integer> chargerIds = getIds("/charger?q=&limit=" + requiredParts + "&is_assigned=false");
        List<Integer> controllerIds = getIds("/motor-controller?q=&limit=" + requiredParts + "&is_assigned=false");
        List<Integer> vcuIds = getIds("/vcu?q=&limit=" + requiredParts + "&is_assigned=false");
        List<Integer> motorIds = getIds("/motor?q=&limit=" + requiredParts + "&is_assigned=false");
        List<Integer> commBoardIds = getIds("/comm-board?q=&limit=" + requiredParts + "&is_assigned=false", false);

        int creatableCount = minimumSize(fixedBatteryIds, displayIds, chargerIds, controllerIds, vcuIds, motorIds);
        Assert.assertTrue(creatableCount >= bikeCount,
            "Not enough unassigned parts for " + bikeCount + " bikes. Available complete sets: " + creatableCount);

        Map<String, Object> model = getFirstMap("/bike-model?selectColumns=model_name,id,colors");
        int modelId = toInt(model.get("id"));
        String bikeColor = firstColorOrDefault(model.get("colors"), "black");

        for (int offset = 0; offset < bikeCount; offset++) {
            int bikeIndex = startIndex + offset;
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("bike_model_id", modelId);
            payload.put("bike_name", "Api Bike " + TestDataGenerator.getRandomName());
            payload.put("bike_color", bikeColor);
            payload.put("manufactured_date", "2026-05-03T18:15:00.000Z");
            payload.put("battery_id", fixedBatteryIds.get(offset));
            payload.put("vin_number", generateVin(bikeIndex));
            payload.put("charger_id", chargerIds.get(offset));
            payload.put("motor_controller_id", controllerIds.get(offset));
            payload.put("vcu_id", vcuIds.get(offset));
            payload.put("motor_id", motorIds.get(offset));
            payload.put("display_id", displayIds.get(offset));

            if (offset < commBoardIds.size()) {
                payload.put("comm_board_id", commBoardIds.get(offset));
            }

            Response response = apiRequest()
                .body(payload)
                .post(API_BASE_PATH + "/bikes");

            Assert.assertTrue(response.statusCode() == 200 || response.statusCode() == 201,
                "Bike API create failed at index " + bikeIndex + ": " + response.asString());

            if ((offset + 1) % 100 == 0 || offset + 1 == bikeCount) {
                System.out.println("API bulk bike progress: " + (offset + 1) + "/" + bikeCount);
            }
        }
    }

    private io.restassured.specification.RequestSpecification apiRequest() {
        return given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .cookies(cookies);
    }

    private List<Integer> getIds(String path) {
        return getIds(path, true);
    }

    private List<Integer> getIds(String path, boolean required) {
        Response response = apiRequest().get(API_BASE_PATH + path);
        if (required) {
            Assert.assertEquals(response.statusCode(), 200, "Failed to fetch IDs from " + path);
        } else if (response.statusCode() != 200) {
            return new ArrayList<>();
        }

        Object json = response.as(Object.class);
        List<Integer> ids = new ArrayList<>();
        collectLikelyEntityIds(json, ids);
        return ids;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getFirstMap(String path) {
        Response response = apiRequest().get(API_BASE_PATH + path);
        Assert.assertEquals(response.statusCode(), 200, "Failed to fetch data from " + path);

        List<Map<String, Object>> maps = new ArrayList<>();
        collectMaps(response.as(Object.class), maps);
        Assert.assertFalse(maps.isEmpty(), "No objects found in response from " + path);
        return (Map<String, Object>) maps.stream()
            .filter(item -> item.containsKey("id"))
            .findFirst()
            .orElse(maps.get(0));
    }

    @SuppressWarnings("unchecked")
    private void collectLikelyEntityIds(Object value, List<Integer> ids) {
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            Object id = map.get("id");
            if (id != null && looksLikeEntity(map)) {
                ids.add(toInt(id));
            }
            for (Object child : map.values()) {
                collectLikelyEntityIds(child, ids);
            }
        } else if (value instanceof List) {
            for (Object child : (List<Object>) value) {
                collectLikelyEntityIds(child, ids);
            }
        }
    }

    private boolean looksLikeEntity(Map<String, Object> map) {
        return map.containsKey("identifier")
            || map.containsKey("model_name")
            || map.containsKey("bike_name")
            || map.containsKey("created_at")
            || map.containsKey("updated_at");
    }

    @SuppressWarnings("unchecked")
    private void collectMaps(Object value, List<Map<String, Object>> maps) {
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            maps.add(map);
            for (Object child : map.values()) {
                collectMaps(child, maps);
            }
        } else if (value instanceof List) {
            for (Object child : (List<Object>) value) {
                collectMaps(child, maps);
            }
        }
    }

    private Map<String, String> readSeleniumCookies() {
        Map<String, String> result = new HashMap<>();
        for (Cookie cookie : driver.manage().getCookies()) {
            result.put(cookie.getName(), cookie.getValue());
        }
        return result;
    }

    private int minimumSize(List<?> first, List<?>... rest) {
        int min = first.size();
        for (List<?> list : rest) {
            min = Math.min(min, list.size());
        }
        return min;
    }

    private String firstColorOrDefault(Object colors, String defaultColor) {
        if (colors instanceof List && !((List<?>) colors).isEmpty()) {
            return String.valueOf(((List<?>) colors).get(0));
        }
        return defaultColor;
    }

    private String generateVin(int index) {
        String seed = Long.toHexString(System.currentTimeMillis()).toUpperCase()
            + Integer.toHexString(index).toUpperCase();
        return seed.substring(seed.length() - 8);
    }

    private int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private int getIntProperty(String key, int defaultValue) {
        return Integer.parseInt(getProperty(key, String.valueOf(defaultValue)));
    }

    private String getProperty(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.trim().isEmpty()) {
            return systemValue.trim();
        }

        String configValue = ConfigReader.get(key);
        if (configValue != null && !configValue.trim().isEmpty()) {
            return configValue.trim();
        }

        return defaultValue;
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
