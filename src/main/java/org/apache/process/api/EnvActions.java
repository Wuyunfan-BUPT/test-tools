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

package org.apache.process.api;

//import com.squareup.okhttp.*;
import okhttp3.*;
import org.apache.process.config.Configs;

import java.io.IOException;

public class EnvActions {
    public static final String APP_API = "envs";

    public String URL;
    public EnvActions(){
        URL = "http://"+ Configs.IP +"/"+ Configs.KUBEVELA_API + "/" + APP_API;
    }

    public Response listEnv() throws IOException {
        OkHttpClient client = new OkHttpClient();


        Request request = new Request.Builder()
                .url(URL)
                .get()
                .addHeader("Accept", "application/json, application/xml")
                .addHeader("Authorization", Configs.Authorization)
                .build();

        return client.newCall(request).execute();
    }
    public Response createenv(String bodyContent) throws IOException{
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, bodyContent);

        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json, application/xml")
                .addHeader("Authorization", Configs.Authorization)
                .build();

        return client.newCall(request).execute();
    }

    public Response deleteEnv(String namespace) throws IOException {
        String url = URL + "/" +namespace;
        OkHttpClient client = new OkHttpClient();


        Request request = new Request.Builder()
                .url(url)
                .delete(null)
                .addHeader("Accept", "application/json, application/xml")
                .addHeader("Authorization", Configs.Authorization)
                .build();

        return client.newCall(request).execute();
    }

}
