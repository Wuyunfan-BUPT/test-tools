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

package org.apache.process.repo;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import org.apache.process.action.Deploy;
import org.apache.process.action.ProjectClean;
import org.apache.process.action.QueryTestPod;
import org.apache.process.config.Configs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class nacosTestImpl implements RepoTest {
    public static HashMap<String, String> contextMap;

    public nacosTestImpl(HashMap<String, String> map) {
        contextMap = map;
      }


    @Override
    public boolean deploy() throws IOException, InterruptedException, ApiException {
        String appName = contextMap.get("env");
        String alias = contextMap.get("env");
        String description = contextMap.get("velaAppDescription");
        String repoName = contextMap.getOrDefault("repoName","nacos");
        String chartPath = contextMap.getOrDefault("chartPath","./helm");
        String chartBranch = contextMap.getOrDefault("chartBranch","master");
        String chartGit = contextMap.getOrDefault("chartGit", "https://ghproxy.com/https://github.com/Wuyunfan-BUPT/nacos-docker.git");
        String env = contextMap.get("env");
        String helmValue = contextMap.getOrDefault("helmValue",
             "global:\n" +
                        "  mode: cluster\n" +
                        "nacos:\n" +
                        "  replicaCount: 3\n" +
                        "  image: \n" +
                        "    repository: wuyfeedocker/nacos-ci\n" +
                        "    tag: develop-4f26def4-ccb0-45e5-9989-874e78424bea-8\n" +
                        "  storage:\n" +
                        "    type: mysql\n" +
                        "    db:\n" +
                        "      port: 3306\n" +
                        "      username: nacos\n" +
                        "      password: nacos\n" +
                        "      param: characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useSSL=false\n" +
                        "service:\n" +
                        "  nodePort: 30009\n" +
                        "  type: ClusterIP");

        helmValue = "namespace: "+env+"\n"+ helmValue;
        Deploy deploy = new Deploy();
        return deploy.startDeploy(appName,alias,description,
                repoName,chartPath,chartBranch,chartGit,
                Configs.PROJECT_NAME,env,helmValue);
    }
    @Override
    public boolean testRepo() throws IOException, ApiException, InterruptedException {
        System.out.println("**************E2E TEST...***************");
        String namespace = contextMap.get("env");
        String testCodeGit = contextMap.getOrDefault("testCodeGit", "https://github.com/nacos-group/nacos-e2e.git");
        String testCodeBranch = contextMap.getOrDefault("testCodeBranch", "master");
        String testCodePath = contextMap.getOrDefault("testCodePath", "java/nacos-2X");
        String testCmdBase = contextMap.getOrDefault("testCmdBase", "mvn clean test -B");
        String nacosPort = "8848";

//        ApiClient client = Config.defaultClient();
//        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        /* get all nacos cluster IP */
        V1PodList pods = api.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null, null);
        List<String> serverListIP = new ArrayList<>();
        for (V1Pod pod : pods.getItems()) {
            if (pod.getMetadata().getLabels() != null && "nacos".equals(pod.getMetadata().getLabels().get("app.kubernetes.io/instance"))) {
                serverListIP.add(pod.getStatus().getPodIP() + ":" + nacosPort);
            }
        }

        System.out.println("**************create pod***************");
        /* create pod */
        HashMap<String, Quantity> resourcesLimits = new HashMap<>();
        resourcesLimits.put("cpu", new Quantity("8"));
        resourcesLimits.put("memory", new Quantity("8Gi"));
        HashMap<String, Quantity> resourceRequests = new HashMap<>();
        resourceRequests.put("cpu", new Quantity("8"));
        resourceRequests.put("memory", new Quantity("8Gi"));

        String testPodName = "test-" + namespace + new Random().nextInt(100000);
        // create a pod
        V1Pod pod_template = new V1Pod();
        pod_template.setApiVersion("v1");
        pod_template.setKind("Pod");
        // set pod: metadata
        pod_template.setMetadata(new V1ObjectMeta().name(testPodName).namespace(namespace));//
        // set pod: spec
        V1PodSpec pod_template_spec = new V1PodSpec();
        pod_template_spec.setRestartPolicy("Never");
        // set pod spec: container
        V1Container container = new V1Container();
        System.out.println("**********************" + testPodName + "****************");


        // ***********set container***********
        // set pod spec container: name and images
        container.setName(testPodName);
        container.setImage("cloudnativeofalibabacloud/test-runner:v0.0.1");
        // set pod spec container: resource
        V1ResourceRequirements v1ResourceRequirements = new V1ResourceRequirements();
        v1ResourceRequirements.setLimits(resourcesLimits);
        v1ResourceRequirements.setRequests(resourceRequests);
        container.setResources(v1ResourceRequirements);

        // set V container: env elements
        V1EnvVar v1EnvVar1 = new V1EnvVar();
        v1EnvVar1.setName("CODE");
        v1EnvVar1.setValue(testCodeGit);
        container.addEnvItem(v1EnvVar1);

        V1EnvVar v1EnvVar2 = new V1EnvVar();
        v1EnvVar2.setName("BRANCH");
        v1EnvVar2.setValue(testCodeBranch);
        container.addEnvItem(v1EnvVar2);

        V1EnvVar v1EnvVar3 = new V1EnvVar();
        v1EnvVar3.setName("CODE_PATH");
        v1EnvVar3.setValue(testCodePath);
        container.addEnvItem(v1EnvVar3);

        String testCmd = testCmdBase;
        if (testCmd.contains("mvn")) {
            testCmd = testCmdBase + " -Dnacos.client.version=2.2.3";
        }


        V1EnvVar v1EnvVar4 = new V1EnvVar();
        v1EnvVar4.setName("CMD");
        v1EnvVar4.setValue(testCmd);
        container.addEnvItem(v1EnvVar4);

        V1EnvVar v1EnvVar5 = new V1EnvVar();
        v1EnvVar5.setName("serverList");
        StringBuilder serverList = new StringBuilder();
        for (String serverIP : serverListIP) {
            serverList.append(serverIP).append(",");
        }
        System.out.println(serverList.substring(0, serverList.length() - 1));
        v1EnvVar5.setValue(serverList.substring(0, serverList.length() - 1));
        container.addEnvItem(v1EnvVar5);
        // add container to spec
        pod_template_spec.addContainersItem(container);

        // add spec to pod
        pod_template.setSpec(pod_template_spec);

        // create pod
        V1Pod createdPod = api.createNamespacedPod(namespace, pod_template, null, null, null, null);
        System.out.println(createdPod.getStatus().getPhase());
        return new QueryTestPod().getPodResult(testPodName, namespace, testCmd, testCodePath);
    }
    @Override
    public void clean() throws IOException {
        new ProjectClean().clean(contextMap.get("env"), contextMap.get("env"));
    }
}
