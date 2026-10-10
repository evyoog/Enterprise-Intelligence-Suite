package com.vyoog.eisplatform.modules.toolsync.service.e2e;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** The real Macro Planner as a separate process (the jar built from the Macro repository), against a scratch PostgreSQL. */
public class MacroProcess implements AutoCloseable {

    private final File jar;
    private final File log;
    private final int port;
    private final List<String> args = new ArrayList<>();
    private Process process;

    public MacroProcess(String jarPath, int port, File log) {
        this.jar = new File(jarPath);
        this.port = port;
        this.log = log;
    }

    public MacroProcess arg(String keyValue) {
        args.add("--" + keyValue);
        return this;
    }

    public int port() {
        return port;
    }

    public void start() throws Exception {
        List<String> cmd = new ArrayList<>(List.of(System.getProperty("java.home") + "/bin/java", "-Xmx512m", "-jar", jar.getAbsolutePath(), "--server.port=" + port));
        cmd.addAll(args);
        ProcessBuilder pb = new ProcessBuilder(cmd).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.appendTo(log));
        pb.environment().remove("JAVA_TOOL_OPTIONS");
        process = pb.start();
        long deadline = System.currentTimeMillis() + 120_000;
        while (System.currentTimeMillis() < deadline) {
            if (!process.isAlive()) {
                throw new IllegalStateException("The Macro Planner stopped while starting; see " + log);
            }
            try {
                HttpURLConnection c = (HttpURLConnection) URI.create("http://localhost:" + port + "/api/actuator/health").toURL().openConnection();
                c.setConnectTimeout(1000);
                c.setReadTimeout(2000);
                if (c.getResponseCode() == 200) {
                    return;
                }
            } catch (Exception ignored) {
                // not up yet
            }
            Thread.sleep(500);
        }
        throw new IllegalStateException("The Macro Planner did not start in time; see " + log);
    }

    public void stop() throws Exception {
        if (process != null && process.isAlive()) {
            process.destroy();
            if (!process.waitFor(20, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        }
    }

    public boolean running() {
        return process != null && process.isAlive();
    }

    @Override
    public void close() throws Exception {
        stop();
    }
}
