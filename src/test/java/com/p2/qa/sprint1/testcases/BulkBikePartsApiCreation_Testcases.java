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

public class BulkBikePartsApiCreation_Testcases extends Base {
    private static final String API_BASE_PATH = "/api";
    private static final String MANUFACTURED_DATE = "2026-05-03T18:15:00.000Z";
    private static final String PURCHASED_DATE = "2026-05-04T18:15:00.000Z";
    private static final String[] COMM_BOARD_ENDPOINTS = {
        "/communication-board",
        "/comm-board",
        "/commboard",
        "/communicationboard"
    };
    private static final String[] KEY_FOB_ENDPOINTS = {
        "/key-fob",
        "/keyfob",
        "/key-fobs",
        "/keyfobs"
    };

    private WebDriver driver;
    private Map<String, String> cookies;
    private int partSetCount;
    private int startIndex;
    private boolean includeKeyFob;
    private boolean includeCommBoard;
    private int requestDelayMs;
    private int retryCount;
    private int vendorId;

    @BeforeClass
    public void setUp() {
        if (!Boolean.parseBoolean(getProperty("bulk.parts.api.confirm", "false"))) {
            throw new SkipException(
                "API bulk bike-parts creation is guarded. Run with -Dbulk.parts.api.confirm=true."
            );
        }

        partSetCount = getIntProperty("bulk.parts.count", 1000);
        startIndex = getIntProperty("bulk.parts.start.index", 1);
        includeKeyFob = Boolean.parseBoolean(getProperty("bulk.parts.include.keyfob", "true"));
        includeCommBoard = Boolean.parseBoolean(getProperty("bulk.parts.include.commboard", "true"));
        requestDelayMs = getIntProperty("bulk.parts.request.delay.ms", 100);
        retryCount = getIntProperty("bulk.parts.retry.count", 5);

        driver = initializeBrowserAndOpenApplication();
        driver.manage().deleteAllCookies();

        Login loginPage = new Login();
        driver = loginPage.loginAs(driver, "admin");

        cookies = readSeleniumCookies();
        RestAssured.baseURI = ConfigReader.get("base.url").replaceAll("/+$", "");

        vendorId = getConfiguredOrFirstId("bulk.parts.vendor.id", "/vendor?q=&limit=1");
    }

    @Test(description = "Create bike parts directly through dashboard API for fast bulk bike creation")
    public void createBulkBikePartsThroughApi() {
        for (int offset = 0; offset < partSetCount; offset++) {
            int index = startIndex + offset;

            createPart("/battery", batteryPayload(index));
            createPart("/display", displayPayload(index));
            createPart("/charger", commonPayload("CHG", index));
            createPart("/motor-controller", commonPayload("MCR", index));
            createPart("/vcu", vcuPayload(index));
            createPart("/motor", commonPayload("MTR", index));

            if (includeKeyFob) {
                createPart(KEY_FOB_ENDPOINTS, keyFobPayload(index));
            }

            if (includeCommBoard) {
                createPart(COMM_BOARD_ENDPOINTS, communicationBoardPayload(index));
            }

            if ((offset + 1) % 100 == 0 || offset + 1 == partSetCount) {
                System.out.println("API bulk bike-parts progress: " + (offset + 1) + "/" + partSetCount);
            }
        }
    }

    private Map<String, Object> batteryPayload(int index) {
        Map<String, Object> payload = commonPayload("BAT", index);
        payload.put("bms_vendor_id", vendorId);
        payload.put("battery_type", "fixed");
        payload.put("soc", 90 + (index % 11));
        payload.put("soh", 95 + (index % 6));
        payload.put("cell_chemistry", "NMC");
        payload.put("series_parallel_string", "20S10P");
        payload.put("design_voltage", "72.0");
        payload.put("design_capacity", "50.0");
        payload.put("max_charging_current", "20.0");
        payload.put("max_discharging_current", "100.0");
        return payload;
    }

    private Map<String, Object> displayPayload(int index) {
        Map<String, Object> payload = commonPayload("DSP", index);
        payload.put("software_version_mcu", version("MCU", index));
        payload.put("software_version_arm", version("ARM", index));
        payload.put("software_version_fex", version("FEX", index));
        payload.put("hardware_type_mcu", hardwareType(index));
        payload.put("hardware_type_arm", hardwareType(index + 1));
        payload.put("hardware_type_fex", hardwareType(index + 2));
        return payload;
    }

    private Map<String, Object> vcuPayload(int index) {
        Map<String, Object> payload = commonPayload("VCU", index);
        payload.put("software_version", version("VCU", index));
        payload.put("hardware_type", hardwareType(index));
        return payload;
    }

    private Map<String, Object> keyFobPayload(int index) {
        Map<String, Object> payload = commonPayload("KEY", index);
        payload.put("ble_name", "P2-KEY-" + eightDigitHex("BLE", index));
        return payload;
    }

    private Map<String, Object> communicationBoardPayload(int index) {
        Map<String, Object> payload = commonPayload("COM", index);
        payload.put("hardware_type", hardwareType(index));
        payload.put("software_version", version("COM", index));
        payload.put("ble_name", "P2-COM-" + eightDigitHex("CBL", index));
        return payload;
    }

    private Map<String, Object> commonPayload(String partCode, int index) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("identifier", eightDigitHex(partCode, index));
        payload.put("manufactured_date", MANUFACTURED_DATE);
        payload.put("purchased_date", PURCHASED_DATE);
        payload.put("vendor_id", vendorId);
        return payload;
    }

    private void createPart(String endpoint, Map<String, Object> payload) {
        createPart(new String[] { endpoint }, payload);
    }

    private void createPart(String[] endpoints, Map<String, Object> payload) {
        Response response = null;
        for (String endpoint : endpoints) {
            response = postPart(endpoint, payload);
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                pause(requestDelayMs);
                return;
            }

            if (response.statusCode() == 404) {
                System.out.println("Part API endpoint not found: " + endpoint + ". Trying next alias.");
                continue;
            }

            Assert.fail("Part API create failed for " + endpoint + " with payload " + payload + ": "
                + response.asString());
        }

        Assert.fail("Part API create failed. None of these endpoints worked: "
            + String.join(", ", endpoints) + ". Last response: "
            + (response == null ? "none" : response.asString()));
    }

    private Response postPart(String endpoint, Map<String, Object> payload) {
        Response response = null;
        for (int attempt = 1; attempt <= retryCount; attempt++) {
            response = apiRequest()
                .body(payload)
                .post(API_BASE_PATH + endpoint);

            if (response.statusCode() == 400
                && payload.containsKey("purchased_date")
                && response.asString().contains("property purchased_date should not exist")) {
                Map<String, Object> payloadWithoutPurchaseDate = new LinkedHashMap<>(payload);
                payloadWithoutPurchaseDate.remove("purchased_date");
                response = apiRequest()
                    .body(payloadWithoutPurchaseDate)
                    .post(API_BASE_PATH + endpoint);
            }

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return response;
            }

            if (response.statusCode() != 429 && response.statusCode() < 500) {
                break;
            }

            int waitMs = Math.min(30000, 2000 * attempt);
            System.out.println("Part API " + endpoint + " returned " + response.statusCode()
                + ". Retry " + attempt + "/" + retryCount + " after " + waitMs + " ms.");
            pause(waitMs);
        }

        return response;
    }

    private void pause(int milliseconds) {
        if (milliseconds <= 0) {
            return;
        }
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting between API requests", e);
        }
    }

    private io.restassured.specification.RequestSpecification apiRequest() {
        return given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .cookies(cookies);
    }

    private int getConfiguredOrFirstId(String configKey, String path) {
        String configured = getProperty(configKey, "");
        if (!configured.isEmpty()) {
            return Integer.parseInt(configured);
        }

        List<Integer> ids = getIds(path, true);
        Assert.assertFalse(ids.isEmpty(), "No ID found from " + path);
        return ids.get(0);
    }

    private Integer getOptionalFirstId(String... paths) {
        for (String path : paths) {
            List<Integer> ids = getIds(path, false);
            if (!ids.isEmpty()) {
                return ids.get(0);
            }
        }
        return null;
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
        collectEntityIds(json, ids);
        return ids;
    }

    @SuppressWarnings("unchecked")
    private void collectEntityIds(Object value, List<Integer> ids) {
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            Object id = map.get("id");
            if (id != null && looksLikeEntity(map)) {
                ids.add(toInt(id));
            }
            for (Object child : map.values()) {
                collectEntityIds(child, ids);
            }
        } else if (value instanceof List) {
            for (Object child : (List<Object>) value) {
                collectEntityIds(child, ids);
            }
        }
    }

    private boolean looksLikeEntity(Map<String, Object> map) {
        return map.containsKey("identifier")
            || map.containsKey("name")
            || map.containsKey("vendor_name")
            || map.containsKey("company_name")
            || map.containsKey("hardware_version")
            || map.containsKey("hardware_type")
            || map.containsKey("created_at")
            || map.containsKey("updated_at");
    }

    private Map<String, String> readSeleniumCookies() {
        Map<String, String> result = new HashMap<>();
        for (Cookie cookie : driver.manage().getCookies()) {
            result.put(cookie.getName(), cookie.getValue());
        }
        return result;
    }

    private String eightDigitHex(String partCode, int index) {
        long partSeed = Math.abs(partCode.hashCode()) & 0xFFL;
        long timeSeed = System.currentTimeMillis() & 0xFFFL;
        long seed = (partSeed << 24) + (timeSeed << 12) + (index & 0xFFFL);
        String hex = Long.toHexString(seed).toUpperCase();
        return hex.substring(hex.length() - 8);
    }

    private String version(String component, int index) {
        return component + "-" + (2 + (index % 3)) + "." + (index % 10) + "." + ((index / 10) % 10);
    }

    private String hardwareType(int index) {
        String[] types = { "TYPE_ZERO", "TYPE_ONE", "TYPE_TWO", "TYPE_THREE", "TYPE_FOUR" };
        return types[Math.floorMod(index, types.length)];
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
