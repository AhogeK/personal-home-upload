package com.ahogek.config;

import com.ahogek.util.SFTPUtil;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * @author AhogeK
 * @since 2024-10-26 02:14:08
 */
@Startup
public class SFTPInitializer {

    private static final Logger LOGGER = Logger.getLogger(SFTPInitializer.class);

    @ConfigProperty(name = "sftp.host")
    String host;

    @ConfigProperty(name = "sftp.username")
    String username;

    @ConfigProperty(name = "sftp.password")
    String password;

    @ConfigProperty(name = "sftp.port", defaultValue = "22")
    int port;

    @ConfigProperty(name = "sftp.root-dir")
    String rootDir;

    private final SFTPUtil sftpUtil;

    @Inject
    public SFTPInitializer(SFTPUtil sftpUtil) {
        this.sftpUtil = sftpUtil;
    }

    @PostConstruct
    void init() {
        LOGGER.info("Initializing SFTP connection...");
        sftpUtil.initialize(host, username, password, port, rootDir);
        LOGGER.info("SFTP connection initialized");
    }
}
