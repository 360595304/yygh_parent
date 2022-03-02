package com.hu.yygh.hosp.receiver;

import com.hu.yygh.common.rabbit.constant.MqConst;
import com.hu.yygh.common.rabbit.service.RabbitService;
import com.hu.yygh.hosp.service.ScheduleService;
import com.hu.yygh.model.hosp.Schedule;
import com.hu.yygh.vo.msm.MsmVo;
import com.hu.yygh.vo.order.OrderMqVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author suhu
 * @createDate 2022/3/2
 */
@Component
public class HospitalReceiver {

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private ScheduleService scheduleService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = MqConst.ROUTING_ORDER
    ))
    public void receiver(OrderMqVo orderMqVo, Message message, Channel channel) throws IOException {
        Schedule schedule = scheduleService.getScheduleById(orderMqVo.getScheduleId());
        schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
        schedule.setReservedNumber(orderMqVo.getReservedNumber());

        scheduleService.update(schedule);

        MsmVo msmVo = orderMqVo.getMsmVo();
        if (null != msmVo) {
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }
}
