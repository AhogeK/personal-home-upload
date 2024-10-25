package com.ahogek.util;

import com.jcraft.jsch.*;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * SFTP op util
 *
 * @author AhogeK
 * @since 2024-10-23 21:12:52
 */
@Singleton
public class SFTPUtil {
    // Using Record to store configuration information
    private record SSHConfig(String host, String username, String password, int port, String rootDir) {
        // Constructor validation
        public SSHConfig {
            if (host == null || host.isBlank())
                throw new IllegalArgumentException("Host cannot be null or blank");
            if (username == null || username.isBlank())
                throw new IllegalArgumentException("Username cannot be null or blank");
            if (password == null || password.isBlank())
                throw new IllegalArgumentException("Password cannot be null or blank");
            if (port <= 0 || port > 65535)
                throw new IllegalArgumentException("Invalid port number");

            if (!rootDir.startsWith("/"))
                rootDir = "/" + rootDir;
            if (rootDir.endsWith("/"))
                rootDir = rootDir.substring(0, rootDir.length() - 1);
        }

        String getFullPath(String relativePath) {
            if (relativePath == null || relativePath.isBlank())
                return rootDir;

            Path fullPath = Paths.get(rootDir, relativePath).normalize();
            return fullPath.toString().replace("\\", "/");
        }
    }

    private SSHConfig config;
    private Session session;
    private ChannelSftp channelSftp;
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private static final int CHANNEL_TIMEOUT = 15000; // 15s
    private static final int RETRY_TIMES = 3;

    // Private constructor
    private SFTPUtil() {
    }

    // init config
    public void initialize(String host, String username, String password, int port, String rootDir) {
        if (initialized.get())
            throw new IllegalStateException("SFTPUtil has already been initialized");
        this.config = new SSHConfig(host, username, password, port, rootDir);

        try {
            // Make sure the root directory exists
            connectWithRetry();
            ensureRootDirectoryExists();
            initialized.set(true);
        } catch (Exception e) {
            disconnect();
            throw new IllegalStateException("Failed to initialize SFTP connection", e);
        }
    }

    private void ensureConnected() {
        lock.lock();
        try {
            if (!isConnected()) {
                connectWithRetry();
            }
        } finally {
            lock.unlock();
        }
    }

    private boolean isConnected() {
        return session != null && session.isConnected()
                && channelSftp != null && channelSftp.isConnected();
    }

    private void connectWithRetry() {
        Exception lastException = null;
        for (int i = 0; i < RETRY_TIMES; i++) {
            try {
                connect();
                return;
            } catch (Exception e) {
                lastException = e;
                disconnect();
            }
        }
        throw new IllegalStateException("Failed to connect after " + RETRY_TIMES + " attempts", lastException);
    }

    private void connect() throws JSchException {
        JSch jsch = new JSch();
        session = jsch.getSession(config.username(), config.host(), config.port());
        session.setPassword(config.password());

        Properties properties = new Properties();
        properties.put("StrictHostKeyChecking", "no");
        session.setConfig(properties);

        session.connect(CHANNEL_TIMEOUT);
        channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect(CHANNEL_TIMEOUT);
    }

    @PreDestroy
    public void disconnect() {
        if (channelSftp != null) {
            channelSftp.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }

    /**
     * Check if the root directory exists.
     * If the root directory does not exist, throw an exception instead of creating it
     *
     * @throws IllegalStateException if the root directory does not exist
     */
    private void ensureRootDirectoryExists() {
        try {
            channelSftp.lstat(config.rootDir());
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
                throw new IllegalStateException("Root directory does not exist: " + config.rootDir());
            throw new IllegalStateException("Failed to check root directory: " + e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface SFTPOperation<T> {
        T execute(ChannelSftp channelSftp) throws SftpException, JSchException;
    }

    private <T> T executeOperation(SFTPOperation<T> operation) {
        if (!initialized.get())
            throw new IllegalStateException("SFTPUtil is not initialized");

        lock.lock();
        try {
            ensureConnected();
            return operation.execute(channelSftp);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to execute SFTP operation", e);
        } finally {
            lock.unlock();
        }
    }

    // Using sealed interface to define operation results
    public sealed interface Result {
        record Success(String message) implements Result {
        }

        record Fail(String message) implements Result {
        }
    }

    public boolean exists(String relativePath) {
        return executeOperation(channelSftp -> {
            try {
                String fullPath = config.getFullPath(relativePath);
                channelSftp.lstat(fullPath);
                return true;
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
                    return false;
                throw e;
            }
        });
    }

    public Result createDirectory(String relativePath) {
        return executeOperation(channelSftp -> {
            try {
                String fullPath = config.getFullPath(relativePath);
                channelSftp.mkdir(fullPath);
                return new Result.Success("Directory created successfully: " + fullPath);
            } catch (SftpException e) {
                return new Result.Fail("Failed to create directory: " + e.getMessage());
            }
        });
    }

    public Result deleteDirectory(String relativePath) {
        return executeOperation(channelSftp -> {
            try {
                String fullPath = config.getFullPath(relativePath);
                List<?> files = new ArrayList<>(channelSftp.ls(fullPath));
                if (files.size() > 2)
                    return new Result.Fail("Directory is not empty: " + fullPath);
                channelSftp.rmdir(fullPath);
                return new Result.Success("Directory deleted successfully: " + fullPath);
            } catch (SftpException e) {
                return new Result.Fail("Failed to delete directory: " + e.getMessage());
            }
        });
    }

    public Result uploadFile(InputStream inputStream, String relativePath) {
        return executeOperation(channelSftp -> {
            try {
                String fullPath = config.getFullPath(relativePath);
                channelSftp.put(inputStream, fullPath);
                return new Result.Success("File uploaded Successfully: " + fullPath);
            } catch (Exception e) {
                return new Result.Fail("Failed to upload file: " + e.getMessage());
            }
        });
    }

    public Result downloadFile(String relativePath, OutputStream outputStream) {
        return executeOperation(channelSftp -> {
            try {
                String fullPath = config.getFullPath(relativePath);
                channelSftp.get(fullPath, outputStream);
                return new Result.Success("File downloaded successfully: " + fullPath);
            } catch (Exception e) {
                return new Result.Fail("Failed to download file: " + e.getMessage());
            }
        });
    }

    public SftpATTRS getFileAttributes(String relativePath) {
        return executeOperation(channelSftp -> {
            String fullPath = config.getFullPath(relativePath);
            return channelSftp.lstat(fullPath);
        });
    }

    public Result deleteFile(String relativePath) {
        return executeOperation(channelSftp -> {
            try {
                String fullPath = config.getFullPath(relativePath);
                channelSftp.rm(fullPath);
                return new Result.Success("File deleted successfully: " + fullPath);
            } catch (SftpException e) {
                return new Result.Fail("Failed to delete file: " + e.getMessage());
            }
        });
    }
}
