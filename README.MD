# IHС DNS Tools

Инструмент командной строки для работы с DNS от [IHC.ru](https://ihc.ru).

## Получить список доменов

```shell
ihc-dns domains --user='me@example.com' --password='s3cr3T'
```

```
 ID     | Домен        | Punycode
--------|--------------|--------------------------
 111111 | example-1.ru |
 222222 | example-2.ru |
 333333 | пример-3.рф  | xn---3-mlcluqhd.xn--p1ai
```

## Получить список DNS записей домена

```shell
ihc-dns domain-record list 'example-1.ru' --user='me@example.com' --password='s3cr3T'
```

```
 ID       | Запись              | Тип   | Приоритет | Контент                                                     | Только для чтения
----------|---------------------|-------|-----------|-------------------------------------------------------------|-------------------
 7000001  |                     | SOA   |           | ns1.ihc.ru. info.ihc.ru. 2014120801 10800 3600 604800 3600  | да
 7000002  |                     | NS    |           | ns1.ihc.ru                                                  | да
 7000003  |                     | NS    |           | ns2.ihc.ru                                                  | да
 7000004  | yamail-XXXXXXXXXXXX | CNAME |           | mail.yandex.ru                                              | нет
 7000005  |                     | MX    | 10        | mx.yandex.ru                                                | нет
 7000006  |                     | A     |           | 127.0.0.1                                                   | нет
 7000007  | _acme-challenge     | TXT   |           | XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX                 | нет
```

## Добавить DNS запись

```shell
ihc-dns domain-record add 'example-1.ru' --name='some-name' --type='TXT' --content='some-content' --user='me@example.com' --password='s3cr3T'
```

## Редактирование DNS записи

```shell
ihc-dns domain-record edit 'example-1.ru' 'some-name' --content='some-content-2' --user='me@example.com' --password='s3cr3T'
```

## Удалить DNS записи

```shell
ihc-dns domain-record delete 'example-1.com' 'some-name' --user='me@example.com' --password='s3cr3T'
```
