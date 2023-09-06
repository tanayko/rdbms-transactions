package com.lseg.acadia.skills.rdbmstx.demo;

import com.lseg.acadia.skills.rdbmstx.mappers.OrdersMapper;
import com.lseg.acadia.skills.rdbmstx.mappers.PeopleMapper;
import com.lseg.acadia.skills.rdbmstx.models.Orders;
import com.lseg.acadia.skills.rdbmstx.models.People;
import org.flywaydb.core.Flyway;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;

@Testcontainers
@SpringBootTest
class DemoApplicationTests extends AbstractDatabaseTest {
	private static final Logger logger
			= LoggerFactory.getLogger(DemoApplicationTests.class);
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

	@Autowired
	private OrdersMapper ordersMapper;

	@Autowired
	private PlatformTransactionManager platformTransactionManager;
	private TransactionTemplate transactionTemplate;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	public void setup() {
		flyway.clean();
		flyway.migrate();
		transactionTemplate = new TransactionTemplate(platformTransactionManager);
	}

	@Test
	public void testPeopleInsert() {
		People people = new People("John", 12);
		peopleMapper.insert(people);

		People peopleInDb = peopleMapper.selectById(1);
		assertEquals(peopleInDb.id, people.id);
		assertEquals(peopleInDb.name, people.name);
		assertEquals(peopleInDb.age, people.age);
	}

	@Test
	public void testOrdersInsert() {
		People people = new People("John", 12);
		peopleMapper.insert(people);

		Orders orders = new Orders("Car", BigDecimal.TEN, 1);
		ordersMapper.insert(orders);

		Orders ordersInDb = ordersMapper.selectById(1);
		assertEquals(ordersInDb.id, orders.id);
		assertEquals(ordersInDb.product, orders.product);
//		assertThat(Matchers.comparesEqualTo(ordersInDb.cost), orders.cost);
		assertEquals(ordersInDb.userId, orders.userId);
	}

	@Test
	public void testSingleTransactionSuccess() {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(@NotNull TransactionStatus status) {
				try {
					jdbcTemplate.update("insert into people (name, age) values (?, ?)", "person1", 50);
					jdbcTemplate.update("insert into people (name, age) values (?, ?)", "person2", 20);
				} catch (Exception e) {
					logger.info("error");
				}
			}
		});

		assertEquals(peopleMapper.selectAll().size(), 2);

		People person1 = peopleMapper.selectById(1);
		assertEquals(person1.name, "person1");
		assertEquals(person1.age, 50);

		People person2 = peopleMapper.selectById(2);
		assertEquals(person2.name, "person2");
		assertEquals(person2.age, 20);
	}

	@Test
	public void testSingleTransactionFailure() {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(@NotNull TransactionStatus status) {
				try {
					jdbcTemplate.update("insert into people (name, age) values (?, ?)", "person1", 50);
					jdbcTemplate.update("insert into people (name, age) values (?, ?)", "person2", 20);
					jdbcTemplate.update("insert into people (name, age) values (?, ?)", "person1", 80);
				} catch (Exception e) {
					logger.info("error");
				}
			}
		});

		assertEquals(peopleMapper.selectAll().size(), 0);
	}

	@Test
	public void testTransaction1() {
		DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
		def1.setName("T1");
		def1.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		TransactionStatus status1 = platformTransactionManager.getTransaction(def1);

		assertEquals(peopleMapper.selectAll().size(), 0);

		try {
			jdbcTemplate.update("insert into people (name, age) values (?, ?)", "person1", 50);
		} catch (Exception e) {
			logger.error("T1 rolled back");
			platformTransactionManager.rollback(status1);
		}

		DefaultTransactionDefinition def2 = new DefaultTransactionDefinition();
		def2.setName("T2");
		def2.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		TransactionStatus status2 = platformTransactionManager.getTransaction(def2);

		assertEquals(peopleMapper.selectAll().size(), 0);

		try {
			Long id = jdbcTemplate.queryForObject("select id from people where name = 'person1'",
					Long.class);
		} catch (EmptyResultDataAccessException e) {
			logger.info("No info found");
		} catch (Exception e) {
			logger.error("T2 rolled back");
			platformTransactionManager.rollback(status2);
		}

		try {
			platformTransactionManager.commit(status1);
		} catch (Exception e) {
			logger.info("Error committing T1");
		}

		assertEquals(peopleMapper.selectAll().size(), 1);

		try {
			People people = jdbcTemplate.queryForObject("select name from people where name = ?",
					(resultSet, rowNum) -> {
						People newPeople = new People();
						newPeople.name = resultSet.getString("name");
						newPeople.age = resultSet.getInt("age");
						return newPeople;
					},
					"person1");
			assertNotNull(people);
		} catch (Exception e1) {
			logger.info("Rolling back T2");
			platformTransactionManager.rollback(status2);
		}

		try {
			platformTransactionManager.commit(status2);
		} catch (Exception e) {
			logger.info("Error committing T2");
		}
	}

	@Test
	public void testTransaction2() {

	}
}
