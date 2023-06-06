/*
 * #
 * # Licensed to the Apache Software Foundation (ASF) under one or more
 * # contributor license agreements.  See the NOTICE file distributed with
 * # this work for additional information regarding copyright ownership.
 * # The ASF licenses this file to You under the Apache License, Version 2.0
 * # (the "License"); you may not use this file except in compliance with
 * # the License.  You may obtain a copy of the License at
 * #
 * #     http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 * #
 */

package org.apache.process;

import io.kubernetes.client.openapi.ApiException;
//import io.kubernetes.client.util.Yaml;
import io.kubernetes.client.util.exception.CopyNotSupportedException;

import org.apache.process.action.PortForward;
import org.apache.process.repo.RepoTest;
import org.apache.process.repo.TestImplLoader;
import org.apache.commons.cli.*;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.process.config.Configs;
import org.apache.process.utils.GetParam;
import org.apache.process.utils.SetConfig;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.util.*;
//import java.util.Base64;


/**
 * Hello world!
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    //static final Base64.Decoder DECODER = Base64.getDecoder();


    public static void main(String[] args) throws IOException, InterruptedException, ApiException, CopyNotSupportedException, GitAPIException, MavenInvocationException, ParseException {
        /* define input parameters */
        Options options = new Options();
        options.addOption(Option.builder("testRepo").longOpt("testRepo").argName("testRepo").desc("test repo, currently support [nacos, rocketmq]").hasArg().required(true).build());
        options.addOption(Option.builder("action").longOpt("action").argName("action").desc("an string array, you can input [deploy, e2e-test, test-local and clean]").hasArg().required(false).build());
        options.addOption(Option.builder("version").longOpt("version").argName("test-version").desc("mark workflow").hasArg().required(false).build());
        options.addOption(Option.builder("askConfig").longOpt("askConfig").argName("askConfig").desc("ASK cluster config").hasArg().required(false).build());
        options.addOption(Option.builder("velauxUsername").longOpt("velauxUsername").argName("velaxUserName").desc("velaux username").hasArg().required(true).build());
        options.addOption(Option.builder("velauxPassword").longOpt("velauxPassword").argName("velauxPassword").desc("velaux password").hasArg().required(true).build());
        options.addOption(Option.builder("chartGit").longOpt("chartGit").argName("chartGit").desc("helm chart resposity").hasArg().required(false).build());
        options.addOption(Option.builder("chartBranch").longOpt("chartBranch").argName("chartBranch").desc("helm chart resposity branch").hasArg().required(false).build());
        options.addOption(Option.builder("chartPath").longOpt("chartPath").argName("chartPath").desc("helm chart path in resposity").hasArg().required(false).build());
        options.addOption(Option.builder("testCodeGit").longOpt("testCodeGit").argName("testCodeGit").desc("test code resposity").hasArg().required(false).build());
        options.addOption(Option.builder("testCodeBranch").longOpt("testCodeBranch").argName("testCodeBranch").desc("test code resposity branch").hasArg().required(false).build());
        options.addOption(Option.builder("testCodePath").longOpt("testCodePath").argName("testCodePath").desc("test code path in resposity").hasArg().required(false).build());
        options.addOption(Option.builder("testCmdBase").longOpt("testCmdBase").argName("testCmdBase").desc("execute testcode command").hasArg().required(false).build());
        options.addOption(Option.builder("jobIndex").longOpt("jobIndex").argName("jobIndex").desc("job index").hasArg().required(false).build());
        options.addOption(Option.builder("helmValue").longOpt("helmValue").argName("helmValue").desc("helm chart Value").hasArg().required(false).build());

        /* parse input parameters */
        CommandLine cmd;
        CommandLineParser parser = new DefaultParser();
        HelpFormatter helper = new HelpFormatter();
        HashMap<String, String> paramsMap = null;
        try {
            cmd = parser.parse(options, args);
            GetParam getParam = new GetParam();
            paramsMap = getParam.setParam(cmd);
        }catch (ParseException e) {
            System.out.println(e.getMessage());
            helper.printHelp("Usage:", options);
            System.exit(1);
        }

//        String GITHUB_WORKFLOW = "PUSH-CI";
//        String GITHUB_RUN_ID = "5118080574";//System.getenv("GITHUB_RUN_ID");
        String repoName = paramsMap.get("testRepo");
        //String env = repoName + "-" + GITHUB_RUN_ID + "-" + paramsMap.getOrDefault("jobIndex", "0");  //repoName+"-"+System.getenv("GITHUB_RUN_ID")+"-"+jobIndex;
        String env = repoName+"-"+System.getenv("GITHUB_RUN_ID")+"-"+ paramsMap.getOrDefault("jobIndex", "0"); ;
        //String velaAppDescription = repoName+"-"+GITHUB_WORKFLOW + "-"+GITHUB_RUN_ID+ "@" + paramsMap.get("version");
        String velaAppDescription = repoName+"-"+System.getenv("GITHUB_WORKFLOW") + "-"+System.getenv("GITHUB_RUN_ID") + "@"+paramsMap.get("version");

        paramsMap.put("repoName", repoName);
        paramsMap.put("env", env);
        paramsMap.put("velaAppDescription", velaAppDescription);

        try{
            Configs.VELAUX_USERNAME = paramsMap.get("velauxUsername");
            Configs.VELAUX_PASSWORD = paramsMap.get("velauxPassword");
            SetConfig setConfig = new SetConfig();
            setConfig.setConfig(paramsMap.get("askConfig"));
            RepoTest repoTest =new TestImplLoader(paramsMap.get("testRepo"), paramsMap).getRepoTest();
            if(repoTest!=null) {
                new PortForward().startPortForward(Configs.VELA_NAMESPACE, Configs.VELA_POD_LABELS, Configs.PORT_FROWARD);
                if(!repoTest.deploy()){
                    System.out.println("Deploy error!");
                    System.exit(1);
                }
                repoTest.testRepo();

                repoTest.clean();
                System.exit(0);
            }else{
                System.out.printf("Not support %s! %n", paramsMap.get("testRepo"));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        System.exit(1);
    }
}
