package com.github.hkzorman.avakinitemdb.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class AzureTranslatorService {

    private static final String key = "34d872f6a7af4fdfb3e3480cabd761dc";
    private static final String location = "<YOUR-RESOURCE-LOCATION>";
    private final HttpClient client;

    public AzureTranslatorService() {
        this.client = HttpClient.newHttpClient();
    }

    public TranslationResult translate(String text, String language) throws Exception {
        if (language == null) language = "en";
        var body = "[{\"Text\": \"" + text + "\"}]";

        var request = HttpRequest.newBuilder(URI.create("https://api.cognitive.microsofttranslator.com/translate?api-version=3.0&to=" + language))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Ocp-Apim-Subscription-Key", key)
                .header("Content-Type", "application/json")
                .build();
        var httpResponse = this.client.send(request, HttpResponse.BodyHandlers.ofString());
        var mapper = new ObjectMapper();
        var response = mapper.readValue(httpResponse.body(), TranslationResponse.class);
        return new TranslationResult(response.detectedLanguage.language, response.translations[0].text);
    }

    /**
     * Sample response:
     * [{
     *     "detectedLanguage": {
     *         "language": "en",
     *         "score": "0.34"
     *     }
     *     "translations": [{
     *         "text": "AA",
     *         "to": "es"
     *     }]
     * }]
     */
    private class TranslationResponse {
        private LanguageInfo detectedLanguage;

        private Translation[] translations;

        private class LanguageInfo {
            public String language;
            public double score;
        }

        private class Translation {
            public String text;
            public String language;
        }
    }

    public static class TranslationResult {
        private final String detectedLanguage;
        private final String output;

        public TranslationResult(String detectedLanguage, String text) {
            this.detectedLanguage = detectedLanguage;
            this.output = text;
        }

        public String getDetectedLanguage() {
            return detectedLanguage;
        }

        public String getOutput() {
            return output;
        }
    }
}
