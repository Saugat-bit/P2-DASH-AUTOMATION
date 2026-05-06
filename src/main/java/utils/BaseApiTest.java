
package utils;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

public class BaseApiTest {
    
    protected static RequestSpecification requestSpec;
    
    @BeforeClass
    public void setupAPI() {
        RestAssured.baseURI = "";
        RestAssured.basePath = "";
        
        requestSpec = new RequestSpecBuilder()
            .setContentType("application/json")
            .setAccept("application/json")
            .build();
    }
}