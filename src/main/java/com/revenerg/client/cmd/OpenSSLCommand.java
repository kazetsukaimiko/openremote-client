package com.revenerg.client.cmd;

import lombok.extern.jbosslog.JBossLog;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

@JBossLog
public abstract class OpenSSLCommand<I, R> implements Function<I, R> {

    public static Path tempFile(String subdir, String name) {
        try {
            Path result = Files.createDirectories(Paths.get("/tmp", "openremote-client", subdir));
            return result.resolve(name);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void executeOpenSSL(String... command) {
        try {
            Process p = new ProcessBuilder()
                    .command(command)
                    .start();
            log.infof("Invoked OpenSSL:\n%s", String.join(" ", command));
            int result = p.waitFor();
            if (result != 0) {
                try (BufferedOutputStream out = new BufferedOutputStream(System.out)) {
                    p.getErrorStream().transferTo(out);
                    out.flush();
                }
            } else {
                try (BufferedOutputStream out = new BufferedOutputStream(System.out)) {
                    p.getInputStream().transferTo(out);
                    out.flush();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static boolean accessibilityCheck(String name, Path readable) {
        if (!Files.exists(readable)) {
            log.errorf("%s '%s' doesn't exist".formatted(name, readable));
            return false;
        }
        if (!Files.exists(readable)) {
            log.errorf("%s '%s' doesn't exist".formatted(name, readable));
            return false;
        }
        if (!Files.isReadable(readable)) {
            log.errorf("%s '%s' tlsCertnnot be read".formatted(name, readable));
            return false;
        }
        return true;
    }
}
