package com.tcu.projectpulse.auth.service;

public record InvitationDeliveryAttempt(boolean delivered, String detail) {

    public static InvitationDeliveryAttempt delivered(String detail) {
        return new InvitationDeliveryAttempt(true, detail);
    }

    public static InvitationDeliveryAttempt failed(String detail) {
        return new InvitationDeliveryAttempt(false, detail);
    }
}
