Design and implement a multi-tenant Admin UI for Shopbee.

Requirements:
- Support 3 roles: PLATFORM_ADMIN, STORE_OWNER, STORE_SUPERVISOR
- Multi-tenant isolation: each tenant has its own data scope
- UI must dynamically adapt based on role + tenant context
- Backend is Quarkus-based microservices with REST APIs
- Authentication via JWT (Keycloak or equivalent)

Deliver:
1. Frontend architecture (module structure, routing, layout)
2. Role-based access control (RBAC) strategy
3. API integration pattern (interceptors, token handling, error handling)
4. Multi-tenant context handling (tenantId propagation)
5. UI component breakdown per feature
6. Tech stack (Nuxt 4 + Pinia + PrimeVue + Tailwindcss)
7. Production-ready best practices