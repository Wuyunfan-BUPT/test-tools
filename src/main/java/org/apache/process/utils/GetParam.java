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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class GetParam {
    public HashMap<String, String> setParam(CommandLine cmd) {
        HashMap<String, String> result = new HashMap<>();
        for(Option option:cmd.getOptions()){
            System.out.println(option.getLongOpt()+":"+option.getValue());
            result.put(option.getLongOpt(), option.getValue());
        }
        return result;
    }

    public static HashMap<String, String> parseParams(String s, String target){
        StringBuilder builder = new StringBuilder();
        StringBuilder targetBuilder = new StringBuilder();
        String[] lines = s.split("\n");
        boolean isTargetText = false;
        for(String line:lines){
            if(line.startsWith(target)){
                isTargetText = true;
                continue;
            }
            if(isTargetText && line.startsWith("  ")){
                targetBuilder.append(line).append("\n");
            }else{
                builder.append("  ").append(line).append("\n");
                isTargetText = false;
            }
        }
        Yaml yaml = new Yaml();
        HashMap<String,String> builderMap= (HashMap<String,String>) yaml.load(builder.toString());
        HashMap<String,String> targetMap= (HashMap<String, String>) yaml.load(targetBuilder.toString());
        JSONObject jsonObject=new JSONObject(targetMap);
        builderMap.put(target, jsonObject.toString().replaceAll("\"", "\\\\\"").toString());
        return builderMap;
    }
}
