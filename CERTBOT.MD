Данный инструмент будет полезен для автоматизации продления сертификатов **Let's Encrypt** (certbot).

Для парсинга json ответов используется инструмент [jq](https://jqlang.github.io/jq/).

## certbot_auto.sh

```bash
#!/bin/bash

certbot certonly \
    --manual \
    --preferred-challenges=dns \
    --manual-auth-hook /path/to/authenticator.sh \
    --manual-cleanup-hook /path/to/cleanup.sh \
    -d 'example.com, *.example.com'
```

## authenticator.sh

```bash
#!/bin/bash

bash /path/to/ihc-dns.sh \
    --user 'me%40example.com' \
    --password 's3cr3T' \
    --domain 'example.com' \
    --add --name '_acme-challenge' --type 'TXT' --content "$CERTBOT_VALIDATION"
```

## cleanup.sh

```bash
#!/bin/bash

V_RECORD=$(bash /path/to/ihc-dns.sh \
    --user 'me%40example.com' \
    --password 's3cr3T' \
    --domain 'example.com' \
    --list | jq -r '. | select(.name == "_acme-challenge") | .id')

bash /path/to/ihc-dns.sh \
    --user 'me%40example.com' \
    --password 's3cr3T' \
    --domain 'example.com' \
    --delete --record $V_RECORD
```
