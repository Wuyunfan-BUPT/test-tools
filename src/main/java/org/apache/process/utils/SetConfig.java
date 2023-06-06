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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class SetConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetConfig.class);


    public void setConfig(String kubeConfig) throws IOException {

        LOGGER.info("Set config... ");
        ;
        String usrHome = System.getProperty("user.home");
        System.out.println(usrHome);

        String kubeDirPath = String.format("%s/.kube", usrHome);
        File kubeDir = new File(kubeDirPath);
        if (!kubeDir.exists() && !kubeDir.mkdirs()) {
            LOGGER.error(String.format("%s directory create fail！", kubeDirPath));
            System.out.printf("%s directory create fail！%n", kubeDirPath);
        }
        String kubeFilePath = String.format("%s/.kube/config", usrHome);
        System.setProperty("KUBECONFIG", kubeFilePath);
        File kubeFile = new File(kubeFilePath);
        if (kubeDir.exists()) {
            kubeFile.delete();
        }
        if (!kubeFile.createNewFile()) {
            LOGGER.error(String.format("%s create fail！", kubeFilePath));
            System.out.printf("%s create fail！%n", kubeFilePath);
        }
        try {
            // 覆盖模式写
            FileWriter fileWriter = new FileWriter(kubeFilePath);
            fileWriter.write(kubeConfig);
            System.out.println(kubeConfig.substring(0,12)+"   "+ kubeConfig.substring(kubeConfig.length()-12, kubeConfig.length()));
            fileWriter.close();
        } catch (IOException e) {
            LOGGER.error(String.format("write %s error!", kubeFilePath));
            System.out.printf("write %s error!%n", kubeFilePath);
        }

    }
}
