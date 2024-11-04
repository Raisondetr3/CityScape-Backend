package ru.itmo.cs.adminStatus;

import org.springframework.stereotype.Component;

@Component("PENDING")
public class PendingStatusHandler implements AdminRequestStatusHandler {
    @Override
    public String getStatusMessage() {
        return "Ваша заявка на администратора находится в ожидании.";
    }
}
