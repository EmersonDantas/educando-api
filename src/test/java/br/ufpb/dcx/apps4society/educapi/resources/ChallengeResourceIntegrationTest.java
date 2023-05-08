package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.EducApiApplicationTests;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeDTO;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ChallengeBuilder;
import br.ufpb.dcx.apps4society.educapi.utils.CHALLENGE_RequestsUtil;
import br.ufpb.dcx.apps4society.educapi.utils.CONTEXT_RequestsUtil;
import br.ufpb.dcx.apps4society.educapi.utils.FileUtils;
import br.ufpb.dcx.apps4society.educapi.utils.USER_RequestsUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.restassured.RestAssured.*;

public class ChallengeResourceIntegrationTest extends EducApiApplicationTests {

    @Test
    public void insertChallengeByCreatorTokenBodyContextID_ShouldReturn201Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        //Create challenge
        Response challengeDTOResponse = given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + contextID)
                .then()
                .assertThat().statusCode(201)
                .extract().response();

        JSONObject challengeDTOJSONActual = new JSONObject(challengeDTOResponse.getBody().prettyPrint());

        String challengeIDActual = challengeDTOJSONActual.getString("id");
        String challengeImageURLActual = challengeDTOJSONActual.getString("imageUrl");
        String challengeWordActual = challengeDTOJSONActual.getString("word");
        String challengeSoundURLActual = challengeDTOJSONActual.getString("soundUrl");
        String challengeVideoURLActual = challengeDTOJSONActual.getString("videoUrl");

        ChallengeDTO challengeDTOActual = ChallengeBuilder.anChallenge()
                .withId(Long.valueOf(challengeIDActual))
                .withWord(challengeWordActual)
                .withImageUrl(challengeImageURLActual)
                .withSoundUrl(challengeSoundURLActual)
                .withVideoUrl(challengeVideoURLActual).buildChallengeDTO();

        ObjectMapper mapper =  new ObjectMapper();
        mapper.writeValue(new File("src/test/resources/CHALLENGE_ActualContextDTOBody[spawned].json"), challengeDTOActual);
        JSONObject challengeJSONExpected = new JSONObject(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(challengeDTOJSONActual.getString("id"));
        Assertions.assertEquals(challengeJSONExpected.getString("word"), challengeDTOJSONActual.getString("word"));
        Assertions.assertEquals(challengeJSONExpected.getString("imageUrl"), challengeDTOJSONActual.getString("imageUrl"));
        Assertions.assertEquals(challengeJSONExpected.getString("soundUrl"), challengeDTOJSONActual.getString("soundUrl"));
        Assertions.assertEquals(challengeJSONExpected.getString("videoUrl"), challengeDTOJSONActual.getString("videoUrl"));

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }

    @Test
    public void insertChallengeByAlreadyExistingWord_ShouldReturn201Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        //Create challenge
        Response challengeDTOResponse = given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + contextID)
                .then()
                .assertThat().statusCode(201)
                .extract().response();

        JSONObject challengeDTOJSONActual = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDActual = challengeDTOJSONActual.getString("id");

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }

    @Test
    public void insertChallengeByInvalidWordLessThan2Characters_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_AuthenticateBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        given().body(FileUtils.getJsonFromFile("CHALLENGE_POST_InvalidWordLessThan2CharactersBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"auth/challenges/" + contextID)
                .then().assertThat().statusCode(400);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);

    }

    @Test
    public void insertChallengeByInvalidWordMoreThan72Characters_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_AuthenticateBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        given().body(FileUtils.getJsonFromFile("CHALLENGE_POST_InvalidWordMoreThan72CharactersBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"auth/challenges/" + contextID)
                .then().assertThat().statusCode(400);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);

    }

    @Test
    public void insertChallengeMissingID_ShouldReturn405Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_AuthenticateBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        given().body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"auth/challenges")
                .then().assertThat().statusCode(405);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);

    }

    @Test
    public void insertChallengeByContextIDLinkedToMissingContext_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        CONTEXT_RequestsUtil.deleteContext(token, contextID);

        //Create challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + contextID)
                .then()
                .assertThat().statusCode(404);

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void insertChallengeByNonNumericID_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        //Create challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + "abc")
                .then()
                .assertThat().statusCode(400);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);

    }

    @Test
    public void insertChallengeMissingToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        //Create challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + contextID)
                .then()
                .assertThat().statusCode(400);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);

    }

    @Test
    public void insertChallengeByCreatorTokenLinkedToMissingCreator_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        USER_RequestsUtil.deleteUser(token);

        //Create challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + contextID)
                .then()
                .assertThat().statusCode(404);

        CONTEXT_RequestsUtil.deleteContext(token, contextID);

    }

    @Test
    public void insertChallengeByMalformedOrExpiredToken_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        //Create challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + invalidToken,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + contextID)
                .then()
                .assertThat().statusCode(500);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);

    }


    @Test
    public void findChallengesByCreatorToken_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        //Read challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/")
                .then()
                .assertThat().statusCode(200);

        JSONObject challengeDTOActualJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDActual = challengeDTOActualJSON.getString("id");

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }

    @Test
    public void findChallengesByMissingToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        //Read challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/")
                .then()
                .assertThat().statusCode(400);

        JSONObject challengeDTOActualJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDActual = challengeDTOActualJSON.getString("id");

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }

    @Test
    public void findChallengesByCreatorTokenLinkedToMissingCreator_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        USER_RequestsUtil.deleteUser(token);

        //Read challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/")
                .then()
                .assertThat().statusCode(404);

        JSONObject challengeDTOActualJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDActual = challengeDTOActualJSON.getString("id");

        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);


    }

    @Test
    public void findChallengesByMalformedOrExpiredToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        //Read challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + invalidToken,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/")
                .then()
                .assertThat().statusCode(500);

        JSONObject challengeDTOActualJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDActual = challengeDTOActualJSON.getString("id");

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }


    @Test
    public void findChallengeByCreatorTokenID_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        JSONObject challengeDTOActualJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDActual = challengeDTOActualJSON.getString("id");

        //Read challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDActual)
                .then()
                .assertThat().statusCode(200);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }

    @Test
    public void findChallengeMissingID_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        //Read challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/")
                .then()
                .assertThat().statusCode(200);

        JSONObject challengeDTOActualJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDActual = challengeDTOActualJSON.getString("id");

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }

    @Test
    public void findChallengeByNonNumericID_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        //Read challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/" + "abc")
                .then()
                .assertThat().statusCode(400);

        JSONObject challengeDTOActualJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDActual = challengeDTOActualJSON.getString("id");

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }

    @Test
    public void findChallengeByIDLinkedToMissingChallenge_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        //Read challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/" + "-1")
                .then()
                .assertThat().statusCode(404);

        JSONObject challengeDTOActualJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDActual = challengeDTOActualJSON.getString("id");

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }

    @Test
    public void findChallengeByIDMissingContext_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        JSONObject challengeDTOActualJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDActual = challengeDTOActualJSON.getString("id");

        CONTEXT_RequestsUtil.deleteContext(token, contextID);

        //Read challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDActual)
                .then()
                .assertThat().statusCode(404);

        USER_RequestsUtil.deleteUser(token);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }

    @Test
    public void findChallengeByIDMissingToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        JSONObject challengeDTOActualJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDActual = challengeDTOActualJSON.getString("id");

        //Read challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDActual)
                .then()
                .assertThat().statusCode(400);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }

    @Test
    public void findChallengeByMalformedOrExpiredToken_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        JSONObject challengeDTOActualJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDActual = challengeDTOActualJSON.getString("id");

        //Read challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + invalidToken,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDActual)
                .then()
                .assertThat().statusCode(500);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }


    @Test
    public void findChallengeByQuery_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        //Read challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"challenges/" + "?word=&page=0&size=20")
                .then()
                .assertThat().statusCode(200);

        JSONObject challengeDTOActualJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDActual = challengeDTOActualJSON.getString("id");

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }

    @Test
    public void findChallengeQueryMissingToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        //Read challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"challenges/" + "?word=&page=0&size=20")
                .then()
                .assertThat().statusCode(400);

        JSONObject challengeDTOActualJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDActual = challengeDTOActualJSON.getString("id");

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }

    @Test
    public void findChallengeQueryByTokenLinkedToMissingCreator_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        USER_RequestsUtil.deleteUser(token);

        //Read challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"challenges/" + "?word=&page=0&size=20")
                .then()
                .assertThat().statusCode(404);

        JSONObject challengeDTOActualJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDActual = challengeDTOActualJSON.getString("id");

        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }

    @Test
    public void findChallengeQueryByMalformedOrExpiredToken_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + invalidToken,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"challenges/" + "?word=&page=0&size=20")
                .then()
                .assertThat().statusCode(500).log().all();

        JSONObject challengeDTOActualJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDActual = challengeDTOActualJSON.getString("id");

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }


    @Test
    public void updateChallengeByCreatorTokenBodyChallengeID_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponseExpected = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        JSONObject challengeDTOJSONExpected = new JSONObject(challengeDTOResponseExpected.getBody().prettyPrint());

        String challengeIDExpected = challengeDTOJSONExpected.getString("id");

        //Update challenge
        Response challengeDTOResponseActual = given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_PUT_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(200)
                .extract().response();

        JSONObject challengeDTOJSONActual = new JSONObject(challengeDTOResponseActual.getBody().prettyPrint());

        String challengeIDActual = challengeDTOJSONActual.getString("id");
        String challengeImageURLActual = challengeDTOJSONActual.getString("imageUrl");
        String challengeWordActual = challengeDTOJSONActual.getString("word");
        String challengeSoundURLActual = challengeDTOJSONActual.getString("soundUrl");
        String challengeVideoURLActual = challengeDTOJSONActual.getString("videoUrl");

        ChallengeDTO challengeDTOActual = ChallengeBuilder.anChallenge()
                .withId(Long.valueOf(challengeIDActual))
                .withWord(challengeWordActual)
                .withImageUrl(challengeImageURLActual)
                .withSoundUrl(challengeSoundURLActual)
                .withVideoUrl(challengeVideoURLActual).buildChallengeDTO();

        ObjectMapper mapper =  new ObjectMapper();
        mapper.writeValue(new File("src/test/resources/CHALLENGE_ActualContextDTOBody[spawned].json"), challengeDTOActual);
        JSONObject challengeJSONExpected = new JSONObject(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(challengeDTOJSONActual.getString("id"));
        Assertions.assertNotEquals(challengeJSONExpected.getString("word"), challengeDTOJSONActual.getString("word"));
        Assertions.assertEquals(challengeJSONExpected.getString("imageUrl"), challengeDTOJSONActual.getString("imageUrl"));
        Assertions.assertEquals(challengeJSONExpected.getString("soundUrl"), challengeDTOJSONActual.getString("soundUrl"));
        Assertions.assertEquals(challengeJSONExpected.getString("videoUrl"), challengeDTOJSONActual.getString("videoUrl"));

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }

    @Test
    public void updateChallengeByWordAlreadyExists_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponseExpected = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        JSONObject challengeDTOJSONExpected = new JSONObject(challengeDTOResponseExpected.getBody().prettyPrint());

        String challengeIDExpected = challengeDTOJSONExpected.getString("id");

        //Update challenge
        Response challengeDTOResponseActual = given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(200)
                .extract().response();

        JSONObject challengeDTOJSONActual = new JSONObject(challengeDTOResponseActual.getBody().prettyPrint());

        String challengeIDActual = challengeDTOJSONActual.getString("id");
        String challengeImageURLActual = challengeDTOJSONActual.getString("imageUrl");
        String challengeWordActual = challengeDTOJSONActual.getString("word");
        String challengeSoundURLActual = challengeDTOJSONActual.getString("soundUrl");
        String challengeVideoURLActual = challengeDTOJSONActual.getString("videoUrl");

        ChallengeDTO challengeDTOActual = ChallengeBuilder.anChallenge()
                .withId(Long.valueOf(challengeIDActual))
                .withWord(challengeWordActual)
                .withImageUrl(challengeImageURLActual)
                .withSoundUrl(challengeSoundURLActual)
                .withVideoUrl(challengeVideoURLActual).buildChallengeDTO();

        ObjectMapper mapper =  new ObjectMapper();
        mapper.writeValue(new File("src/test/resources/CHALLENGE_ActualContextDTOBody[spawned].json"), challengeDTOActual);
        JSONObject challengeJSONExpected = new JSONObject(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(challengeDTOJSONActual.getString("id"));
        Assertions.assertEquals(challengeJSONExpected.getString("word"), challengeDTOJSONActual.getString("word"));
        Assertions.assertEquals(challengeJSONExpected.getString("imageUrl"), challengeDTOJSONActual.getString("imageUrl"));
        Assertions.assertEquals(challengeJSONExpected.getString("soundUrl"), challengeDTOJSONActual.getString("soundUrl"));
        Assertions.assertEquals(challengeJSONExpected.getString("videoUrl"), challengeDTOJSONActual.getString("videoUrl"));

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDActual);

    }

    @Test
    public void updateChallengeMissingBody_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponseExpected = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        JSONObject challengeDTOJSONExpected = new JSONObject(challengeDTOResponseExpected.getBody().prettyPrint());

        String challengeIDExpected = challengeDTOJSONExpected.getString("id");

        //Update challenge
        given().headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(400);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);

    }

    @Test
    public void updateChallengeMissingID_ShouldReturn405Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponseExpected = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        JSONObject challengeDTOJSONExpected = new JSONObject(challengeDTOResponseExpected.getBody().prettyPrint());

        //Update challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/")
                .then()
                .assertThat().statusCode(405);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);

    }

    @Test
    public void updateChallengeByNonNumericID_ShouldReturn405Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        JSONObject challengeDTOJSONExpected = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOJSONExpected.getString("id");

        //Update challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" +"abc")
                .then()
                .assertThat().statusCode(400);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDExpected);

    }

    @Test
    public void updateChallengeMissingContext_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponseExpected = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        JSONObject challengeDTOJSONExpected = new JSONObject(challengeDTOResponseExpected.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOJSONExpected.getString("id");

        CONTEXT_RequestsUtil.deleteContext(token, contextID);

        //Update challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(404);

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void updateChallengeByIDLinkedToMissingChallenge_ShouldReturn404() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponseExpected = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        JSONObject challengeDTOJSONExpected = new JSONObject(challengeDTOResponseExpected.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOJSONExpected.getString("id");

        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDExpected);

        //Update challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(404);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);

    }

    @Test
    public void updateChallengeByTokenLinkedToMissingCreator_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponseExpected = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        JSONObject challengeDTOJSONExpected = new JSONObject(challengeDTOResponseExpected.getBody().prettyPrint());

        String challengeIDExpected = challengeDTOJSONExpected.getString("id");

        CONTEXT_RequestsUtil.deleteContext(token, contextID);

        //Update challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(404);

        USER_RequestsUtil.deleteUser(token);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDExpected);

    }

    @Test
    public void updateChallengeMissingToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponseExpected = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        JSONObject challengeDTOJSONExpected = new JSONObject(challengeDTOResponseExpected.getBody().prettyPrint());

        String challengeIDExpected = challengeDTOJSONExpected.getString("id");

        //Update challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(400);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDExpected);

    }

    @Test
    public void updateChallengeByMalformedOrExpiredToken_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponseExpected = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextID);

        JSONObject challengeDTOJSONExpected = new JSONObject(challengeDTOResponseExpected.getBody().prettyPrint());

        String challengeIDExpected = challengeDTOJSONExpected.getString("id");

        //Update challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + invalidToken,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(500);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDExpected);

    }


    @Test
    public void deleteChallengeByCreatorTokenID_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json",contextID);

        JSONObject challengeDTOJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeID =challengeDTOJSON.getString("id");

        //Delete challenge
        given()
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/challenges/" + challengeID)
                .then()
                .assertThat().statusCode(200);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);

    }

    @Test
    public void deleteChallengeMissingID_ShouldReturn405Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json",contextID);

        JSONObject challengeDTOJSONExpected = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOJSONExpected.getString("id");

        //Delete challenge
        given()
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/challenges/")
                .then()
                .assertThat().statusCode(405);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDExpected);

    }

    @Test
    public void deleteChallengeByIDContextMissing_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json",contextID);

        JSONObject challengeDTOJSONExpected = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOJSONExpected.getString("id");

        CONTEXT_RequestsUtil.deleteContext(token, contextID);

        //Delete challenge
        given()
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(404);

        USER_RequestsUtil.deleteUser(token);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDExpected);

    }

    @Test
    public void deleteChallengeByNonNumericID_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json",contextID);

        JSONObject challengeDTOJSONExpected = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOJSONExpected.getString("id");

        //Delete challenge
        given()
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/challenges/" + "abc")
                .then()
                .assertThat().statusCode(400);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDExpected);

    }

    @Test
    public void deleteChallengeMissingToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json",contextID);

        JSONObject challengeDTOJSONExpected = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOJSONExpected.getString("id");

        //Delete challenge
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(400);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDExpected);

    }

    @Test
    public void deleteChallengeByTokenLinkedToMissingCreator_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json",contextID);

        JSONObject challengeDTOJSONExpected = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOJSONExpected.getString("id");

        USER_RequestsUtil.deleteUser(token);

        //Delete challenge
        given()
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(404);

        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDExpected);

    }

    @Test
    public void deleteChallengeByCreatorTokenMalformedOrExpired_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json",contextID);

        JSONObject challengeDTOJSONExpected = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOJSONExpected.getString("id");

        //Delete challenge
        given()
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + invalidToken,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(500);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, challengeIDExpected);


    }

}

//ISSUES
// i1: GET por QUERY não está buscando nada, sugestão: buscar por word
// i2: PUT desafio com word já existente(200 OK) não altera nada e retorna o mesmo desafio(body) do input. Sugestão: Deveria ser tratado

//TODO's
// ToDo: FAZER A VERIFICAÇÃO DE QUANTIDADE DE CARACTERES DE CONTEXTS
// ToDo: FAZER UMA TABELA DAS FUNÇÕES E O QUE FORAM TESTADAS NELAS em x e y(BATALHA NAVAL)
// ToDo: Drop tables between tests
