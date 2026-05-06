package com.p2.qa.apitestcases;

 


import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import org.testng.annotations.Test;
import utils.BaseApiTest;

public class Apitestcases extends BaseApiTest {
    
    @Test(description = "when admin login in dash with valid credientials then status code should be 200/201 with proper role id ")
    public void testLoginRequest() {
        given()
            .spec(requestSpec)
        .when()
            .get("/users/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1));
    }
    
    @Test
    public void testPostRequest() {
        String requestBody = "{\n" +
            "  \"name\": \"John Doe\",\n" +
            "  \"email\": \"john@example.com\"\n" +
            "}";
        
        given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .body("name", equalTo("John Doe"));
    }
}