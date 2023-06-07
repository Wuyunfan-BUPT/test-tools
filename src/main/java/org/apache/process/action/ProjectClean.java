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
import io.kubernetes.client.openapi.apis.CoreV1Api;
import org.apache.process.api.AppActions;
import org.apache.process.api.AuthAction;
import org.apache.process.api.EnvActions;
import org.apache.process.utils.PrintInfo;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ProjectClean {
    public ProjectClean(){}
    public void clean(String namespace, String appName){
        System.out.println("************************************");
        System.out.println("*       Delete app and env...      *");
        System.out.println("************************************");

//        ApiClient client = Config.fromConfig(Configs.KUBECONFIG_PATH);
//        //ApiClient client = Config.defaultClient();
//        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        /* delete vela application and namespace */
        try{
            AuthAction authAction = new AuthAction();
            authAction.setToken("refresh_token");
            AppActions appActions = new AppActions();
            appActions.deleteOAM(namespace, appName).close();
            boolean isDeletedsuccessed = false;
            while(!isDeletedsuccessed){
                isDeletedsuccessed = PrintInfo.isResponseSuccess(appActions.deleteApplication(appName));
                TimeUnit.SECONDS.sleep(2);
                authAction.setToken("refresh_token");
            }
            System.out.printf("vela application:%s delete success!%n", appName);
            EnvActions envActions = new EnvActions();
            isDeletedsuccessed = false;
            while(!isDeletedsuccessed){
                isDeletedsuccessed = PrintInfo.isResponseSuccess(envActions.deleteEnv(namespace));
                TimeUnit.SECONDS.sleep(2);
                authAction.setToken("refresh_token");
            }
            System.out.printf("vela namespace:%s delete success!%n", namespace);
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        /* delete kubernetes pods and relevant namespace */
        try {
            api.deleteNamespace(namespace, null, null, null, null, null, null);
            System.out.println("Namespace " + namespace + " deleted successfully.");
        }catch (ApiException e){
            System.err.println("Failed to delete namespace " + namespace + ": " + e.getResponseBody());
        }
    }
}
