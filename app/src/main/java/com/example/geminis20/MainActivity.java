package com.example.geminis20;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.common.GenerateContentRequest;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.SafetySetting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.15f;
        configBuilder.topK = 32;
        configBuilder.topP = 1f;
        configBuilder.maxOutputTokens = 4096;

        ArrayList<SafetySetting> safetySettings = new ArrayList();
        safetySettings.add(new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE));
        safetySettings.add(new SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE));
        safetySettings.add(new SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE));
        safetySettings.add(new SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE));

        GenerativeModel generativeModel = new GenerativeModel(
                "gemini-1.5-flash-001",
                BuildConfig.GEMINI_API_KEY,
                configBuilder.build(),
                safetySettings
        );
        GenerativeModelFutures model = GenerativeModelFutures.from(generativeModel);
        //Log.d("Main Activity","BuildConfigField = " + BuildConfig.MAPS_API_KEY);
        Button generateBtn = findViewById(R.id.generateBtn);
        generateBtn.setOnClickListener(v -> {
            TextView promptTextView = findViewById(R.id.promptTxt);
            String prompt = promptTextView.getText().toString();
            TextView responseTextView = findViewById(R.id.responseTxt);
            Content content = new Content.Builder()
                    .addText(prompt)
                    .build();

            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
            response.addListener(() ->{
                try{
                    GenerateContentResponse result = response.get(); // Get the result
                    String resultText = result.getText();

                    // Update UI on the main thread
                    runOnUiThread(() -> {
                        responseTextView.setText(resultText);
                        System.out.println(resultText);
                    });

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }, ContextCompat.getMainExecutor(this));
        });
    }
}