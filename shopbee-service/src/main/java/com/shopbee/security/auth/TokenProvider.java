package com.shopbee.security.auth;

public interface TokenProvider {

    String getApplicationAdministrationToken();

    String getBusinessAdministrationToken();

    String getApplicationAdministrationToken(String tenantId);

    String getBusinessAdministrationToken(String tenantId);
}
