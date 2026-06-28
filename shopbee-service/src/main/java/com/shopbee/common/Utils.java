package com.shopbee.common;

import jakarta.ws.rs.core.Response;

/**
 * The type Utils.
 */
public final class Utils {

    public static final String LOCATION_HEADER = "location";

    private Utils() {
    }

    /**
     * Gets resource id from response. ex: Location: http://localhost:8080/api/resource/12345 -> returns 12345
     *
     * @param response the response
     * @return the resource id from response, or null if not found
     */
    public static String getResourceIdFromResponse(Response response) {
        if (response == null) {
            return null;
        }

        String headerString = response.getHeaderString(LOCATION_HEADER);
        if (headerString == null) {
            return null;
        }

        String[] locations = headerString.split("/");
        return locations[locations.length - 1];
    }
}
