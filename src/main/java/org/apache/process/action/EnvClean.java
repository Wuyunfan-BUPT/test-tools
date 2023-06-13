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

import java.util.HashMap;
import java.util.concurrent.TimeUnit;


public class EnvClean {
    public EnvClean(){}
    public boolean clean(HashMap<String, Object> paramsMap){
        System.out.println("************************************");
        System.out.println("*       Delete app and env...      *");
        System.out.println("************************************");

        CoreV1Api api = new CoreV1Api();
        String namespace = paramsMap.get("namespace").toString();

        boolean result = true;
        /* delete vela application and namespace */
        try{
            AuthAction authAction = new AuthAction();
            authAction.setToken("login");
            AppActions appActions = new AppActions();
            appActions.deleteOAM(namespace, namespace).close();
            boolean isDeletedsuccessed = false;
            int times = 10;
            while(!isDeletedsuccessed && times>0){
                isDeletedsuccessed = PrintInfo.isResponseSuccess(appActions.deleteApplication(namespace));
                TimeUnit.SECONDS.sleep(3);
                times--;
                authAction.setToken("refresh_token");
            }
            System.out.printf("vela application:%s delete success!%n", namespace);
            EnvActions envActions = new EnvActions();
            isDeletedsuccessed = false;
            times = 10;
            while(!isDeletedsuccessed && times>0){
                isDeletedsuccessed = PrintInfo.isResponseSuccess(envActions.deleteEnv(namespace));
                TimeUnit.SECONDS.sleep(3);
                times --;
                authAction.setToken("refresh_token");
            }
            System.out.printf("vela namespace:%s delete success!%n", namespace);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Fail to delete vela application/namespace!");
            result = false;
        }

        /* delete kubernetes pods and relevant namespace */
        try {
            api.deleteNamespace(namespace, null, null, null, null, null, null);
            System.out.println("Namespace " + namespace + " deleted successfully.");
        }catch (ApiException e){
            System.err.println("Failed to delete namespace " + namespace + ": " + e.getResponseBody());
            result = false;
        }
        return result;
    }
}
