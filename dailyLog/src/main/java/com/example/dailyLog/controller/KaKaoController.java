package com.example.dailyLog.controller;

import com.example.dailyLog.dto.KakaoTokenDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("kakao")
@Slf4j
public class KaKaoController {

    @GetMapping("login")
    public String kakaoCode(@RequestParam(value = "code") String code) {
        log.info("code {}", code);

        try {
            String url = "https://kauth.kakao.com/oauth/token";
            RestTemplate restTemplate = new RestTemplate();

            MultiValueMap headers = new LinkedMultiValueMap();
            headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("client_id", "477ea0788a39a67ac40fa6b1bc49e7d8");
            body.add("redirect_uri", "http://localhost:5173/oauth");
            body.add("code", code);
            body.add("client_secret", "TbN9J7UNCPyH9mpkDVFJTyNiY5frkU3y");

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<KakaoTokenDto> result = restTemplate.exchange(url,
                    HttpMethod.POST,
                    requestEntity ,
                    KakaoTokenDto.class);
            log.info("result {}", result);


            KakaoTokenDto kakaoTokenDto = result.getBody();

            url = "https://kapi.kakao.com/v2/api/talk/memo/default/send";

            MultiValueMap headers2 = new LinkedMultiValueMap();
            headers2.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
            headers2.add("Authorization", "Bearer " + kakaoTokenDto.getAccess_token());

            MultiValueMap<String, String> body2 = new LinkedMultiValueMap<>();
            body2.add("template_object", messageString());

            HttpEntity<MultiValueMap<String, String>> requestEntity2 = new HttpEntity<>(body2, headers2);

            ResponseEntity<String> result2 = restTemplate.exchange(url,
                    HttpMethod.POST,
                    requestEntity2 ,
                    String.class);
            log.info("msg 카카옥 메시지 전송 성공"+result2.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "kakao code";
    }


    public String messageString(){
        return "{\n" +
                "        \"object_type\": \"text\",\n" +
                "        \"text\": \"곧 쉬는 시간입니다. 사람 불러야되요.\",\n" +
                "        \"link\": {\n" +
                "            \"web_url\": \"http://first.hellomh.site/first/test\",\n" +
                "            \"mobile_web_url\": \"http://first.hellomh.site/first/test\"\n" +
                "        },\n" +
                "        \"button_title\": \"바로 확인\"\n" +
                "    }";
    }


    public String templateString (){
        return "{\n" +
                "        \"object_type\": \"feed\",\n" +
                "        \"content\": {\n" +
                "            \"title\": \"오늘의 MSA\",\n" +
                "            \"description\": \"내용 바꾸기... 내일 더 해볼꼐요... \",\n" +
                "            \"image_url\": \"https://mud-kage.kakao.com/dn/NTmhS/btqfEUdFAUf/FjKzkZsnoeE4o19klTOVI1/openlink_640x640s.jpg\",\n" +
                "            \"image_width\": 640,\n" +
                "            \"image_height\": 640,\n" +
                "            \"link\": {\n" +
                "                \"web_url\": \"http://www.daum.net\",\n" +
                "                \"mobile_web_url\": \"http://m.daum.net\",\n" +
                "                \"android_execution_params\": \"contentId=100\",\n" +
                "                \"ios_execution_params\": \"contentId=100\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"item_content\" : {\n" +
                "            \"profile_text\" :\"Kakao\",\n" +
                "            \"profile_image_url\" :\"https://mud-kage.kakao.com/dn/Q2iNx/btqgeRgV54P/VLdBs9cvyn8BJXB3o7N8UK/kakaolink40_original.png\",\n" +
                "            \"title_image_url\" : \"https://mud-kage.kakao.com/dn/Q2iNx/btqgeRgV54P/VLdBs9cvyn8BJXB3o7N8UK/kakaolink40_original.png\",\n" +
                "            \"title_image_text\" :\"Cheese cake\",\n" +
                "            \"title_image_category\" : \"Cake\",\n" +
                "            \"items\" : [\n" +
                "                {\n" +
                "                    \"item\" :\"Cake1\",\n" +
                "                    \"item_op\" : \"1000원\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"item\" :\"Cake2\",\n" +
                "                    \"item_op\" : \"2000원\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"item\" :\"Cake3\",\n" +
                "                    \"item_op\" : \"3000원\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"item\" :\"Cake4\",\n" +
                "                    \"item_op\" : \"4000원\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"item\" :\"Cake5\",\n" +
                "                    \"item_op\" : \"5000원\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"sum\" :\"Total\",\n" +
                "            \"sum_op\" : \"15000원\"\n" +
                "        },\n" +
                "        \"social\": {\n" +
                "            \"like_count\": 100,\n" +
                "            \"comment_count\": 200,\n" +
                "            \"shared_count\": 300,\n" +
                "            \"view_count\": 400,\n" +
                "            \"subscriber_count\": 500\n" +
                "        },\n" +
                "        \"buttons\": [\n" +
                "            {\n" +
                "                \"title\": \"웹으로 이동\",\n" +
                "                \"link\": {\n" +
                "                    \"web_url\": \"http://www.naver.com\",\n" +
                "                    \"mobile_web_url\": \"http://m.naver.com\"\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"title\": \"앱으로 이동\",\n" +
                "                \"link\": {\n" +
                "                    \"android_execution_params\": \"contentId=100\",\n" +
                "                    \"ios_execution_params\": \"contentId=100\"\n" +
                "                }\n" +
                "            }\n" +
                "        ]\n" +
                "    }";
    }
}
