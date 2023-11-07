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

import okhttp3.Response;

import java.io.IOException;

public class PrintInfo {

    public static void printRocketInfo(Response response, String name) throws IOException {
        if(response.isSuccessful()){
            System.out.println(name);
        }else{
            System.out.println("Fail! "+ response.body().string());
        }
    }

    public static void printRocketInfoAndExit(Response response, String name){
        if(response.isSuccessful()){
            System.out.println(name);
        }else{
            System.out.println(response.message());
            System.exit(1);
        }
        response.close();
    }
    public static boolean isResponseSuccess(Response response){
       boolean isSuccessed = response.isSuccessful();
       response.close();
       return isSuccessed;
    }
}
