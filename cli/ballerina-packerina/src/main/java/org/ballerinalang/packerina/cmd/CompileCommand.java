/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.packerina.cmd;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.packerina.TaskExecutor;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.task.CleanTargetDirTask;
import org.ballerinalang.packerina.task.CompileTask;
import org.ballerinalang.packerina.task.CopyModuleJarTask;
import org.ballerinalang.packerina.task.CopyNativeLibTask;
import org.ballerinalang.packerina.task.CreateBaloTask;
import org.ballerinalang.packerina.task.CreateBirTask;
import org.ballerinalang.packerina.task.CreateDocsTask;
import org.ballerinalang.packerina.task.CreateJarTask;
import org.ballerinalang.packerina.task.CreateLockFileTask;
import org.ballerinalang.packerina.task.CreateTargetDirTask;
import org.ballerinalang.packerina.task.RunTestsTask;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SIDDHI_RUNTIME_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
import static org.ballerinalang.packerina.cmd.Constants.COMPILE_COMMAND;

/**
 * Compile Ballerina modules in to balo.
 *
 * @since 0.992.0
 */
@CommandLine.Command(name = COMPILE_COMMAND, description = "Compile Ballerina modules")
public class CompileCommand implements BLauncherCmd {
    
    private Path userDir;
    private final PrintStream outStream;
    private final PrintStream errStream;
    private boolean exitWhenFinish;

    public CompileCommand() {
        this.userDir = Paths.get(System.getProperty("user.dir"));
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
    }

    public CompileCommand(Path userDir, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.userDir = userDir;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    @CommandLine.Option(names = {"--offline"})
    private boolean offline;

    @CommandLine.Option(names = {"--lockEnabled"})
    private boolean lockEnabled;

    @CommandLine.Option(names = {"--skip-tests"})
    private boolean skipTests;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--native"}, hidden = true,
            description = "compile Ballerina program to a native binary")
    private boolean nativeBinary;

    @CommandLine.Option(names = "--dump-bir", hidden = true)
    private boolean dumpBIR;

    @CommandLine.Option(names = "--dump-llvm-ir", hidden = true)
    private boolean dumpLLVMIR;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--experimental", description = "enable experimental language features")
    private boolean experimentalFlag;

    @CommandLine.Option(names = {"--config"}, description = "path to the configuration file")
    private String configFilePath;

    @CommandLine.Option(names = "--siddhi-runtime", description = "enable siddhi runtime for stream processing")
    private boolean siddhiRuntimeFlag;

    public void execute() {

        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(COMPILE_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }

        if (argList != null && argList.size() > 1) {
            CommandUtil.printError(errStream,
                    "too many arguments.",
                    "ballerina compile [<module-name>]",
                    true);
        }

        // Get source root path.
        Path sourceRootPath = userDir;

        // Compile command only works inside a project
        if (!ProjectDirs.isProject(sourceRootPath)) {
            Path findRoot = ProjectDirs.findProjectRoot(sourceRootPath);
            if (null == findRoot) {
                CommandUtil.printError(errStream,
                        "compile command can be only run inside a Ballerina project",
                        null,
                        false);
                return;
            }
            sourceRootPath = findRoot;
        }

        if (nativeBinary) {
            genNativeBinary(sourceRootPath, argList);
        } else if (argList == null || argList.size() == 0) {
            // to build all modules of a project
            if (!ProjectDirs.isProject(sourceRootPath)) {
                Path findRoot = ProjectDirs.findProjectRoot(sourceRootPath);
                if (null == findRoot) {
                    CommandUtil.printError(errStream,
                            "Please provide a Ballerina file as a " +
                            "input or run compile command inside a project",
                            "ballerina compile [<filename.bal>]",
                            false);
                    return;
                }
                sourceRootPath = findRoot;
            }
    
            CompilerContext context = new CompilerContext();
            CompilerOptions options = CompilerOptions.getInstance(context);
            options.put(PROJECT_DIR, sourceRootPath.toString());
            options.put(OFFLINE, Boolean.toString(offline));
            options.put(COMPILER_PHASE, CompilerPhase.BIR_GEN.toString());
            options.put(LOCK_ENABLED, Boolean.toString(lockEnabled));
            options.put(SKIP_TESTS, Boolean.toString(skipTests));
            options.put(TEST_ENABLED, "true");
            options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(experimentalFlag));
            options.put(SIDDHI_RUNTIME_ENABLED, Boolean.toString(siddhiRuntimeFlag));
    
            BuildContext buildContext = new BuildContext(sourceRootPath);
            buildContext.setOut(outStream);
            buildContext.setOut(errStream);
            buildContext.put(BuildContextField.COMPILER_CONTEXT, context);
    
            TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                    .addTask(new CleanTargetDirTask())
                    .addTask(new CreateTargetDirTask())
                    .addTask(new CompileTask())
                    .addTask(new CreateBaloTask())
                    .addTask(new CreateBirTask())
                    .addTask(new CopyNativeLibTask())
                    .addTask(new CreateJarTask())
                    .addTask(new CopyModuleJarTask())
                    .addTask(new RunTestsTask(), this.skipTests)
                    .addTask(new CreateLockFileTask())
                    .addTask(new CreateDocsTask())
                    .build();
    
            taskExecutor.executeTasks(buildContext);
        } else {
            CompilerContext context = new CompilerContext();
            CompilerOptions options = CompilerOptions.getInstance(context);
            options.put(PROJECT_DIR, sourceRootPath.toString());
            options.put(OFFLINE, Boolean.toString(offline));
            options.put(COMPILER_PHASE, CompilerPhase.BIR_GEN.toString());
            options.put(LOCK_ENABLED, Boolean.toString(lockEnabled));
            options.put(SKIP_TESTS, Boolean.toString(skipTests));
            options.put(TEST_ENABLED, "true");
            options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(experimentalFlag));
            options.put(SIDDHI_RUNTIME_ENABLED, Boolean.toString(siddhiRuntimeFlag));
    
            // remove the hyphen of the module folder if it exists
            String pkgOrSourceFileNameAsString = argList.get(0);
            if (pkgOrSourceFileNameAsString.endsWith("/")) {
                pkgOrSourceFileNameAsString = pkgOrSourceFileNameAsString.substring(0,
                        pkgOrSourceFileNameAsString.length() - 1);
            }
    
            // normalize the source path to remove './' or '.\' characters that can appear before the name
            Path pkgOrSourceFileName = Paths.get(pkgOrSourceFileNameAsString).normalize();
    
            // get the absolute path for the source. source can be a module or a bal file.
            Path sourceFullPath = RepoUtils.isBallerinaProject(sourceRootPath) ?
                                  sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                                          .resolve(pkgOrSourceFileName).toAbsolutePath() :
                                  sourceRootPath.resolve(pkgOrSourceFileName).toAbsolutePath();
    
            // check if source exists or not
            if (Files.notExists(sourceFullPath)) {
                throw LauncherUtils.createLauncherException("the given module or source file does not exist.");
            }
            
            // Checks if the source is a module and if its inside a project (with a Ballerina.toml folder)
            if (!RepoUtils.isBallerinaProject(sourceRootPath)) {
                throw LauncherUtils.createLauncherException("you are trying to compile a module that is not inside " +
                                                            "a project. Run `ballerina new` from " +
                                                            sourceRootPath + " to initialize it as a " +
                                                            "project and then compile the module.");
            }
    
            BuildContext buildContext = new BuildContext(sourceRootPath, pkgOrSourceFileName);
            buildContext.setOut(outStream);
            buildContext.setOut(errStream);
            buildContext.put(BuildContextField.COMPILER_CONTEXT, context);
    
            TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                    .addTask(new CleanTargetDirTask())
                    .addTask(new CreateTargetDirTask())
                    .addTask(new CompileTask())
                    .addTask(new CreateBaloTask())
                    .addTask(new CreateBirTask())
                    .addTask(new CopyNativeLibTask())
                    .addTask(new CreateJarTask())
                    .addTask(new CopyModuleJarTask())
                    .addTask(new RunTestsTask(), this.skipTests)
                    .addTask(new CreateLockFileTask())
                    .addTask(new CreateDocsTask())
                    .build();
    
            taskExecutor.executeTasks(buildContext);
        }
    
        if (exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    @Override
    public String getName() {
        return COMPILE_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Compiles Ballerina modules and create balo files. \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina compile [<module-name>] \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private void genNativeBinary(Path projectDirPath, List<String> argList) {
        throw LauncherUtils.createLauncherException("llvm native generation is not supported");
    }
}
