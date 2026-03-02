#!/bin/bash

# Portability script to build and run the Java Anomaly Detector
# Suitable for EC2 UserData or Cron Job

echo "------------------------------------------"
echo "AI Log Anomaly Detection Orchestrator (Bash)"
echo "------------------------------------------"

# 1. Sync logs from S3
echo "[Step 1] Syncing logs from S3..."
# aws s3 cp s3://your-log-bucket-name/logs/access_logs.csv ./local_logs.csv

# 2. Build Java Application (if needed)
if [ ! -f "java-detector/target/log-anomaly-detector-1.0-SNAPSHOT.jar" ]; then
    echo "[Step 2] Building Java Detector with Maven..."
    cd java-detector && mvn clean package -DskipTests && cd ..
fi

# 3. Run Detection
echo "[Step 3] Running Java Anomaly Detector..."
java -jar java-detector/target/log-anomaly-detector-1.0-SNAPSHOT.jar

# 4. Upload results back to S3
if [ -f "java_anomaly_report.json" ]; then
    echo "[Step 4] Uploading results to S3..."
    # aws s3 cp java_anomaly_report.json s3://your-log-bucket-name/reports/
    echo "Done."
else
    echo "No report generated."
fi
