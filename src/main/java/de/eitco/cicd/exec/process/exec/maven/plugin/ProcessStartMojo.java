package de.eitco.cicd.exec.process.exec.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mojo(name = "start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class ProcessStartMojo extends AbstractProcessMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping " + name);
            return;
        }
        if (getLog().isDebugEnabled()) {
            for (String arg : arguments) {
                getLog().debug("arg: " + arg);
            }
            if (environment != null) {
                for (Map.Entry<String, String> entry : environment.entrySet()) {
                    getLog().debug("env: " + entry.getKey() + "=" + entry.getValue());
                }
            }
        }
        try {
            startProcess();
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        if (waitForInterrupt) {
            sleepUntilInterrupted();
        }
    }

    private void startProcess() throws IOException {
        final ExecProcess exec = new ExecProcess(name, getLog());
        if (null != processLogFile) {
            File plf = new File(processLogFile);
            ensureDirectory(plf.getParentFile());
            exec.setProcessLogFile(plf);
        }
        getLog().info("Starting process: " + exec.getName());

        File workingDirectory = processWorkingDirectory();

        List<String> finalArguments = new ArrayList<>();

        if (executable != null && !executable.isEmpty()) {

            File execFile = new File(executable);

            if (!execFile.exists() && !execFile.isAbsolute()) {
                execFile = new File(workingDirectory, executable);
            }

            if (!execFile.exists()) {
                finalArguments.add(executable);
            } else {
                finalArguments.add(execFile.getPath());
            }
        }

        finalArguments.addAll(arguments);

        exec.execute(workingDirectory, environment, finalArguments);
        CrossMojoState.get(getPluginContext()).add(exec);
        new ProcessHealthCondition(getLog(), healthCheckUrl, waitAfterLaunch, healthCheckValidateSsl, healthCheckIgnoreFailures)
            .waitSecondsUntilHealthy();
        getLog().info("Started process: " + exec.getName());
    }

    private File processWorkingDirectory() throws IOException {
        String buildDir = project.getBuild().getDirectory();
        if (workingDir == null) {
            return ensureDirectory(new File(buildDir));
        }

        // try to check if buildDir is absolute
        // https://github.com/bazaarvoice/maven-process-plugin/issues/11
        File pwd = new File(workingDir);
        if (!pwd.isAbsolute()) {
            pwd = new File(buildDir, workingDir);
        }
        return ensureDirectory(pwd);
    }
}
