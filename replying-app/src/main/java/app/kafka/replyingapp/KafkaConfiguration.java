package app.kafka.replyingapp;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.SimpleKafkaHeaderMapper;
import org.springframework.kafka.support.converter.MessagingMessageConverter;

import app.kafka.replyingapp.voucher.Voucher;
import app.kafka.replyingapp.voucher.VoucherRequest;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@EnableKafka
public class KafkaConfiguration {
    @Autowired
    private ApplicationProperties applicationProperties;

    // Topic creation.
    @Bean
    public NewTopic vouchersRequest() {
        return new NewTopic(applicationProperties.vouchersRequestTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic vouchers() {
        return new NewTopic(applicationProperties.vouchersTopic, 1, (short) 1);
    }

    @Bean
    public KafkaTemplate<Integer, VoucherRequest> kafkaRequestTemplate(
            ProducerFactory<Integer, VoucherRequest> producerFactory) {
        log.info("Creating KafkaTemplate");

        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<Integer, Voucher> kafkaTemplate(ProducerFactory<Integer, Voucher> producerFactory) {
        log.info("Creating KafkaTemplate");

        return new KafkaTemplate<>(producerFactory);
    }

    // Default ReplyingKafkaTemplate used by VoucherService.
    @Bean
    public ReplyingKafkaTemplate<Integer, VoucherRequest, Voucher> replyingTemplate(
            ProducerFactory<Integer, VoucherRequest> pf,
            ConcurrentMessageListenerContainer<Integer, Voucher> replyContainer) {

        log.info("Creating ReplyingKafkaTemplate");

        var replyingTemplate = new ReplyingKafkaTemplate<>(pf, replyContainer);
        replyingTemplate.setSharedReplyTopic(true);
        return replyingTemplate;
    }

    @Bean
    public ConcurrentMessageListenerContainer<Integer, Voucher> replyContainer(
            ConcurrentKafkaListenerContainerFactory<Integer, Voucher> containerFactory) {

        ConcurrentMessageListenerContainer<Integer, Voucher> replyContainer = containerFactory
                .createContainer(applicationProperties.vouchersTopic);
        replyContainer.getContainerProperties().setGroupId(applicationProperties.vouchersConsumerGroupId);
        replyContainer.setAutoStartup(false);

        return replyContainer;
    }

    @Bean // not required if Jackson is on the classpath
    public MessagingMessageConverter simpleMapperConverter() {
        MessagingMessageConverter messagingMessageConverter = new MessagingMessageConverter();
        messagingMessageConverter.setHeaderMapper(new SimpleKafkaHeaderMapper());
        return messagingMessageConverter;
    }
}
