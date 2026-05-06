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

    private WebDriver driver;
    private Map<String, String> cookies;
    private int partSetCount;
    private int startIndex;
    private boolean includeCommBoard;
    private int vendorId;
    private Integer hardwareVersionId;
    private Integer hardwareTypeId;

    @BeforeClass
    public void setUp() {
        if (!Boolean.parseBoolean(getProperty("bulk.parts.api.confirm", "false"))) {
            throw new SkipException(
                "API bulk bike-parts creation is guarded. Run with -Dbulk.parts.api.confirm=true."
            );
        }

        partSetCount = getIntProperty("bulk.parts.count", 1000);
        startIndex = getIntProperty("bulk.parts.start.index", 1);
        includeCommBoard = Boolean.parseBoolean(getProperty("bulk.parts.include.commboard", "true"));

        driver = initializeBrowserAndOpenApplication();
        driver.manage().deleteAllCookies();

        Login loginPage = new Login();
        driver = loginPage.loginAs(driver, "admin");

        cookies = readSeleniumCookies();
        RestAssured.baseURI = ConfigReader.get("base.url").replaceAll("/+$", "");

        vendorId = getConfiguredOrFirstId("bulk.parts.vendor.id", "/vendor?q=&limit=1");
        hardwareVersionId = getOptionalFirstId(
            "/hardware-version?q=&limit=1",
            "/vcu-hardware-version?q=&limit=1",
            "/vcu/hardware-version?q=&limit=1"
        );
        hardwareTypeId = getOptionalFirstId(
            "/hardware-type?q=&limit=1",
            "/display-hardware-type?q=&limit=1",
            "/display/hardware-type?q=&limit=1"
        );
    }

    @Test(description = "Create bike parts directly through dashboard API for fast bulk bike creation")
    public void createBulkBikePartsThroughApi() {
        for (int offset = 0; offset < partSetCount; offset++) {
            int index = startIndex + offset;

            createPart("/battery", batteryPayload(index));
            createPart("/display", displayPayload(index));
            createPart("/charger", commonPayload("API-CHG-", index));
            createPart("/motor-controller", commonPayload("API-MC-", index));
            createPart("/vcu", vcuPayload(index));
            createPart("/motor", commonPayload("API-MTR-", index));

            if (includeCommBoard) {
                createPart("/comm-board", commonPayload("API-COMM-", index));
            }

            if ((offset + 1) % 100 == 0 || offset + 1 == partSetCount) {
                System.out.println("API bulk bike-parts progress: " + (offset + 1) + "/" + partSetCount);
            }
        }
    }

    private Map<String, Object> batteryPayload(int index) {
        Map<String, Object> payload = commonPayload("API-BAT-", index);
        payload.put("bms_vendor_id", vendorId);
        payload.put("battery_type", "fixed");
        payload.put("cell_chemistry", "NMC");
        payload.put("series_parallel_string", "20S10P");
        payload.put("design_voltage", 72);
        payload.put("design_capacity", 50);
        payload.put("max_charging_current", 20);
        payload.put("max_discharging_current", 100);
        return payload;
    }

    private Map<String, Object> displayPayload(int index) {
        Map<String, Object> payload = commonPayload("API-DSP-", index);
        payload.put("software_version_mcu", "1.0.0");
        payload.put("software_version_arm", "1.0.0");
        payload.put("software_version_fex", "1.0.0");
        if (hardwareTypeId != null) {
            payload.put("hardware_type_id", hardwareTypeId);
        }
        return payload;
    }

    private Map<String, Object> vcuPayload(int index) {
        Map<String, Object> payload = commonPayload("API-VCU-", index);
        payload.put("software_version", "1.0.0");
        if (hardwareVersionId != null) {
            payload.put("hardware_version_id", hardwareVersionId);
        }
        return payload;
    }

    private Map<String, Object> commonPayload(String prefix, int index) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("identifier", uniqueIdentifier(prefix, index));
        payload.put("manufactured_date", MANUFACTURED_DATE);
        payload.put("vendor_id", vendorId);
        payload.put("purchased_date", PURCHASED_DATE);
        return payload;
    }

    private void createPart(String endpoint, Map<String, Object> payload) {
        Response response = apiRequest()
            .body(payload)
            .post(API_BASE_PATH + endpoint);

        Assert.assertTrue(response.statusCode() == 200 || response.statusCode() == 201,
            "Part API create failed for " + endpoint + " with payload " + payload + ": " + response.asString());
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

    private String uniqueIdentifier(String prefix, int index) {
        return prefix + System.currentTimeMillis() + "-" + index;
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
