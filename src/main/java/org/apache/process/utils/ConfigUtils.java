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

package org.apache.process.utils;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.Config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;


public class ConfigUtils {

    public String setConfig(String kubeConfig) throws IOException {

        String usrHome = System.getProperty("user.home");
        String kubeDirPath = String.format("%s/.kube", usrHome);
        File kubeDir = new File(kubeDirPath);
        if (!kubeDir.exists() && !kubeDir.mkdirs()) {
            System.out.printf("%s directory create fail！%n", kubeDirPath);
        }
        String kubeFilePath = String.format("%s/.kube/config", usrHome);
        File kubeFile = new File(kubeFilePath);
        if (kubeDir.exists()) {
            kubeFile.delete();
        }
        if (!kubeFile.createNewFile()) {
            System.out.printf("%s create fail！%n", kubeFilePath);
        }
        try {
            // 覆盖模式写
            FileWriter fileWriter = new FileWriter(kubeFilePath);
            fileWriter.write(kubeConfig);
            fileWriter.close();
        } catch (IOException e) {
            System.out.printf("write %s error!%n", kubeFilePath);
        }
        return kubeFilePath;
    }
    public void setKubeClientConfig(String kubeConfigPath) throws IOException {
        ApiClient client = Config.fromConfig(kubeConfigPath);
        Configuration.setDefaultApiClient(client);
    }

    /**
     * get velaUX username and password
     * @param kubeConfig ask config
     * @return username:password
     */
    public String getAuthInfoFromConfig(String kubeConfig){
        String text = kubeConfig.length()>150?kubeConfig.substring(kubeConfig.length() - 150):kubeConfig;
        StringBuilder userName = new StringBuilder();
        StringBuilder password = new StringBuilder();
        boolean digitMark = false;
        for(int index=text.length()-1;index>=0;index--){
            if(userName.length()>=6 && password.length()>= 12){
                break;
            }
            boolean isLetter = Character.isLetter(text.charAt(index));
            boolean isDigit = Character.isDigit(text.charAt(index));
            if(isDigit || isLetter){
                if(isLetter && userName.length()<6){
                    userName.append(Character.toLowerCase(text.charAt(index)));
                }
                if(password.length()<12){
                    if(digitMark && isDigit){
                        password.append(text.charAt(index));
                        digitMark = false;
                    }else if(!digitMark && isLetter){
                        password.append(text.charAt(index));
                        digitMark = true;
                    }
                }
            }
        }
        return userName+":"+password;
    }

    /**
     * decoder ask config.
     * @param config ask config.
     * @return ask config after base64 decoder.
     */
    public String base64Decoder(String config){
        byte [] base64Bytes = Base64.getDecoder().decode(config.replace("\n", ""));
        return new String(base64Bytes);
    }
}
