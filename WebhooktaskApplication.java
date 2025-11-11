package com.bajaj.webhooktask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class WebhooktaskApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebhooktaskApplication.class, args);
	}
}

@Component
class StartupRunner {

	@EventListener(ApplicationReadyEvent.class)
	public void executeOnStartup() {
		RestTemplate restTemplate = new RestTemplate();

		String myName = "Anish Sawhney";
		String myRegNo = "U25UV22T029005";
		String myEmail = "anish.sawhney@campusuvce.in";

		try {
			Map<String, String> body = new HashMap<>();
			body.put("name", myName);
			body.put("regNo", myRegNo);
			body.put("email", myEmail);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

			String webhookUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
			ResponseEntity<Map> response = restTemplate.postForEntity(webhookUrl, requestEntity, Map.class);

			Map<String, Object> responseData = response.getBody();
			if (responseData == null) return;

			String token = (String) responseData.get("accessToken");

			String query =
					"SELECT p.AMOUNT AS SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
							"TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, d.DEPARTMENT_NAME " +
							"FROM PAYMENTS p " +
							"JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
							"JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
							"WHERE DAY(p.PAYMENT_TIME) != 1 " +
							"ORDER BY p.AMOUNT DESC LIMIT 1";

			Map<String, String> solutionBody = new HashMap<>();
			solutionBody.put("finalQuery", query);

			HttpHeaders submitHeaders = new HttpHeaders();
			submitHeaders.setContentType(MediaType.APPLICATION_JSON);
			submitHeaders.set("Authorization", token);
			HttpEntity<Map<String, String>> submitRequest = new HttpEntity<>(solutionBody, submitHeaders);

			String submitUrl = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";
			ResponseEntity<String> submitResponse = restTemplate.postForEntity(submitUrl, submitRequest, String.class);

			System.out.println("Status: " + submitResponse.getStatusCode());
			System.out.println(submitResponse.getBody());
			System.out.println("Webhook processed successfully for Question 1!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}