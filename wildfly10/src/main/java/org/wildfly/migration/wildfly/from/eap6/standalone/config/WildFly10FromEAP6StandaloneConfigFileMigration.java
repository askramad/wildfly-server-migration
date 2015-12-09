package org.wildfly.migration.wildfly.from.eap6.standalone.config;

import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.logger.ServerMigrationLogger;
import org.wildfly.migration.eap.EAP6StandaloneConfig;
import org.wildfly.migration.wildfly.WildFly10Server;
import org.wildfly.migration.wildfly.standalone.EmbeddedWildFly10StandaloneServer;
import org.wildfly.migration.wildfly.standalone.WildFly10StandaloneServer;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by emmartins on 08/12/15.
 */
public class WildFly10FromEAP6StandaloneConfigFileMigration {

    private final WildFly10FromEAP6StandaloneConfigFileSubsystemsMigration subsystemsMigration;
    private  final WildFly10FromEAP6StandaloneConfigFileSecurityRealmsMigration securityRealmsMigration;

    public WildFly10FromEAP6StandaloneConfigFileMigration(WildFly10FromEAP6StandaloneConfigFileSubsystemsMigration subsystemsMigration, WildFly10FromEAP6StandaloneConfigFileSecurityRealmsMigration securityRealmsMigration) {
        this.subsystemsMigration = subsystemsMigration;
        this.securityRealmsMigration = securityRealmsMigration;
    }

    public void run(EAP6StandaloneConfig source, WildFly10Server target, ServerMigrationContext context) throws IOException {
        context.getConsoleWrapper().printf("%n%n");
        ServerMigrationLogger.ROOT_LOGGER.infof("Migrating standalone server configuration %s", source.getPath());
        copyFileToTargetServer(source.getPath(), target, context);
        final WildFly10StandaloneServer standaloneServer = startEmbeddedServer(source.getPath(), target);
        subsystemsMigration.run(source, standaloneServer, context);
        securityRealmsMigration.run(source, standaloneServer, context);
        // shutdown server
        standaloneServer.stop();
        ServerMigrationLogger.ROOT_LOGGER.info("Standalone server configuration file migration done.");
    }

    protected void copyFileToTargetServer(Path source, WildFly10Server targetServer, ServerMigrationContext context) throws IOException {
        // check if server file exists
        final Path target = targetServer.getStandaloneConfigurationDir().resolve(source.getFileName());
        ServerMigrationLogger.ROOT_LOGGER.debugf("Source server configuration file is %s", source);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Target server configuration file is %s", target);
        context.getMigrationFiles().copy(source, target);
        ServerMigrationLogger.ROOT_LOGGER.infof("Server configuration file %s copied to %s", source, target);
    }

    protected WildFly10StandaloneServer startEmbeddedServer(Path source, WildFly10Server target) throws IOException {
        final String config = source.getFileName().toString();
        ServerMigrationLogger.ROOT_LOGGER.infof("Starting server server configuration %s", config);
        final WildFly10StandaloneServer wildFly10StandaloneServer = new EmbeddedWildFly10StandaloneServer(config, target);
        wildFly10StandaloneServer.start();
        ServerMigrationLogger.ROOT_LOGGER.infof("Started server server configuration %s", config);
        return wildFly10StandaloneServer;
    }


}
