#!/bin/sh

curl -X PUT http://localhost:9200/analytics
curl -X PUT http://localhost:9200/healthstats

cat <<EOF | curl -X PUT -H "content-type: application/json" http://localhost:9200/healthstats/_mapping -d @-
            {
                "properties": {
                    "installationUuid": { "type": "text" },
                    "type": { "type": "integer" },
                    "secondsTracingActivated": { "type": "long" },
                    "riskLevel": { "type": "float" },
                    "dateSample": { "type": "text" },
                    "dateFirstSymptoms": { "type": "text" },
                    "dateLastContactNotification": { "type": "text" }
                }
            }
EOF
