import pandas as pd
import numpy as np
from sklearn.ensemble import IsolationForest
import boto3
import json
import os
from datetime import datetime

# AWS Configuration (usually handled by IAM Role on EC2)
S3_BUCKET = "your-log-bucket-name"
LOG_FILE_PATH = "logs/access_logs.csv"
REPORT_OUTPUT_PATH = "reports/anomaly_report.json"

def download_logs_from_s3():
    """Downloads the log file from S3."""
    print("Downloading logs from S3...")
    s3 = boto3.client('s3')
    try:
        s3.download_file(S3_BUCKET, LOG_FILE_PATH, 'local_logs.csv')
        return True
    except Exception as e:
        print(f"Error downloading logs: {e}")
        return False

def detect_anomalies(data_path):
    """Uses Isolation Forest to detect anomalies in logs."""
    print("Analyzing logs for anomalies...")
    
    # Load sample log data
    # Expected columns: timestamp, cpu_usage, memory_usage, failed_login_count
    df = pd.read_csv(data_path)
    
    # Preprocessing: Convert timestamp to unix if necessary
    # For simplicity, we assume we're analyzing numerical metrics
    features = df[['cpu_usage', 'memory_usage', 'failed_login_count']]
    
    # Initialize Isolation Forest
    # contamination is the expected proportion of outliers (anomalies)
    model = IsolationForest(contamination=0.05, random_state=42)
    
    # Fit and predict
    df['anomaly_score'] = model.fit_predict(features)
    
    # Isolation Forest returns -1 for anomalies and 1 for normal data
    df['is_anomaly'] = df['anomaly_score'].apply(lambda x: True if x == -1 else False)
    
    anomalies = df[df['is_anomaly'] == True]
    
    print(f"Detected {len(anomalies)} anomalies out of {len(df)} records.")
    return anomalies

def upload_report_to_s3(anomalies):
    """Uploads the anomaly report back to S3."""
    print("Uploading report to S3...")
    report_data = {
        "timestamp": datetime.now().isoformat(),
        "total_anomalies": len(anomalies),
        "anomalies": anomalies.to_dict(orient='records')
    }
    
    with open('anomaly_report.json', 'w') as f:
        json.dump(report_data, f, indent=4)
        
    s3 = boto3.client('s3')
    try:
        s3.upload_file('anomaly_report.json', S3_BUCKET, REPORT_OUTPUT_PATH)
        print("Report successfully uploaded to S3.")
    except Exception as e:
        print(f"Error uploading report: {e}")

if __name__ == "__main__":
    if download_logs_from_s3():
        detected = detect_anomalies('local_logs.csv')
        if not detected.empty:
            upload_report_to_s3(detected)
        else:
            print("No anomalies detected.")
