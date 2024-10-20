package com.ahogek.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

/**
 * @author AhogeK ahogek@gmail.com
 * @since 2024-10-20 04:25:44
 */
@QuarkusTest
class FileSystemResourceTest {

    @Test
    void testFilesDataOpIsOk() {
        RestAssured.given().when().get("/files/test")
                .then().statusCode(200)
                .body(CoreMatchers.notNullValue());
    }
}
