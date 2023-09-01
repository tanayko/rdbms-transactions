package com.lseg.acadia.skills.rdbmstx.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.containers.Network;
import org.testcontainers.images.ImagePullPolicy;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDatabaseTest {

    public static String POSTGRES_DOCKER_IMAGE = "nexus3.acadiasoft.net:4445/postgres:15.3";

    private static final Logger log = LoggerFactory.getLogger(AbstractDatabaseTest.class);

    //    public static Integer STABLE_DB_PORT = 5555;
    public static Integer STABLE_DB_PORT = null;

    public static String STABLE_SOURCE_USER = "postgres";
    public static String STABLE_SOURCE_PASSWORD = "";
    public static String STABLE_DEST_USER = "postgres";
    public static String STABLE_DEST_PASSWORD = "";

    public static void setPropertiesNameNotRelevant(DynamicPropertyRegistry registry, GenericContainer<?> container) {
        registry.add("spring.datasource.url", () -> {
            final Integer actualPort = container.getMappedPort(5432);
            return String.format("jdbc:postgresql://localhost:%d/rdbmstx", actualPort);
        });
        registry.add("spring.flyway.url", () -> {
            final Integer actualPort = container.getMappedPort(5432);
            return String.format("jdbc:postgresql://localhost:%d/rdbmstx", actualPort);
        });
    }

    protected static GenericContainer<?> postgresContainer(ImagePullPolicy pullPolicy, Integer stablePort) {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(POSTGRES_DOCKER_IMAGE));
        container.withNetwork(Network.SHARED);
        container.withNetworkAliases("postgres");
        container.withImagePullPolicy(pullPolicy);
//    container.addEnv("POSTGRES_HOST_AUTH_METHOD", "trust");
        container.addEnv("POSTGRES_PASSWORD", "password");
        container.addEnv("POSTGRES_DB", "rdbmstx");
        container.addEnv("POSTGRES_USER", "rdbmstx");

        if (stablePort != null) {
            final List<String> portBindings = new ArrayList<>();
            portBindings.add(String.format("%d:%d/%s", stablePort, 5432, InternetProtocol.TCP.toDockerNotation()));
            container.setPortBindings(portBindings);
        }

        container.withExposedPorts(5432);

        return container;
    }
}
