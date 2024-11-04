package ru.itmo.cs.adminStatus;

import org.springframework.stereotype.Component;

@Component("REJECTED")
public class RejectedStatusHandler implements AdminRequestStatusHandler {
    @Override
    public String getStatusMessage() {
        return "Ваша заявка отклонена.";
    }
}
