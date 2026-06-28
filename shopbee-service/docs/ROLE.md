| Permission Category | Action                   | Admin | Store Owner | Manager | Staff | Customer  | System Service |
|---------------------|--------------------------|:-----:|:-----------:|:-------:|:-----:|:---------:|:--------------:|
| **Auth & Identity** | View users               |   ✓   |      ✓      |    ✗    |   ✗   |     ✗     |       ✗        |
|                     | Manage users             |   ✓   |      ✗      |    ✗    |   ✗   |     ✗     |       ✗        |
|                     | Manage store staff       |   ✓   |      ✓      |    ✓    |   ✗   |     ✗     |       ✗        |
| **Products**        | Create products          |   ✓   |      ✓      |    ✓    |   ✓   |     ✗     |       ✗        |
|                     | Edit products            |   ✓   |      ✓      |    ✓    |   ✓   |     ✗     |       ✗        |
|                     | Delete products          |   ✓   |      ✓      |    ✓    |   ✗   |     ✗     |       ✗        |
|                     | View products            |   ✓   |      ✓      |    ✓    |   ✓   |     ✓     |       ✓        |
| **Inventory**       | Adjust stock             |   ✓   |      ✓      |    ✓    |   ✓   |     ✗     |       ✗        |
|                     | View stock               |   ✓   |      ✓      |    ✓    |   ✓   |     ✗     |       ✗        |
| **Orders**          | View orders              |   ✓   |      ✓      |    ✓    |   ✓   |    Own    |       ✗        |
|                     | Update order status      |   ✓   |      ✓      |    ✓    |   ✓   |     ✗     |       ✗        |
|                     | Cancel order             |   ✓   |      ✓      |    ✓    |   ✓   |    Own    |       ✗        |
| **Payments**        | View payments            |   ✓   |      ✓      |    ✓    |   ✗   |     ✗     |       ✗        |
|                     | Issue refunds            |   ✓   |      ✓      |    ✓    |   ✗   |     ✗     |       ✗        |
| **Store Settings**  | Update store settings    |   ✓   |      ✓      |    ✗    |   ✗   |     ✗     |       ✗        |
|                     | Manage integrations      |   ✓   |      ✓      |    ✗    |   ✗   |     ✗     |       ✓        |
| **Multi-tenancy**   | Access tenant config     |   ✓   |      ✓      |    ✗    |   ✗   |     ✗     |       ✓        |
|                     | Manage API keys          |   ✓   |      ✓      |    ✗    |   ✗   |     ✗     |       ✓        |
| **System**          | Service-to-service calls |   ✗   |      ✗      |    ✗    |   ✗   |     ✗     |       ✓        |

### Admin
Platform-level administrator with full access to all resources across all tenants.

### Store Owner
Owns one store; full control of store configuration, staff, and products.

### Manager
Handles operations: products, orders, and refunds.

### Staff
Daily operations: process orders, manage products (except delete).

### Customer
End-user with permissions limited to their own orders.

### System Service
Internal microservices that require service-to-service authentication.

- USER_VIEW
- USER_MANAGE
- STAFF_MANAGE
- PRODUCT_CREATE
- PRODUCT_EDIT
- PRODUCT_DELETE
- PRODUCT_VIEW
- STOCK_ADJUST
- STOCK_VIEW
- ORDER_VIEW
- ORDER_UPDATE
- ORDER_CANCEL_OWN
- PAYMENT_VIEW
- PAYMENT_REFUND
- STORE_CONFIG_UPDATE
- INTEGRATION_MANAGE
- TENANT_CONFIG_ACCESS
- API_KEY_MANAGE
- INTERNAL_SERVICE_CALL
