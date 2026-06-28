# Stage 1: Build & Optimize
FROM quay.io/keycloak/keycloak:26.5.4 AS builder

# Set build-time options
ENV KC_DB=postgres
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

# Copy custom themes
COPY themes/shopbee /opt/keycloak/themes/shopbee

# Optimize build
RUN /opt/keycloak/bin/kc.sh build

# Stage 2: Final Runtime Image
FROM quay.io/keycloak/keycloak:26.5.4
COPY --from=builder /opt/keycloak/ /opt/keycloak/

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
