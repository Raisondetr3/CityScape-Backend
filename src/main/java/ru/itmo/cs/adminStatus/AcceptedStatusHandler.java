package ru.itmo.cs.adminStatus;

import org.springframework.stereotype.Component;

@Component("ACCEPTED")
public class AcceptedStatusHandler implements AdminRequestStatusHandler {
    @Override
    public String getStatusMessage() {
        return "Ваша заявка одобрена. Вы теперь администратор.";
    }
}
