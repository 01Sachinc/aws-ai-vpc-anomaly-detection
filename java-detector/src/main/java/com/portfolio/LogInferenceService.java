package com.portfolio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.*;

/**
 * LogInferenceService - Professional AI Anomaly Detection Engine
 * 
 * @author Sachin C S
 */
public class LogInferenceService {

    private static final String BUCKET_NAME = "prod-log-analytics-storage";
    private static final String LOG_KEY = "ingested/access_logs.csv";
    private static final String REPORT_KEY = "results/anomaly_report_v1.json";

    public static void main(String[] args) {
        System.out.println("Starting Java-based Log Anomaly Detection...");

        try {
            // 1. Download logs from S3 (Simulation if no credentials)
            downloadLogs();

            // 2. Perform Statistical Analysis
            List<Map<String, String>> anomalies = analyzeLogs("local_logs.csv");

            // 3. Generate and Upload Report
            if (!anomalies.isEmpty()) {
                generateReport(anomalies);
            } else {
                System.out.println("No anomalies found.");
            }

        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void downloadLogs() {
        System.out.println("Downloading logs from S3: " + LOG_KEY);
        // Note: Production code would use S3Client
        // For portfolio demonstration, we assume logs are synced via Bash or exist
        // locally
    }

    private static List<Map<String, String>> analyzeLogs(String path) throws IOException {
        System.out.println("Analyzing logs using Z-Score statistical method...");
        List<Double> cpuValues = new ArrayList<>();
        List<String[]> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                lines.add(values);
                cpuValues.add(Double.parseDouble(values[1])); // cpu_usage
            }
        }

        double mean = cpuValues.stream().mapToDouble(val -> val).average().orElse(0.0);
        double variance = cpuValues.stream().mapToDouble(val -> Math.pow(val - mean, 2)).average().orElse(0.0);
        double stdDev = Math.sqrt(variance);

        List<Map<String, String>> anomalies = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            double value = cpuValues.get(i);
            double zScore = (value - mean) / stdDev;

            // Threshold: If Z-score > 2, it's an anomaly (statistically significant
            // deviation)
            if (Math.abs(zScore) > 2.0) {
                Map<String, String> anomaly = new HashMap<>();
                anomaly.put("timestamp", lines.get(i)[0]);
                anomaly.put("cpu_usage", String.valueOf(value));
                anomaly.put("reason", "Z-Score: " + String.format("%.2f", zScore));
                anomalies.add(anomaly);
            }
        }

        System.out.println("Detected " + anomalies.size() + " anomalies.");
        return anomalies;
    }

    private static void generateReport(List<Map<String, String>> anomalies) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(anomalies);

        try (FileWriter writer = new FileWriter("java_anomaly_report.json")) {
            writer.write(json);
        }
        System.out.println("Report generated: java_anomaly_report.json");
        // In production: S3Client.putObject(...)
    }
}
