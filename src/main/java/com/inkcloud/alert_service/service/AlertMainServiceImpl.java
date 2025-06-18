package com.inkcloud.alert_service.service;

import java.time.format.DateTimeFormatter;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkcloud.alert_service.dto.OrderDto;
import com.inkcloud.alert_service.dto.OrderItemDto;
import com.inkcloud.alert_service.enums.OrderState;
import com.inkcloud.alert_service.enums.PaymentMethod;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertMainServiceImpl implements AlertMainService {
    private final JavaMailSender sender;
    private final KafkaTemplate<String, Object> kafkaTemplate; // 토픽 이름, event객체

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-complete-alert", groupId = "order_group")
    @Transactional
    public void orderSendMail(String event) throws Exception {
        log.info("Kafka Consumer : Order-Service, receive event : {}", event);
        OrderDto orderDto = objectMapper.readValue(event, OrderDto.class);
        log.info(" ============= dto : {}", orderDto);

        // 주문 완료 메일 발송
        sendOrderCompleteEmail(orderDto);
    }

    public void sendOrderCompleteEmail(OrderDto orderDto) {
        MimeMessage mimeMessage = sender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            mimeMessageHelper.setFrom("inkcloud@gmail.com");
            mimeMessageHelper.setTo(orderDto.getMember().getMemberEmail());

            mimeMessageHelper.setSubject("[잉크클라우드] 주문이 완료되었습니다 - 주문번호: " + orderDto.getId());

            StringBuilder itemsHtml = new StringBuilder();
            for (OrderItemDto item : orderDto.getOrderItems()) {
                itemsHtml.append(String.format("""
                        <tr>
                            <td style="padding: 10px; border-bottom: 1px solid #eee;">%s</td>
                            <td style="padding: 10px; border-bottom: 1px solid #eee; text-align: center;">%s</td>
                            <td style="padding: 10px; border-bottom: 1px solid #eee; text-align: center;">%d개</td>
                            <td style="padding: 10px; border-bottom: 1px solid #eee; text-align: right;">%,d원</td>
                        </tr>
                        """, item.getName(), item.getAuthor(), item.getQuantity(), item.getPrice()));
            }

            // HTML 메일 내용
            String content = String.format(
                    """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta charset="UTF-8">
                                <style>
                                    body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
                                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                                    .header { text-align: center; margin-bottom: 30px; }
                                    .header h1 { color: #333; margin: 0; }
                                    .order-info { background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin-bottom: 20px; }
                                    .order-table { width: 100%%; border-collapse: collapse; margin-bottom: 20px; }
                                    .order-table th { background-color: #007bff; color: white; padding: 12px; text-align: left; }
                                    .total { text-align: right; font-size: 18px; font-weight: bold; color: #007bff; margin: 20px 0; }
                                    .shipping-info { background-color: #e9ecef; padding: 15px; border-radius: 5px; margin-top: 20px; }
                                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                                </style>
                            </head>
                            <body>
                                <div class="container">
                                    <div class="header">
                                        <h1>📚 잉크클라우드</h1>
                                        <h2>주문이 완료되었습니다!</h2>
                                    </div>

                                    <div class="order-info">
                                        <h3>주문 정보</h3>
                                        <p><strong>주문번호:</strong> %s</p>
                                        <p><strong>주문일시:</strong> %s</p>
                                        <p><strong>주문자:</strong> %s</p>
                                        <p><strong>결제방법:</strong> %s</p>
                                        <p><strong>주문상태:</strong> %s</p>
                                    </div>

                                    <h3>주문 상품</h3>
                                    <table class="order-table">
                                        <thead>
                                            <tr>
                                                <th>상품명</th>
                                                <th>저자</th>
                                                <th>수량</th>
                                                <th>가격</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            %s
                                        </tbody>
                                    </table>

                                    <div class="total">
                                        총 주문 금액: %,d원
                                    </div>

                                    <div class="shipping-info">
                                        <h3>배송 정보</h3>
                                        <p><strong>받는분:</strong> %s</p>
                                        <p><strong>연락처:</strong> %s</p>
                                        <p><strong>주소:</strong> [%d] %s %s</p>
                                    </div>

                                    <div class="footer">
                                        <p>주문해 주셔서 감사합니다!</p>
                                        <p>문의사항이 있으시면 고객센터로 연락해 주세요.</p>
                                        <hr>
                                        <p style="font-size: 12px; color: #999;">
                                            본 메일은 발신전용입니다. 문의사항은 고객센터를 이용해 주세요.
                                        </p>
                                    </div>
                                </div>
                            </body>
                            </html>
                            """,
                    orderDto.getId(), 
                    orderDto.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")),
                    orderDto.getMember().getMemberName(),
                    getPaymentMethodName(orderDto.getMethod()),
                    getOrderStateName(orderDto.getState()), 
                    itemsHtml.toString(), 
                    orderDto.getPrice(), 
                    orderDto.getOrderShip().getReceiver(),
                    orderDto.getOrderShip().getContact(), 
                    orderDto.getOrderShip().getZipcode(), 
                    orderDto.getOrderShip().getAddressMain(), 
                    orderDto.getOrderShip().getAddressSub() 
            );

            mimeMessageHelper.setText(content, true);

            sender.send(mimeMessage);

            log.info("주문 완료 메일 발송 성공! 주문번호: {}, 수신자: {}",
                    orderDto.getId(), orderDto.getMember().getMemberEmail());

        } catch (Exception e) {
            log.error("주문 완료 메일 발송 실패! 주문번호: {}, 에러: {}",
                    orderDto.getId(), e.getMessage(), e);
            throw new RuntimeException("메일 발송 실패", e);
        }
    }

    private String getPaymentMethodName(PaymentMethod method) {
        switch (method) {
            case CARD:
                return "신용카드";
            case KAKAOPAY:
                return "카카오페이";
            case NAVERPAY:
                return "네이버페이";
            default:
                return method.toString();
        }
    }

    // 주문 상태 한글명 변환
    private String getOrderStateName(OrderState state) {
        switch (state) {
            case PREPARE:
                return "주문접수";
            case SHIPPING:
                return "배송중";
            case SHIPPED:
                return "배송완료";
            case CANCELED:
                return "주문취소";
            default:
                return state.toString();
        }
    }
}
