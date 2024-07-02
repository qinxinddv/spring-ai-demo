package com.example.spring_ai_demo.utils;
import okhttp3.*;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class TestFuyu
{
    public static final String API_KEY = "HF6YnnsOJEpZejAxMikL8Z2b";
    public static final String SECRET_KEY = "rVnDEK6VZKCTKWKnAzWg3DKzZzHEV9f1";

    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    public static void main(String []args) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");

        Map<String, String> reqMap = new HashMap<>();
        reqMap.put("prompt","图片里有什么字");
        File file = new File("C:\\Users\\qinxi\\Desktop\\文字.jpg");
        FileInputStream fileInputStream = new FileInputStream(file);

        String base64 = Base64.getEncoder().encodeToString(fileInputStream.readAllBytes());
        reqMap.put("image", base64);
        String content = JSONObject.valueToString(reqMap);
        RequestBody body = RequestBody.create(mediaType, content);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/image2text/fuyu_8b?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        System.out.println(response.body().string());

    }


    /**
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     * @throws IOException IO异常
     */
    static String getAccessToken() throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + API_KEY
                + "&client_secret=" + SECRET_KEY);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return new JSONObject(response.body().string()).getString("access_token");
    }
}
