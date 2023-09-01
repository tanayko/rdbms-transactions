package com.lseg.acadia.skills.rdbmstx.demo;

import com.lseg.acadia.skills.rdbmstx.mappers.PeopleMapper;
import com.lseg.acadia.skills.rdbmstx.models.People;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
class DemoApplicationTests extends AbstractDatabaseTest {
	@Container
	protected static GenericContainer<?> postgresContainer = postgresContainer(PullPolicy.defaultPolicy(), STABLE_DB_PORT);

	@DynamicPropertySource
	public static void setPropertiesNameNotRelevant(DynamicPropertyRegistry registry) {
		setPropertiesNameNotRelevant(registry, postgresContainer);
	}

	@Autowired
	private Flyway flyway;

	@Autowired
	private PeopleMapper peopleMapper;

	@BeforeEach
	public void setup() {
		flyway.clean();
		flyway.migrate();
	}

	@Test
	public void testInsert() {
		People people = new People("John", 12);
		peopleMapper.insert(people);

		People peopleInDb = peopleMapper.selectById(1);
		assertEquals(peopleInDb.id, people.id);
		assertEquals(peopleInDb.name, people.name);
		assertEquals(peopleInDb.age, people.age);
	}

}
