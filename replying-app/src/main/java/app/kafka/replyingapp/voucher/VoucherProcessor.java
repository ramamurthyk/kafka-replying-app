package app.kafka.replyingapp.voucher;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import app.kafka.replyingapp.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VoucherProcessor {
    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private KafkaTemplate<Integer, Voucher> template;

    @Autowired
    private ReplyingKafkaTemplate<Integer, VoucherRequest, Voucher> replyingTemplate;

    public Voucher issueVoucher(VoucherRequest voucherRequest) {
        try {
            ProducerRecord<Integer, VoucherRequest> producerRecord = new ProducerRecord<Integer, VoucherRequest>(
                    applicationProperties.vouchersRequestTopic, voucherRequest.customerId(),
                    voucherRequest);
            // Add headers.
            producerRecord.headers().add(new RecordHeader("x_messageId",
                    UUID.randomUUID().toString().getBytes()));

            RequestReplyFuture<Integer, VoucherRequest, Voucher> replyFuture = replyingTemplate
                    .sendAndReceive(producerRecord);

            var sendResult = replyFuture.getSendFuture().get(applicationProperties.requestReplyTimeout,
                    TimeUnit.MILLISECONDS);
            log.info("Voucher Issue Sent ok: " + sendResult.getRecordMetadata());

            // Wait for the response.

            // Recieve response.
            ConsumerRecord<Integer, Voucher> consumerRecord = replyFuture.get(
                    applicationProperties.requestReplyTimeout,
                    TimeUnit.MILLISECONDS);
            Voucher voucher = consumerRecord.value();
            log.info("Voucher Response Received: " + voucher);

            return voucher;

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            log.error("Voucher issue request failed", e.getMessage());
        }

        return null;
    }

    @KafkaListener(id = "${app.kafka.vouchers.consumer.id}", topics = "${app.topic.vouchers.request}")
    // @SendTo
    public ProducerRecord<Integer, Voucher> processVoucherRequest(
            ConsumerRecord<Integer, VoucherRequest> consumerRecord,
            @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId,
            @Header(KafkaHeaders.REPLY_TOPIC) byte[] replyTopic
    // @Header(KafkaHeaders.REPLY_PARTITION) byte[] replyPartition
    ) {
        VoucherRequest voucherRequest = consumerRecord.value();
        log.info("Received request for synchronous voucher issue: " +
                voucherRequest);

        String voucherCode = voucherRequest.programme() + "-" + new Random().nextInt();

        Voucher response = new Voucher(voucherRequest.customerId(),
                voucherRequest.programme(), voucherCode);
        ProducerRecord<Integer, Voucher> producerRecord = new ProducerRecord<Integer, Voucher>(
                applicationProperties.vouchersTopic, response.customerId(), response);

        producerRecord.headers().add(new RecordHeader(KafkaHeaders.CORRELATION_ID, correlationId));
        producerRecord.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, replyTopic));
        // producerRecord.headers().add(new RecordHeader(KafkaHeaders.REPLY_PARTITION,
        // replyPartition));

        template.send(producerRecord);

        return producerRecord;
    }
}
