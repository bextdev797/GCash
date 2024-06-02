package com.bextdev.gcash;

import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GCash extends AndroidNonvisibleComponent {
  private String gCashApiKey;
  private int gCashAmount;
  private String gCashId;
  private String gCashDescription;

  public GCash(ComponentContainer container) {
    super(container.$form());
  }

  @SimpleFunction
  public void StartPayment(String apiKey) {
    gCashApiKey = apiKey;
    String urlString = "https://api.paymongo.com/v1/payments";
    HttpURLConnection httpConnection = null;
    try {
      URL url = new URL(urlString);
      httpConnection = (HttpURLConnection) url.openConnection();

      httpConnection.setRequestMethod("POST");
      httpConnection.setRequestProperty("Content-Type", "application/json");
      httpConnection.setRequestProperty("Authorization", "Bearer " + gCashApiKey);
      httpConnection.setDoOutput(true);

      String jsonInputString = "{\"data\": {\"attributes\": {\"amount\": " + gCashAmount + ", \"source\": {\"id\": \"" + gCashId + "\", \"type\": \"source\"}, \"currency\": \"PHP\", \"description\": \"" + gCashDescription + "\"}}}";

      try (OutputStream os = httpConnection.getOutputStream()) {
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);
      }

      int responseCode = httpConnection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_CREATED) {
        Success("Payment successfully created.");
      } else {
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream(), "utf-8"))) {
          String responseLine;
          while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
          }
        }
        Failed("Payment failed. Response Code: " + responseCode + ". Response: " + response.toString());
      }
    } catch (Exception e) {
      Failed("Payment failed. Error: " + e.getMessage());
    } finally {
      if (httpConnection != null) {
        httpConnection.disconnect();
      }
    }
  }

  @SimpleProperty
  public void Amount(int amount) {
    gCashAmount = amount;
  }

  @SimpleProperty
  public void Id(String id) {
    gCashId = id;
  }

  @SimpleProperty
  public void Description(String description) {
    gCashDescription = description;
  }

  @SimpleEvent
  public void Success(String successDescription) {
    EventDispatcher.dispatchEvent(this, "Success", successDescription);
  }

  @SimpleEvent
  public void Failed(String error) {
    EventDispatcher.dispatchEvent(this, "Failed", error);
  }
}