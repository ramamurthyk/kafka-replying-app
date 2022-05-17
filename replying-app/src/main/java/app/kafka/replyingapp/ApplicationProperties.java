package app.kafka.replyingapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationProperties {
    @Value("${app.topic.vouchers.request}")
    public String vouchersRequestTopic;

    @Value("${app.topic.vouchers}")
    public String vouchersTopic;

    @Value("${app.voucher.request-reply.timeout}")
    public long requestReplyTimeout;

    @Value("${app.kafka.vouchers.consumer.group-id}")
    public String vouchersConsumerGroupId;
}
