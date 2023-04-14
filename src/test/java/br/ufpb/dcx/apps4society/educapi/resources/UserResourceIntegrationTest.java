package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.dto.user.UserDTO;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;
import br.ufpb.dcx.apps4society.educapi.utils.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.response.*;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.restassured.RestAssured.*;

public class UserResourceIntegrationTest{

    private static String NAME = "jose";
    private static String EMAIL = "jose@educapi.com";
    private static String PASSWORD = "12345678";
    private static String USER_POST_ENDPOINT = baseURI+":"+port+basePath+"users";
    private static String USER_AUTENTICATION_ENDPOINT = baseURI+":"+port+basePath+"auth/login";

    private static String CONTEXT_POST_ENDPOINT = baseURI+":"+port+basePath+"contexts";
    private static String invalidToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb3NlMTdAZWR1Y2FwaS5jb20iLCJleHAiOjE2ODA2OTc2MjN9.qfwlZuirBvosD82v-7lHxb8qhH54_KXR20_0z3guG9rZOW68l5y3gZtvugBtpevmlgK76dsa4hOUPOooRiJ3ng";

    @BeforeEach
    public void setUp(){

        baseURI = "http://localhost";
        port = 8080;
        basePath = "/v1/api/";

    }

    @Test
    public void insertUserByNameEmailPassword_shouldReturn201Test() throws Exception {

        Response userDTOResponse = given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
               .contentType(ContentType.JSON)
       .when()
               .post(baseURI+":"+port+basePath+"users")
       .then()
               .assertThat()
               .statusCode(201)
                
               .extract().response();

        JSONObject userDTOJSONActual = new JSONObject(userDTOResponse.getBody().prettyPrint());

        String actualUserId = userDTOJSONActual.getString("id");
        String actualUserName = userDTOJSONActual.getString("name");
        String actualUserPassword = userDTOJSONActual.getString("password");
        String actualUserEmail = userDTOJSONActual.getString("email");

        UserDTO userDTO = UserBuilder.anUser()
                .withId(Long.valueOf(actualUserId))
                .withName(actualUserName)
                .withEmail(actualUserEmail)
                .withPassword(actualUserPassword).buildUserDTO();

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("src/test/resources/USER_ActualUserDTOBody.json"), userDTO);
        JSONObject userRegisterDTOJSONExpected = new JSONObject(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(userDTOJSONActual.getString("id"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("name"), userDTOJSONActual.getString("name"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("email"), userDTOJSONActual.getString("email"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("password"), userDTOJSONActual.getString("password"));

        //Just to remove user from DB
        String token = given().body(FileUtils.getJsonFromFile("USER_AuthenticateUserBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then().extract().path("token");

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users");

    }

    @Test
    public void insertUserByNameEmailPasswordAlreadyExists_shouldReturn201Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
        .when()
                .post(baseURI+":"+port+basePath+"users");

        given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
        .when()
                .post(baseURI+":"+port+basePath+"users")
        .then()
                .assertThat()
                .statusCode(204)
                .log().all();

        //Just to remove user from DB
        String token = given().body(FileUtils.getJsonFromFile("USER_AuthenticateUserBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then().extract().path("token");

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users");

    }

    @Test
    public void insertUserByInvalidEmail_ShouldReturn400Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_POST_PUT_UserInvalidEmailBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users")
                .then()
                .assertThat()
                .statusCode(400)
                .log().all();
    }

    @Test
    public void insertUserByInvalidPasswordLessThan8Characters_ShouldReturn400Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_POST_PUT_UserInvalidPasswordLessThan8Body.json"))
                .contentType(ContentType.JSON)
            .when()
                .post(baseURI+":"+port+basePath+"users")
            .then()
                .assertThat()
                .statusCode(400)
                .log().all();

    }

    @Test
    public void insertUserByInvalidPasswordMoreThan8Characters_ShouldReturn400Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_POST_PUT_UserInvalidPasswordMoreThan12Body.json"))
                .contentType(ContentType.JSON)
            .when()
                .post(baseURI+":"+port+basePath+"users")
            .then()
                .assertThat()
                .statusCode(400)
                .log().all()
                .extract().response();

    }

    //_____________________________________________________________________________________________________

    @Test
    public void authenticateUserByEmailPassword_shouldReturn201Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        String token = given().body(FileUtils.getJsonFromFile("USER_AuthenticateUserBody.json"))
                .contentType(ContentType.JSON)
        .when()
                .post(baseURI+":"+port+basePath+"auth/login")
        .then()
                .assertThat()
                .statusCode(200)
                .log().all().extract().path("token");

        Assertions.assertNotNull(token);

        //Just to remove user from DB
        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users");

    }


    @Test
    public void authenticateUserInexistent_ShouldReturn401() throws Exception {

        given().body(FileUtils.getJsonFromFile("USER_AuthenticateUserBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login").then().assertThat().statusCode(401);

    }

    @Test
    public void authenticateUserByInvalidEmail_shouldReturn401Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        given().body(FileUtils.getJsonFromFile("USER_AuthenticateUserInvalidEmailBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .assertThat()
                .statusCode(401)
                .log().all();

    }

    @Test
    public void authenticateUserByInvalidPasswordLessThan8Characters_shouldReturn401Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        given().body(FileUtils.getJsonFromFile("USER_AuthenticateUserPasswordLessThan8Body.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .assertThat()
                .statusCode(401)
                .log().all();

    }

    @Test
    public void authenticateUserByInvalidPasswordMoreThan12Characters_shouldReturn401Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        given().body(FileUtils.getJsonFromFile("USER_AuthenticateUserPasswordMoreThan8Body.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .assertThat()
                .statusCode(401)
                .log().all();

    }

    //______________________________________________________________________________________________________

    @Test
    public void findUserByToken_ShouldReturn200Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        String token = given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                    .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                    .log().all()
                    .extract().path("token");

        Response userDTOResponse = given()
                    .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                    .get(baseURI+":"+port+basePath+"auth/users")
                .then()
                    .assertThat().statusCode(200)
                    .extract().response();

        JSONObject userDTOJSON_Actual = new JSONObject(userDTOResponse.getBody().prettyPrint());

        String actualUserId = userDTOJSON_Actual.getString("id");
        String actualUserName = userDTOJSON_Actual.getString("name");
        String actualUserEmail = userDTOJSON_Actual.getString("email");

        UserDTO userDTO = UserBuilder.anUser()
                .withId(Long.valueOf(actualUserId))
                .withName(actualUserName)
                .withEmail(actualUserEmail)
                .withPassword(null).buildUserDTO();

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("src/test/resources/USER_ActualUserDTOBody.json"), userDTO);
        JSONObject userRegisterDTOJSON_Expected = new JSONObject(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(userDTOJSON_Actual.getString("id"));
        Assertions.assertEquals(userRegisterDTOJSON_Expected.getString("name"), userDTOJSON_Actual.getString("name"));
        Assertions.assertEquals(userRegisterDTOJSON_Expected.getString("email"), userDTOJSON_Actual.getString("email"));

        //Just to remove user from DB
        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users");

    }

    @Test
    public void findInexistentUserByToken_ShouldReturn500Test() throws Exception {

        // Create user
        given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        //Authenticate user
        String token = given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        //Delete user
        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users");

        //Try to get a inexistent user
        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(500);

    }

    @Test
    public void findUserByMalformedOrInvalidToken_ShouldReturn500Test() throws Exception {

        given()
                .headers(
                        "Authorization",
                        "Bearer " + invalidToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(500);

    }

    //______________________________________________________________________________________________________

    @Test
    public void updateUserByEmailAndNameAndPasswordAndToken_ShouldReturn200Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        String token = given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        Response userDTOResponse = given()
                    .headers(
                            "Authorization",
                            "Bearer " + token,
                            "Content-Type",
                            ContentType.JSON,
                            "Accept",
                            ContentType.JSON)
                    .body(FileUtils.getJsonFromFile("USER_PUT_UserBody.json"))
                    .contentType(ContentType.JSON)
                .when()
                    .put(baseURI+":"+port+basePath+"auth/users")
                .then()
                    .assertThat().statusCode(200)
                    .log().all()
                    .extract()
                    .response();

        JSONObject userDTOJSONActual = new JSONObject(userDTOResponse.getBody().prettyPrint());

        String actualUserId = userDTOJSONActual.getString("id");
        String actualUserName = userDTOJSONActual.getString("name");
        String actualUserPassword = userDTOJSONActual.getString("password");
        String actualUserEmail = userDTOJSONActual.getString("email");

        UserDTO userDTO = UserBuilder.anUser()
                .withId(Long.valueOf(actualUserId))
                .withName(actualUserName)
                .withEmail(actualUserEmail)
                .withPassword(actualUserPassword).buildUserDTO();

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("src/test/resources/USER_ActualUserDTOBody.json"), userDTO);
        JSONObject userRegisterDTOJSONExpected = new JSONObject(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(userDTOJSONActual.getString("id"));
        Assertions.assertNotEquals(userRegisterDTOJSONExpected.getString("name"), userDTOJSONActual.getString("name"));
        Assertions.assertNotEquals(userRegisterDTOJSONExpected.getString("email"), userDTOJSONActual.getString("email"));
        Assertions.assertNotEquals(userRegisterDTOJSONExpected.getString("password"), userDTOJSONActual.getString("password"));

        //Just to remove user from DB
        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users");

    }

    @Test
    public void updateInexistentUserByEmailAndNameAndPasswordAndToken_ShouldReturn500Test() throws Exception {

        // Create user
        given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        //Authenticate user
        String token = given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        //Delete user
        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users");

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(FileUtils.getJsonFromFile("USER_PUT_UserBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(500);

    }

    @Test
    public void updateUserByInvalidEmail_ShouldReturn400Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        String token = given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(FileUtils.getJsonFromFile("USER_POST_PUT_UserInvalidEmailBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(400);

        //Just to remove user from DB
        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users");

    }

    @Test
    public void updateUserByPasswordLessThan8Characters_ShouldReturn403Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        String token = given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(FileUtils.getJsonFromFile("USER_POST_PUT_UserInvalidPasswordLessThan8Body.json"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(400);

        //Just to remove user from DB
        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users");
    }

    @Test
    public void updateUserByPasswordMoreThan8Characters_ShouldReturn403Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        String token = given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(FileUtils.getJsonFromFile("USER_POST_PUT_UserInvalidPasswordMoreThan12Body.json"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(400);

        //Just to remove user from DB
        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users");
    }

    @Test
    public void updateUserByMalformedOrInvalidToken_ShouldReturn500Test() throws Exception {

        given()
                .headers(
                        "Authorization",
                        "Bearer " + invalidToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(500);

    }


    //______________________________________________________________________________________________________

    @Test
    public void deleteUserByToken_ShouldReturn200Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        String token = given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        Response userDTOResponse = given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(200)
                .extract().response();

        JSONObject userDTOJSONActual = new JSONObject(userDTOResponse.getBody().prettyPrint());

        String actualUserId = userDTOJSONActual.getString("id");
        String actualUserName = userDTOJSONActual.getString("name");
        String actualUserPassword = userDTOJSONActual.getString("password");
        String actualUserEmail = userDTOJSONActual.getString("email");

        UserDTO userDTO = UserBuilder.anUser()
                .withId(Long.valueOf(actualUserId))
                .withName(actualUserName)
                .withEmail(actualUserEmail)
                .withPassword(actualUserPassword).buildUserDTO();

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("src/test/resources/USER_ActualUserDTOBody.json"), userDTO);
        JSONObject userRegisterDTOJSONExpected = new JSONObject(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(userDTOJSONActual.getString("id"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("name"), userDTOJSONActual.getString("name"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("email"), userDTOJSONActual.getString("email"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("password"), userDTOJSONActual.getString("password"));

    }

    @Test
    public void deleteInexistentUserByToken_ShouldReturn500Test() throws Exception {

        // Create user
        given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        //Authenticate user
        String token = given()
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                 
                .extract().path("token");

        //Delete user
        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users");

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(500);

    }

    @Test
    public void deleteUserByMalformedOrInvalidToken_ShouldReturn500Test() throws Exception {

        given()
                .headers(
                        "Authorization",
                        "Bearer " + invalidToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(500);

    }

//    https://www.youtube.com/watch?v=Y4_LmPhx1Jc
//    https://www.youtube.com/watch?v=l5WfHfHvqo8
//    https://www.youtube.com/watch?v=3duamjhP7NM

}