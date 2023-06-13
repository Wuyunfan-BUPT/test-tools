package org.apache.process.utils;

import java.util.Base64;

public class Decoder {
    public static String base64Decoder(String config){
        byte [] base64Bytes = Base64.getDecoder().decode(config.replace("\n", ""));
        return new String(base64Bytes);
    }
}
