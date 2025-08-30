/*
 * PhoneId.java
 *
 * Copyright by shopbee-user-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PhoneId implements Serializable {

    @Column(name = "country_code")
    private String countryCode;

    @Column
    private String number;

    public String getPhoneNumber() {
        return countryCode + number;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PhoneId phoneId = (PhoneId) o;
        return Objects.equals(countryCode, phoneId.countryCode) && Objects.equals(number, phoneId.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryCode, number);
    }
}
