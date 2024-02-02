package com.blumeglobal;

import com.blumeglobal.core.mongo.test.mockdata.MockDataMongoService;
import com.blumeglobal.core.test.config.BlumeSpringRunner;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

@RunWith(BlumeSpringRunner.class)
@SpringBootTest(classes = {Blumecoreapi.class})
@ContextConfiguration(
        initializers = {ConfigDataApplicationContextInitializer.class}
)
@Configuration
@ActiveProfiles("test")
@DirtiesContext
//@PropertySource(value = "classpath:test-mysql-datasource.yaml", factory = YamlPropertySourceFactory.class)
public abstract class AbstractIntegrationTest {


    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("platformMongoDataSource")
    public MongoTemplate mongoTemplate;

    @Configuration
    @EnableCaching
    static class Config {

        // Simulating your caching configuration
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("sample");
        }
    }

    @Before
    public void cleanSchema() {
        mongoTemplate.getDb().drop();
    }
}
