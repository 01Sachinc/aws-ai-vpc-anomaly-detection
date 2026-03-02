#!/bin/bash

# Configuration
S3_BUCKET="your-log-bucket-name"
LOCAL_LOG_DIR="/var/log/app_logs"
REMOTE_LOG_DIR="s3://$S3_BUCKET/logs/"

# Create local log directory if it doesn't exist
mkdir -p $LOCAL_LOG_DIR

# Sync logs from EC2 to S3
echo "Syncing logs to S3..."
aws s3 sync $LOCAL_LOG_DIR $REMOTE_LOG_DIR --delete

# Verification
if [ $? -eq 0 ]; then
    echo "Logs successfully backed up to S3."
else
    echo "Error backing up logs to S3." >&2
    exit 1
fi
