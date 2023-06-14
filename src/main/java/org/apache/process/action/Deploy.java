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

package org.apache.process.action;

import okhttp3.Response;
import org.apache.process.api.AppActions;
import org.apache.process.api.AuthAction;
import org.apache.process.api.EnvActions;
import org.apache.process.config.Configs;
import org.apache.process.model.Deploymodel;
import org.apache.process.utils.PrintInfo;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;


public class Deploy {
    public boolean startDeploy(HashMap<String, Object> paramsMap) throws  InterruptedException {
        System.out.println("************************************");
        System.out.println("*     Create namespace and deploy...     *");
        System.out.println("************************************");

        AuthAction authAction = new AuthAction();
        authAction.setToken("login");
        TimeUnit.SECONDS.sleep(1);

        String namespace = paramsMap.get("namespace").toString();

        System.out.printf("Generate namespace(%s) and namespace namespace(%s)%n", namespace, namespace);
        try{
            EnvActions envActions = new EnvActions();
            String envBodyContent = String.format(Deploymodel.ENV_BODY, namespace, namespace, Configs.PROJECT_NAME, namespace);
            Response response = envActions.createenv(envBodyContent);
            PrintInfo.printRocketInfo(response, String.format("Generate namespace(%s) success!", namespace));
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        System.out.printf("Generate %s Application%n", namespace);
        AppActions appActions = new AppActions();
        try{
            authAction.setToken("refresh_token");
            String componentProperty = paramsMap.get("helm").toString();
            String bodyContent = String.format(Deploymodel.APPLICATION_BODY_COMPONENT, namespace, Configs.PROJECT_NAME, paramsMap.get("velaAppDescription"), namespace, namespace, paramsMap.get("repoName"), componentProperty);
            Response createAppResponse = appActions.createApplication(bodyContent);
            PrintInfo.printRocketInfo(createAppResponse, String.format(String.format("Generate %s Application success!", namespace)));
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        System.out.printf("deploy %s Application%n", namespace);
        try{
            String workflowName = "workflow-"+namespace;
            String deployBodyContent = String.format(Deploymodel.DEPLOY_APP_BODY, workflowName);
            authAction.setToken("refresh_token");
            Response response = appActions.deployOrUpgradeApplication(namespace, deployBodyContent);
            PrintInfo.printRocketInfoAndExit(response, String.format("deploy %s Application success!", namespace));
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        System.out.printf("Query %s Application status%n",namespace);

        int querryTime = Integer.parseInt(paramsMap.get("waitTimes").toString()) / 5;
            try{
                while(querryTime>0) {
                    authAction.setToken("refresh_token");
                    Response response = appActions.getApplicationStatus(namespace, namespace);
                    JSONObject json;
                    if (response.body() != null) {
                        json = new JSONObject(response.body().string());
                        response.close();
                    }else{
                        response.close();
                        continue;
                    }

                    String workflowsStatus = json.getJSONObject("status").getJSONObject("workflow").getString("status");
                    String message =  new JSONObject(json.getJSONObject("status").getJSONArray("services").get(0).toString()).getString("message");

                    if ("succeeded".equals(workflowsStatus)) {
                        System.out.println("message: " + message);
                        break;
                    } else if ("executing".equals(workflowsStatus)) {
                        System.out.println("waiting...");
                        System.out.println("message: " + message);
                        querryTime--;
                        TimeUnit.SECONDS.sleep(5);
                    } else {
                        System.out.println(message);
                        return false;
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            return true;
    }
}
