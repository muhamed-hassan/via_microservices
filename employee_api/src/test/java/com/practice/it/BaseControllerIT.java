package com.practice.it;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BaseControllerIT {

    private static PostgreSQLContainer postgreSQLContainer;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void initTestDB() {
        postgreSQLContainer = new PostgreSQLContainer("postgres:12")
                                    .withDatabaseName("integration-tests-db")
                                    .withUsername("username")
                                    .withPassword("password");
        postgreSQLContainer.start();
        System.setProperty("DB_URL", postgreSQLContainer.getJdbcUrl());
        System.setProperty("DB_USER", postgreSQLContainer.getUsername());
        System.setProperty("DB_PASSWORD", postgreSQLContainer.getPassword());
    }

    @AfterAll
    static void dispose() {
        postgreSQLContainer.stop();
    }

    protected MockMvc getMockMvc() {
        return mockMvc;
    }

    protected void expect(ResultActions resultActions, ResultMatcher... resultMatchers)
            throws Exception {
        for (var resultMatcher : resultMatchers) {
            resultActions.andExpect(resultMatcher);
        }
    }

}
