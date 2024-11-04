package ru.itmo.cs.adminStatus;

import org.springframework.stereotype.Component;

@Component("NONE")
public class NoneStatusHandler implements AdminRequestStatusHandler {
    @Override
    public String getStatusMessage() {
        return "Вы еще не отправили запрос на администратора.";
    }
}
