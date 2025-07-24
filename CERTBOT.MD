Данный инструмент будет полезен для автоматизации продления сертификатов **Let's Encrypt** (certbot).

## certbot_auto.sh

```bash
#!/bin/bash

certbot certonly \
    --manual \
    --preferred-challenges=dns \
    --manual-auth-hook /path/to/authenticator.sh \
    --manual-cleanup-hook /path/to/cleanup.sh \
    -d 'example-1.ru, *.example-1.ru'
```

## authenticator.sh

```bash
#!/bin/bash

bash /path/to/ihc-dns domain-record add \
    "example-1.ru" \
    --name="_acme-challenge" \
    --type="TXT" \
    --content="$CERTBOT_VALIDATION"
    --user='me@example.com' --password='s3cr3T'
```

## cleanup.sh

```bash
#!/bin/bash

bash /path/to/ihc-dns domain-record delete \
    "example-1.ru" \
    "_acme-challenge" \
    --user='me@example.com' --password='s3cr3T'
```
