#!/usr/bin/env bash

if [ ! -d ${HOME}/google-cloud-sdk ]
then
    curl https://sdk.cloud.google.com | bash
fi

openssl aes-256-cbc \
    -K ${encrypted_a6a69c5b1756_key} \
    -iv ${encrypted_a6a69c5b1756_iv} \
    -in ${GCP_CREDENTIALS_FILE}.enc \
    -out ${GCP_CREDENTIALS_FILE} \
    -d

gcloud auth activate-service-account --key-file ${GCP_CREDENTIALS_FILE}
