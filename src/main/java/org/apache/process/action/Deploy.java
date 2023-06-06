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

import io.kubernetes.client.openapi.ApiException;
import okhttp3.Response;
import org.apache.process.api.AppActions;
import org.apache.process.api.AuthAction;
import org.apache.process.api.EnvActions;
import org.apache.process.model.Deploymodel;
import org.apache.process.utils.PrintInfo;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class Deploy {
    public boolean runDeployTestTools(String appName, String alias, String description, String repoName, String chartPath, String chartBranch, String chartGit, String projectName, String env,String helmValues) throws IOException, InterruptedException, ApiException {
        System.out.println("************************************");
        System.out.println("*     Create env and deploy...     *");
        System.out.println("************************************");

        AuthAction authAction = new AuthAction();
        authAction.setToken("login");
        TimeUnit.SECONDS.sleep(1);

        System.out.printf("Generate env(%s) and env namespace(%s)%n", env, env);
        try{
            EnvActions envActions = new EnvActions();
            String envBodyContent = String.format(Deploymodel.ENV_BODY, env, env, projectName, env);
            Response response = envActions.createenv(envBodyContent);
            PrintInfo.printRocketInfo(response, String.format("Generate env(%s) success!", env));
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        System.out.printf("Generate %s Application%n", appName);
        AppActions appActions = new AppActions();
        try{
            authAction.setToken("refresh_token");
            String componentProperty = Deploymodel.generateComponentProperties(helmValues, chartPath, chartBranch, chartGit);
            //String componentProperty = String.format(Deploymodel.COMPONENT_PROPERTY1, chartPath, chartBranch, chartGit); //env,
            String bodyContent = String.format(Deploymodel.APPLICATION_BODY_COMPONENT, appName, projectName, description, alias, env, repoName, componentProperty);
            Response createAppResponse = appActions.createApplication(bodyContent);
            PrintInfo.printRocketInfo(createAppResponse, String.format(String.format("Generate %s Application success!", appName)));
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println(String.format("deploy %s Application", appName));
        try{
            String workflowName = "workflow-"+appName;
            String deployBodyContent = String.format(Deploymodel.DEPLOY_APP_BODY, workflowName);
            authAction.setToken("refresh_token");
            Response response = appActions.deployOrUpgradeApplication(appName, deployBodyContent);
            PrintInfo.printRocketInfoAndExit(response, String.format("deploy %s Application success!", appName));
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        System.out.printf("Query %s Application status%n",appName);

        int querryTime = 120;
            try{
                while(querryTime>0) {
                    authAction.setToken("refresh_token");
                    Response response = appActions.getApplicationStatus(appName, env);
                    JSONObject json = null;
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
                        querryTime++;
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