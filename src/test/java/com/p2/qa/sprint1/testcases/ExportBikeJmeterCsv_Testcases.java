package com.p2.qa.sprint1.testcases;

import static io.restassured.RestAssured.given;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

public class ExportBikeJmeterCsv_Testcases extends Base {
    private static final String API_BASE_PATH = "/api";
    private static final String DEFAULT_OUTPUT = "target/jmeter-bike-details.csv";
    private static final String[] COMM_BOARD_LIST_PATHS = {
        "/communication-board?q=&limit=%d",
        "/comm-board?q=&limit=%d",
        "/commboard?q=&limit=%d"
    };

    private WebDriver driver;
    private Map<String, String> cookies;
    private int limit;
    private String outputPath;

    @BeforeClass
    public void setUp() {
        if (!Boolean.parseBoolean(System.getProperty("export.bikes.confirm", "false"))) {
            throw new SkipException(
                "Bike CSV export is guarded. Run with -Dexport.bikes.confirm=true."
            );
        }

        limit = Integer.parseInt(System.getProperty("export.bikes.limit", "1015"));
        outputPath = System.getProperty("export.bikes.output", DEFAULT_OUTPUT);

        driver = initializeBrowserAndOpenApplication();
        driver.manage().deleteAllCookies();

        Login loginPage = new Login();
        driver = loginPage.loginAs(driver, "admin");

        cookies = readSeleniumCookies();
        RestAssured.baseURI = ConfigReader.get("base.url").replaceAll("/+$", "");
    }

    @Test(description = "Export bike identifiers to a CSV file for JMeter load testing")
    public void exportBikeDetailsForJmeter() throws IOException {
        Response response = apiRequest().get(API_BASE_PATH + "/bikes?page=1&limit=" + limit);
        Assert.assertEquals(response.statusCode(), 200, "Failed to fetch bikes");

        List<Map<String, Object>> bikes = findBikeMaps(response.as(Object.class));
        Assert.assertFalse(bikes.isEmpty(), "No bike rows found in /api/bikes response");

        writeCsv(bikes, outputPath);
        System.out.println("Exported " + bikes.size() + " bikes to " + new File(outputPath).getAbsolutePath());
    }

    private void writeCsv(List<Map<String, Object>> bikes, String path) throws IOException {
        Map<String, String> commBoardIdentifiers = getIdentifierMapFromFirstWorkingPath(COMM_BOARD_LIST_PATHS, 5000);
        Map<String, String> vcuIdentifiers = getIdentifierMap("/vcu?q=&limit=5000");

        File output = new File(path);
        File parent = output.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(output));
        try {
            writer.write("device_id,comm_id,vcu_id,bike_id,uuid,bike_name,model_name,vin_number");
            writer.newLine();

            for (Map<String, Object> bike : bikes) {
                Map<String, String> flat = flatten(bike);
                String deviceId = firstValue(flat, "device_id", "device.id", "uuid", "bike_uuid");
                String commDbId = firstValue(flat, "comm_board_id", "communication_board_id", "commboard_id");
                String vcuDbId = firstValue(flat, "vcu_id");
                String commIdentifier = firstValue(flat, "comm.identifier", "comm_board.identifier",
                    "communication_board.identifier", "commboard.identifier");
                String vcuIdentifier = firstValue(flat, "vcu.identifier");

                List<String> values = new ArrayList<>();
                values.add(deviceId);
                values.add(resolveIdentifier(commIdentifier, commBoardIdentifiers, commDbId));
                values.add(resolveIdentifier(vcuIdentifier, vcuIdentifiers, vcuDbId));
                values.add(firstValue(flat, "id", "bike_id"));
                values.add(firstValue(flat, "uuid", "bike_uuid"));
                values.add(firstValue(flat, "bike_name", "name"));
                values.add(firstValue(flat, "model_name", "bike_model.model_name", "model.model_name"));
                values.add(firstValue(flat, "vin_number", "vin"));

                writer.write(toCsvLine(values));
                writer.newLine();
            }
        } finally {
            writer.close();
        }
    }

    private String resolveIdentifier(String directIdentifier, Map<String, String> identifiers, String dbId) {
        if (directIdentifier != null && !directIdentifier.trim().isEmpty()) {
            return directIdentifier.trim();
        }

        String mappedIdentifier = identifiers.get(dbId);
        if (mappedIdentifier != null && !mappedIdentifier.trim().isEmpty()) {
            return mappedIdentifier.trim();
        }

        return dbId == null ? "" : dbId;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> findBikeMaps(Object json) {
        List<Map<String, Object>> allMaps = new ArrayList<>();
        collectMaps(json, allMaps);

        List<Map<String, Object>> bikes = new ArrayList<>();
        for (Map<String, Object> map : allMaps) {
            if (map.containsKey("bike_name") || map.containsKey("vin_number")) {
                bikes.add(map);
            }
        }
        return bikes;
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

    private Map<String, String> flatten(Map<String, Object> source) {
        Map<String, String> flat = new LinkedHashMap<>();
        flattenInto("", source, flat);
        return flat;
    }

    @SuppressWarnings("unchecked")
    private void flattenInto(String prefix, Object value, Map<String, String> flat) {
        if (value == null) {
            return;
        }

        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                flattenInto(key, entry.getValue(), flat);
            }
        } else if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            for (int index = 0; index < list.size(); index++) {
                flattenInto(prefix + "." + index, list.get(index), flat);
            }
        } else {
            flat.put(prefix, String.valueOf(value));
        }
    }

    private String firstValue(Map<String, String> flat, String... keys) {
        for (String key : keys) {
            String value = flat.get(key);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private Map<String, String> getIdentifierMapFromFirstWorkingPath(String[] pathTemplates, int limit) {
        for (String template : pathTemplates) {
            Map<String, String> identifiers = getIdentifierMap(String.format(template, limit));
            if (!identifiers.isEmpty()) {
                return identifiers;
            }
        }
        return new HashMap<>();
    }

    private Map<String, String> getIdentifierMap(String path) {
        Response response = apiRequest().get(API_BASE_PATH + path);
        if (response.statusCode() != 200) {
            return new HashMap<>();
        }

        List<Map<String, Object>> maps = new ArrayList<>();
        collectMaps(response.as(Object.class), maps);

        Map<String, String> identifiers = new HashMap<>();
        for (Map<String, Object> map : maps) {
            Object id = map.get("id");
            Object identifier = map.get("identifier");
            if (id != null && identifier != null) {
                identifiers.put(String.valueOf(id), String.valueOf(identifier));
            }
        }
        return identifiers;
    }

    private String toCsvLine(List<String> values) {
        List<String> escaped = new ArrayList<>();
        for (String value : values) {
            escaped.add(escapeCsv(value));
        }
        return String.join(",", escaped);
    }

    private String escapeCsv(String value) {
        String safeValue = value == null ? "" : value;
        if (safeValue.contains(",") || safeValue.contains("\"") || safeValue.contains("\n")
            || safeValue.contains("\r")) {
            return "\"" + safeValue.replace("\"", "\"\"") + "\"";
        }
        return safeValue;
    }

    private io.restassured.specification.RequestSpecification apiRequest() {
        return given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .cookies(cookies);
    }

    private Map<String, String> readSeleniumCookies() {
        Map<String, String> result = new HashMap<>();
        for (Cookie cookie : driver.manage().getCookies()) {
            result.put(cookie.getName(), cookie.getValue());
        }
        return result;
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
