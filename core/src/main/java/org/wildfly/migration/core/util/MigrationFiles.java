package org.wildfly.migration.core.util;

import org.wildfly.migration.core.logger.ServerMigrationLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by emmartins on 08/12/15.
 */
public class MigrationFiles {

    private final Map<Path, Path> copiedFiles;

    public MigrationFiles() {
        this.copiedFiles = new HashMap<>();
    }

    public synchronized void copy(Path source, Path target) throws IOException {
        // check if already copied
        final Path existentCopySource = copiedFiles.get(target);
        if (existentCopySource != null) {
            if (!existentCopySource.equals(source)) {
                // FIXME add to logger
                throw new IllegalStateException("Target file previously copied from different source!");
            } else {
                // no need to re-copy same file
                ServerMigrationLogger.ROOT_LOGGER.infof("Skipping previously copied file %s", source);
                return;
            }
        }
        // check source file exists
        if (!Files.exists(source)) {
            throw new IllegalStateException("Source file does not exists!");
        }
        // if target file exists make a backup copy
        if (Files.exists(target)) {
            ServerMigrationLogger.ROOT_LOGGER.infof("File %s exists on target, renaming to %s.beforeMigration", target, target.getFileName().toString());
            Files.copy(target, target.getParent().resolve(target.getFileName().toString()+".beforeMigration"), StandardCopyOption.REPLACE_EXISTING);
        }
        // copy file
        ServerMigrationLogger.ROOT_LOGGER.tracef("Copying file %s", target);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        // keep track of the file copy to prevent more copies for same target
        copiedFiles.put(target, source);
        ServerMigrationLogger.ROOT_LOGGER.infof("File %s copied to %s", source, target);
    }
}
