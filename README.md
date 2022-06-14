# Kafka Replying App
## Introduction
Request-Response Pattern implementation using Kafka.

![Image](docs/overview.png)

## Demo

Send request to API controller.

```Bash
POST http://localhost:8080/api/vouchers
```

```JSON
{
    "customerId": 1234,
    "programme": "Mindfullness"
}
```

Request will be sent to vouchers.request topic.

```JSON
Record headers:
[
  {
    "key": "x_messageId",
    "stringValue": "8c590623-c496-4d1b-a819-e57d97f0e1cf"
  },
  {
    "key": "kafka_replyTopic",
    "stringValue": "vouchers"
  },
  {
    "key": "kafka_correlationId",
    "stringValue": "�a��ʤF�����7&�+"
  },
  {
    "key": "__TypeId__",
    "stringValue": "app.kafka.replyingapp.voucher.VoucherRequest"
  }
]

Data:
{
  "customerId": 1234,
  "programme": "Mindfullness"
}
```

On processor receiving request, it will process and generate a voucher and produces the message to the vouchers topic.

```JSON
Record headers:
[
  {
    "key": "kafka_correlationId",
    "stringValue": "�a��ʤF�����7&�+"
  },
  {
    "key": "kafka_replyTopic",
    "stringValue": "vouchers"
  },
  {
    "key": "__TypeId__",
    "stringValue": "app.kafka.replyingapp.voucher.Voucher"
  }
]

Data:
{
  "customerId": 1234,
  "programme": "Mindfullness",
  "voucherCode": "Mindfullness--1223506041"
}
```

API controller responds with the generated voucher.

```JSON
{
    "customerId": 1234,
    "programme": "Mindfullness",
    "voucherCode": "Mindfullness--1223506041"
}
```
