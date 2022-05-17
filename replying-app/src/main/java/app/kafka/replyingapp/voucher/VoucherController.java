package app.kafka.replyingapp.voucher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class VoucherController {
    @Autowired
    VoucherProcessor voucherProcessor;

    @PostMapping(value = "/api/vouchers")
    public Voucher issueVoucher(@RequestBody VoucherRequest voucherRequest) {
        log.info("Received POST request for issuing vouchers");
        return voucherProcessor.issueVoucher(voucherRequest);
    }
}
