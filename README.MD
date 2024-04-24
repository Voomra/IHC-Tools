# IHС Tool

Инструмент командной строки для работы с IHC.ru

## DNS

Результат выполнения в формате JSON.

- login: `me@example.com`
- passwd: `s3cr3T`

### Получить список доменов

```shell
ihc-dns.sh --list --user 'me%40example.com' --password 's3cr3T'
```

```json
{
  "id": 123456,
  "domain": "example.com"
}
{
  "id": 554433,
  "domain": "some-domain.ru"
}
```

### Получить список DNS записей домена

```shell
ihc-dns.sh --domain 'example.com' --list --user 'me%40example.com' --password 's3cr3T'
```

```json
{
  "readOnly": true,
  "id": 1111111,
  "name": "",
  "type": "SOA",
  "content": "ns1.ihc.ru. info.ihc.ru. 9999999999 99999 9999 999999 9999",
  "prio": null
}
{
  "readOnly": true,
  "id": 2222222,
  "name": "",
  "type": "NS",
  "content": "ns1.ihc.ru",
  "prio": null
}
{
  "readOnly": true,
  "id": 3333333,
  "name": "",
  "type": "NS",
  "content": "ns2.ihc.ru",
  "prio": null
}
{
  "readOnly": false,
  "id": 4444444,
  "name": "_some_txt_name",
  "type": "TXT",
  "content": "SomeTxtContent",
  "prio": null
}
```

### Добавить DNS запись

```shell
ihc-dns.sh --domain 'example.com' --add --name '_acme-challenge' --type 'TXT' --content 'SomeTxtContent' --user 'me%40example.com' --password 's3cr3T'
```

```json
{
  "alert": {
    "type": "success",
    "message": "<span class='alert__msg'>Операция выполнена успешно</span>"
  },
  "data": {
    "success": true
  }
}
```

### Редактирование DNS записи

Перед редактированием, нужно узнать `id`. Для примера возьмём `id` из примера получения списка DNS записей.

```shell
ihc-dns.sh --domain 'example.com' --edit --record '4444444' --type 'TXT' --content 'SomeTxtContent' --user 'me%40example.com' --password 's3cr3T'
```

```json
{
  "alert": {
    "type": "success",
    "message": "<span class='alert__msg'>Операция выполнена успешно</span>"
  },
  "data": {
    "success": true
  }
}
```

### Удалить DNS записи

Перед редактированием, нужно узнать `id`. Для примера возьмём `id` из примера получения списка DNS записей.

```shell
ihc-dns.sh --domain 'example.com' --delete --record '4444444' --user 'me%40example.com' --password 's3cr3T'
```

```json
{
  "alert": {
    "type": "success",
    "message": "<span class='alert__msg'>запись удалена</span>"
  },
  "data": {
    "success": true
  }
}
```
