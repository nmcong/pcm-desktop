# Oracle Database Function Calling Integration Guide

## Tổng quan
Tài liệu này hướng dẫn tích hợp Oracle Database vào PCM Desktop Application như một data source bổ sung, tạo các LLM Function Calling để hỗ trợ khách hàng truy xuất dữ liệu Oracle dễ dàng thông qua AI Assistant.

## Kiến trúc tích hợp

### Dual Database Architecture
```
┌─────────────────────────────────────────────────────────┐
│                PCM Desktop Application                  │
├─────────────────┬───────────────────┬───────────────────┤
│    UI Layer     │   Service Layer   │   Data Layer      │
│                 │                   │                   │
│ AIAssistantPage │ LLM Function      │ SQLite (App Data) │
│ ChatInterface   │ Calling System    │ - Conversations   │
│                 │                   │ - Projects        │
│                 │ Oracle Functions  │ - User Settings   │
│                 │ - CustomerQuery   │                   │
│                 │ - OrderQuery      │ Oracle (Customer) │
│                 │ - ProductQuery    │ - Customers       │
│                 │ - ReportQuery     │ - Orders          │
│                 │                   │ - Products        │
│                 │                   │ - Reports         │
└─────────────────┴───────────────────┴───────────────────┘
```

### Function Calling Flow
```
User Question → AI Assistant → Function Calling → Oracle Query → Results → AI Response
    ↓              ↓              ↓                ↓           ↓           ↓
"Tìm khách      Parse intent   call_oracle_     SELECT *     Customer    "Tôi tìm thấy
hàng Nguyễn"   → determine      customer_        FROM         data        5 khách hàng
                function       search()         customers                 tên Nguyễn..."
```

## 1. Oracle Connection Configuration

### 1.1 Oracle Connection Manager
Tạo `OracleConnectionManager.java` riêng biệt với SQLite:

```java
package com.noteflix.pcm.infrastructure.database.oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * Oracle Database Connection Manager
 * 
 * Quản lý kết nối đến Oracle database của khách hàng
 * Riêng biệt với SQLite database của application
 */
@Slf4j
public class OracleConnectionManager {
    
    private static OracleConnectionManager instance;
    
    // Oracle connection configuration
    private String host;
    private int port;
    private String serviceName;
    private String username;
    private String password;
    private Connection connection;
    
    private OracleConnectionManager() {}
    
    public static synchronized OracleConnectionManager getInstance() {
        if (instance == null) {
            instance = new OracleConnectionManager();
        }
        return instance;
    }
    
    /**
     * Initialize Oracle connection with customer database credentials
     */
    public void initialize(OracleConfig config) {
        this.host = config.getHost();
        this.port = config.getPort();
        this.serviceName = config.getServiceName();
        this.username = config.getUsername();
        this.password = config.getPassword();
        
        log.info("🔧 Oracle connection initialized for: {}:{}/{}", host, port, serviceName);
    }
    
    /**
     * Get Oracle database connection
     */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            establishConnection();
        }
        return connection;
    }
    
    private void establishConnection() throws SQLException {
        try {
            // Load Oracle JDBC driver
            Class.forName("oracle.jdbc.OracleDriver");
            
            // Build connection URL
            String url = String.format("jdbc:oracle:thin:@//%s:%d/%s", host, port, serviceName);
            
            // Set connection properties
            Properties props = new Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            props.setProperty("oracle.jdbc.ReadTimeout", "30000");
            props.setProperty("oracle.net.CONNECT_TIMEOUT", "10000");
            
            // Create connection
            connection = DriverManager.getConnection(url, props);
            
            // Test connection
            if (!connection.isValid(5)) {
                throw new SQLException("Invalid Oracle connection");
            }
            
            log.info("✅ Oracle database connected successfully");
            
        } catch (ClassNotFoundException e) {
            log.error("❌ Oracle JDBC driver not found", e);
            throw new SQLException("Oracle JDBC driver not available", e);
        } catch (SQLException e) {
            log.error("❌ Failed to connect to Oracle database", e);
            throw e;
        }
    }
    
    /**
     * Test Oracle connection
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (Exception e) {
            log.error("Oracle connection test failed", e);
            return false;
        }
    }
    
    /**
     * Execute a test query to verify Oracle accessibility
     */
    public boolean verifyDatabaseAccess() {
        try (Connection conn = getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT SYSDATE FROM DUAL")) {
            
            return rs.next();
            
        } catch (Exception e) {
            log.error("Oracle database access verification failed", e);
            return false;
        }
    }
    
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                log.info("✅ Oracle connection closed");
            } catch (SQLException e) {
                log.error("Error closing Oracle connection", e);
            } finally {
                connection = null;
            }
        }
    }
}

/**
 * Oracle configuration class
 */
@Data
@Builder
public class OracleConfig {
    private String host;
    private int port;
    private String serviceName;
    private String username;
    private String password;
    private String schema; // Optional: specific schema to query
}
```

### 1.2 Configuration Loading
```java
package com.noteflix.pcm.infrastructure.database.oracle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OracleConfigLoader {
    
    private static final String CONFIG_FILE = "oracle-database.properties";
    
    public static OracleConfig loadConfiguration() {
        Properties props = new Properties();
        
        try (InputStream is = OracleConfigLoader.class.getResourceAsStream("/" + CONFIG_FILE)) {
            if (is == null) {
                throw new RuntimeException("Oracle configuration file not found: " + CONFIG_FILE);
            }
            
            props.load(is);
            
            // Override with environment variables for production
            overrideWithEnvironmentVariables(props);
            
            return OracleConfig.builder()
                .host(props.getProperty("oracle.host"))
                .port(Integer.parseInt(props.getProperty("oracle.port", "1521")))
                .serviceName(props.getProperty("oracle.service"))
                .username(props.getProperty("oracle.username"))
                .password(props.getProperty("oracle.password"))
                .schema(props.getProperty("oracle.schema"))
                .build();
                
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Oracle configuration", e);
        }
    }
    
    private static void overrideWithEnvironmentVariables(Properties props) {
        String host = System.getenv("ORACLE_HOST");
        if (host != null) props.setProperty("oracle.host", host);
        
        String port = System.getenv("ORACLE_PORT");
        if (port != null) props.setProperty("oracle.port", port);
        
        String service = System.getenv("ORACLE_SERVICE");
        if (service != null) props.setProperty("oracle.service", service);
        
        String username = System.getenv("ORACLE_USERNAME");
        if (username != null) props.setProperty("oracle.username", username);
        
        String password = System.getenv("ORACLE_PASSWORD");
        if (password != null) props.setProperty("oracle.password", password);
    }
}
```

### 1.3 Configuration File
Tạo `src/main/resources/oracle-database.properties`:

```properties
# Oracle Database Configuration for Customer Data
# This connects to customer's existing Oracle database

# Connection settings
oracle.host=localhost
oracle.port=1521
oracle.service=CUSTOMER_DB
oracle.username=app_user
oracle.password=app_password

# Optional: specific schema
oracle.schema=CUSTOMER_SCHEMA

# Connection pool settings (if needed)
oracle.connection.timeout=30000
oracle.read.timeout=60000

# Query settings
oracle.default.fetch.size=100
oracle.max.results=1000

# Security settings
oracle.ssl.enabled=false
oracle.ssl.truststore.path=
oracle.ssl.truststore.password=
```

## 2. Oracle Function Calling Framework

### 2.1 Base Oracle Function Class
```java
package com.noteflix.pcm.functions.oracle;

import com.noteflix.pcm.infrastructure.database.oracle.OracleConnectionManager;
import java.sql.*;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for Oracle database functions
 * 
 * Cung cấp common functionality cho các Oracle query functions
 */
@Slf4j
public abstract class BaseOracleFunction {
    
    protected final OracleConnectionManager oracleManager;
    
    public BaseOracleFunction() {
        this.oracleManager = OracleConnectionManager.getInstance();
    }
    
    /**
     * Execute Oracle query and return results as List of Maps
     */
    protected List<Map<String, Object>> executeQuery(String sql, Object... parameters) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = oracleManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Set parameters
            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }
            
            log.debug("🔍 Executing Oracle query: {}", sql);
            log.debug("📋 Parameters: {}", Arrays.toString(parameters));
            
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    
                    results.add(row);
                }
            }
            
            log.info("✅ Oracle query completed: {} rows returned", results.size());
            
        } catch (SQLException e) {
            log.error("❌ Oracle query failed: {}", sql, e);
            throw new RuntimeException("Database query failed: " + e.getMessage(), e);
        }
        
        return results;
    }
    
    /**
     * Execute Oracle query and return single result
     */
    protected Map<String, Object> executeQuerySingle(String sql, Object... parameters) {
        List<Map<String, Object>> results = executeQuery(sql, parameters);
        return results.isEmpty() ? null : results.get(0);
    }
    
    /**
     * Execute Oracle query and return count
     */
    protected long executeCount(String sql, Object... parameters) {
        try (Connection conn = oracleManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Set parameters
            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
            
        } catch (SQLException e) {
            log.error("❌ Oracle count query failed: {}", sql, e);
            throw new RuntimeException("Count query failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build LIKE pattern for text search
     */
    protected String buildLikePattern(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return "%";
        }
        return "%" + searchText.trim().replace("'", "''") + "%";
    }
    
    /**
     * Sanitize input to prevent SQL injection
     */
    protected String sanitizeInput(String input) {
        if (input == null) return null;
        return input.replace("'", "''").replace("\"", "\"\"");
    }
    
    /**
     * Build pagination LIMIT clause for Oracle
     */
    protected String buildPaginationClause(Integer limit, Integer offset) {
        StringBuilder sb = new StringBuilder();
        
        if (offset != null && offset > 0) {
            sb.append(" OFFSET ").append(offset).append(" ROWS");
        }
        
        if (limit != null && limit > 0) {
            sb.append(" FETCH NEXT ").append(limit).append(" ROWS ONLY");
        }
        
        return sb.toString();
    }
    
    /**
     * Convert Oracle data types to Java-friendly formats
     */
    protected Object convertOracleValue(Object value) {
        if (value instanceof oracle.sql.TIMESTAMP) {
            oracle.sql.TIMESTAMP ts = (oracle.sql.TIMESTAMP) value;
            try {
                return ts.timestampValue();
            } catch (SQLException e) {
                return value.toString();
            }
        }
        
        if (value instanceof oracle.sql.DATE) {
            oracle.sql.DATE date = (oracle.sql.DATE) value;
            try {
                return date.dateValue();
            } catch (SQLException e) {
                return value.toString();
            }
        }
        
        return value;
    }
}
```

### 2.2 Customer Query Functions
```java
package com.noteflix.pcm.functions.oracle;

import com.noteflix.pcm.llm.annotation.FunctionProvider;
import com.noteflix.pcm.llm.annotation.LLMFunction;
import com.noteflix.pcm.llm.annotation.Param;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Oracle Customer Query Functions
 * 
 * Các function để truy vấn thông tin khách hàng từ Oracle database
 */
@FunctionProvider("Oracle Customer Functions")
@Slf4j
public class CustomerOracleFunction extends BaseOracleFunction {
    
    @LLMFunction(
        name = "oracle_search_customers",
        description = "Tìm kiếm khách hàng trong database Oracle theo tên, email, hoặc mã khách hàng. " +
                     "Hỗ trợ tìm kiếm mờ (fuzzy search) và có thể giới hạn số kết quả trả về.",
        categories = {"oracle", "customer", "search"}
    )
    public List<Map<String, Object>> searchCustomers(
            @Param(description = "Từ khóa tìm kiếm (tên, email, hoặc mã khách hàng)", required = false)
            String searchText,
            
            @Param(description = "Số lượng kết quả tối đa (mặc định 20)", defaultValue = "20")
            Integer limit,
            
            @Param(description = "Bỏ qua số record đầu (để phân trang)", defaultValue = "0")
            Integer offset
    ) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        sql.append("SELECT ")
           .append("    customer_id, ")
           .append("    customer_name, ")
           .append("    email, ")
           .append("    phone, ")
           .append("    address, ")
           .append("    city, ")
           .append("    registration_date, ")
           .append("    status ")
           .append("FROM customers ");
        
        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append("WHERE (")
               .append("    UPPER(customer_name) LIKE UPPER(?) ")
               .append("    OR UPPER(email) LIKE UPPER(?) ")
               .append("    OR customer_id = ? ")
               .append(") ");
            
            String likePattern = buildLikePattern(searchText);
            params.add(likePattern);
            params.add(likePattern);
            
            // Try to parse as customer ID
            try {
                params.add(Long.parseLong(searchText.trim()));
            } catch (NumberFormatException e) {
                params.add(-1L); // Invalid ID that won't match
            }
        }
        
        sql.append("ORDER BY customer_name ");
        sql.append(buildPaginationClause(limit, offset));
        
        return executeQuery(sql.toString(), params.toArray());
    }
    
    @LLMFunction(
        name = "oracle_get_customer_details",
        description = "Lấy thông tin chi tiết của một khách hàng cụ thể theo ID. " +
                     "Bao gồm thông tin cơ bản, địa chỉ, và thống kê đơn hàng.",
        categories = {"oracle", "customer", "details"}
    )
    public Map<String, Object> getCustomerDetails(
            @Param(description = "ID của khách hàng", required = true)
            Long customerId
    ) {
        String sql = """
            SELECT 
                c.customer_id,
                c.customer_name,
                c.email,
                c.phone,
                c.address,
                c.city,
                c.postal_code,
                c.country,
                c.registration_date,
                c.status,
                c.customer_type,
                c.credit_limit,
                COUNT(o.order_id) as total_orders,
                NVL(SUM(o.total_amount), 0) as total_spent,
                MAX(o.order_date) as last_order_date
            FROM customers c
            LEFT JOIN orders o ON c.customer_id = o.customer_id
            WHERE c.customer_id = ?
            GROUP BY c.customer_id, c.customer_name, c.email, c.phone, 
                     c.address, c.city, c.postal_code, c.country, 
                     c.registration_date, c.status, c.customer_type, c.credit_limit
            """;
        
        return executeQuerySingle(sql, customerId);
    }
    
    @LLMFunction(
        name = "oracle_get_customer_orders",
        description = "Lấy danh sách đơn hàng của một khách hàng cụ thể. " +
                     "Có thể lọc theo trạng thái và thời gian.",
        categories = {"oracle", "customer", "orders"}
    )
    public List<Map<String, Object>> getCustomerOrders(
            @Param(description = "ID của khách hàng", required = true)
            Long customerId,
            
            @Param(description = "Trạng thái đơn hàng (PENDING, COMPLETED, CANCELLED)", required = false)
            String status,
            
            @Param(description = "Số lượng đơn hàng tối đa", defaultValue = "10")
            Integer limit
    ) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        sql.append("SELECT ")
           .append("    o.order_id, ")
           .append("    o.order_date, ")
           .append("    o.total_amount, ")
           .append("    o.status, ")
           .append("    o.payment_method, ")
           .append("    COUNT(oi.product_id) as item_count ")
           .append("FROM orders o ")
           .append("LEFT JOIN order_items oi ON o.order_id = oi.order_id ")
           .append("WHERE o.customer_id = ? ");
        
        params.add(customerId);
        
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND UPPER(o.status) = UPPER(?) ");
            params.add(status.trim());
        }
        
        sql.append("GROUP BY o.order_id, o.order_date, o.total_amount, o.status, o.payment_method ")
           .append("ORDER BY o.order_date DESC ");
           
        sql.append(buildPaginationClause(limit, 0));
        
        return executeQuery(sql.toString(), params.toArray());
    }
    
    @LLMFunction(
        name = "oracle_get_top_customers",
        description = "Lấy danh sách khách hàng VIP/top theo tổng giá trị mua hàng. " +
                     "Hữu ích để phân tích khách hàng quan trọng.",
        categories = {"oracle", "customer", "analytics"}
    )
    public List<Map<String, Object>> getTopCustomers(
            @Param(description = "Số lượng khách hàng top", defaultValue = "10")
            Integer limit,
            
            @Param(description = "Tháng/năm để phân tích (YYYY-MM), để trống để lấy toàn bộ", required = false)
            String month
    ) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        sql.append("SELECT ")
           .append("    c.customer_id, ")
           .append("    c.customer_name, ")
           .append("    c.email, ")
           .append("    c.city, ")
           .append("    COUNT(o.order_id) as total_orders, ")
           .append("    SUM(o.total_amount) as total_spent, ")
           .append("    AVG(o.total_amount) as avg_order_value, ")
           .append("    MAX(o.order_date) as last_order_date ")
           .append("FROM customers c ")
           .append("INNER JOIN orders o ON c.customer_id = o.customer_id ");
        
        if (month != null && !month.trim().isEmpty()) {
            sql.append("WHERE TO_CHAR(o.order_date, 'YYYY-MM') = ? ");
            params.add(month.trim());
        }
        
        sql.append("GROUP BY c.customer_id, c.customer_name, c.email, c.city ")
           .append("HAVING SUM(o.total_amount) > 0 ")
           .append("ORDER BY total_spent DESC ");
           
        sql.append(buildPaginationClause(limit, 0));
        
        return executeQuery(sql.toString(), params.toArray());
    }
    
    @LLMFunction(
        name = "oracle_customer_statistics",
        description = "Thống kê tổng quan về khách hàng: tổng số, khách hàng mới, khách hàng hoạt động.",
        categories = {"oracle", "customer", "statistics"}
    )
    public Map<String, Object> getCustomerStatistics() {
        String sql = """
            SELECT 
                COUNT(*) as total_customers,
                COUNT(CASE WHEN registration_date >= TRUNC(SYSDATE, 'MM') THEN 1 END) as new_this_month,
                COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_customers,
                COUNT(CASE WHEN EXISTS (
                    SELECT 1 FROM orders o 
                    WHERE o.customer_id = customers.customer_id 
                    AND o.order_date >= SYSDATE - 30
                ) THEN 1 END) as active_buyers_30days
            FROM customers
            """;
        
        return executeQuerySingle(sql);
    }
}
```

### 2.3 Order Query Functions
```java
package com.noteflix.pcm.functions.oracle;

import com.noteflix.pcm.llm.annotation.FunctionProvider;
import com.noteflix.pcm.llm.annotation.LLMFunction;
import com.noteflix.pcm.llm.annotation.Param;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Oracle Order Query Functions
 * 
 * Các function để truy vấn thông tin đơn hàng từ Oracle database
 */
@FunctionProvider("Oracle Order Functions")
@Slf4j
public class OrderOracleFunction extends BaseOracleFunction {
    
    @LLMFunction(
        name = "oracle_search_orders",
        description = "Tìm kiếm đơn hàng theo order ID, customer name, hoặc khoảng thời gian. " +
                     "Hỗ trợ lọc theo trạng thái và phương thức thanh toán.",
        categories = {"oracle", "order", "search"}
    )
    public List<Map<String, Object>> searchOrders(
            @Param(description = "Từ khóa tìm kiếm (Order ID hoặc tên khách hàng)", required = false)
            String searchText,
            
            @Param(description = "Trạng thái đơn hàng (PENDING, COMPLETED, CANCELLED)", required = false)
            String status,
            
            @Param(description = "Từ ngày (YYYY-MM-DD)", required = false)
            String fromDate,
            
            @Param(description = "Đến ngày (YYYY-MM-DD)", required = false)
            String toDate,
            
            @Param(description = "Số lượng kết quả tối đa", defaultValue = "20")
            Integer limit,
            
            @Param(description = "Bỏ qua số record đầu", defaultValue = "0")
            Integer offset
    ) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        sql.append("SELECT ")
           .append("    o.order_id, ")
           .append("    o.order_date, ")
           .append("    c.customer_name, ")
           .append("    c.email as customer_email, ")
           .append("    o.total_amount, ")
           .append("    o.status, ")
           .append("    o.payment_method, ")
           .append("    COUNT(oi.product_id) as item_count ")
           .append("FROM orders o ")
           .append("INNER JOIN customers c ON o.customer_id = c.customer_id ")
           .append("LEFT JOIN order_items oi ON o.order_id = oi.order_id ");
        
        List<String> whereConditions = new ArrayList<>();
        
        // Search text filter
        if (searchText != null && !searchText.trim().isEmpty()) {
            whereConditions.add("(UPPER(c.customer_name) LIKE UPPER(?) OR o.order_id = ?)");
            String likePattern = buildLikePattern(searchText);
            params.add(likePattern);
            
            try {
                params.add(Long.parseLong(searchText.trim()));
            } catch (NumberFormatException e) {
                params.add(-1L);
            }
        }
        
        // Status filter
        if (status != null && !status.trim().isEmpty()) {
            whereConditions.add("UPPER(o.status) = UPPER(?)");
            params.add(status.trim());
        }
        
        // Date range filter
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            whereConditions.add("o.order_date >= TO_DATE(?, 'YYYY-MM-DD')");
            params.add(fromDate.trim());
        }
        
        if (toDate != null && !toDate.trim().isEmpty()) {
            whereConditions.add("o.order_date <= TO_DATE(?, 'YYYY-MM-DD') + 1");
            params.add(toDate.trim());
        }
        
        if (!whereConditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", whereConditions)).append(" ");
        }
        
        sql.append("GROUP BY o.order_id, o.order_date, c.customer_name, c.email, ")
           .append("         o.total_amount, o.status, o.payment_method ")
           .append("ORDER BY o.order_date DESC ");
           
        sql.append(buildPaginationClause(limit, offset));
        
        return executeQuery(sql.toString(), params.toArray());
    }
    
    @LLMFunction(
        name = "oracle_get_order_details",
        description = "Lấy thông tin chi tiết của một đơn hàng cụ thể bao gồm items và customer info.",
        categories = {"oracle", "order", "details"}
    )
    public Map<String, Object> getOrderDetails(
            @Param(description = "Order ID", required = true)
            Long orderId
    ) {
        // Get order basic info
        String orderSql = """
            SELECT 
                o.order_id,
                o.order_date,
                o.total_amount,
                o.status,
                o.payment_method,
                o.shipping_address,
                o.notes,
                c.customer_id,
                c.customer_name,
                c.email,
                c.phone
            FROM orders o
            INNER JOIN customers c ON o.customer_id = c.customer_id
            WHERE o.order_id = ?
            """;
        
        Map<String, Object> orderInfo = executeQuerySingle(orderSql, orderId);
        
        if (orderInfo != null) {
            // Get order items
            String itemsSql = """
                SELECT 
                    oi.product_id,
                    p.product_name,
                    oi.quantity,
                    oi.unit_price,
                    oi.total_price,
                    p.category
                FROM order_items oi
                INNER JOIN products p ON oi.product_id = p.product_id
                WHERE oi.order_id = ?
                ORDER BY oi.product_id
                """;
            
            List<Map<String, Object>> items = executeQuery(itemsSql, orderId);
            orderInfo.put("items", items);
        }
        
        return orderInfo;
    }
    
    @LLMFunction(
        name = "oracle_get_recent_orders",
        description = "Lấy danh sách đơn hàng gần đây nhất. Hữu ích để monitor hoạt động kinh doanh.",
        categories = {"oracle", "order", "recent"}
    )
    public List<Map<String, Object>> getRecentOrders(
            @Param(description = "Số ngày gần đây", defaultValue = "7")
            Integer days,
            
            @Param(description = "Số lượng đơn hàng", defaultValue = "20")
            Integer limit
    ) {
        String sql = """
            SELECT 
                o.order_id,
                o.order_date,
                c.customer_name,
                o.total_amount,
                o.status,
                o.payment_method
            FROM orders o
            INNER JOIN customers c ON o.customer_id = c.customer_id
            WHERE o.order_date >= SYSDATE - ?
            ORDER BY o.order_date DESC
            """ + buildPaginationClause(limit, 0);
        
        return executeQuery(sql, days);
    }
    
    @LLMFunction(
        name = "oracle_order_statistics",
        description = "Thống kê đơn hàng: tổng số, doanh thu, đơn hàng theo trạng thái.",
        categories = {"oracle", "order", "statistics"}
    )
    public Map<String, Object> getOrderStatistics(
            @Param(description = "Số ngày để thống kê (mặc định 30 ngày)", defaultValue = "30")
            Integer days
    ) {
        String sql = """
            SELECT 
                COUNT(*) as total_orders,
                SUM(total_amount) as total_revenue,
                AVG(total_amount) as avg_order_value,
                COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_orders,
                COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_orders,
                COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled_orders,
                COUNT(DISTINCT customer_id) as unique_customers
            FROM orders
            WHERE order_date >= SYSDATE - ?
            """;
        
        return executeQuerySingle(sql, days);
    }
}
```

### 2.4 Product Query Functions
```java
package com.noteflix.pcm.functions.oracle;

import com.noteflix.pcm.llm.annotation.FunctionProvider;
import com.noteflix.pcm.llm.annotation.LLMFunction;
import com.noteflix.pcm.llm.annotation.Param;
import java.util.*;

@FunctionProvider("Oracle Product Functions")
public class ProductOracleFunction extends BaseOracleFunction {
    
    @LLMFunction(
        name = "oracle_search_products",
        description = "Tìm kiếm sản phẩm theo tên, mô tả, hoặc category. Hỗ trợ lọc theo giá và tình trạng kho.",
        categories = {"oracle", "product", "search"}
    )
    public List<Map<String, Object>> searchProducts(
            @Param(description = "Từ khóa tìm kiếm", required = false)
            String searchText,
            
            @Param(description = "Danh mục sản phẩm", required = false)
            String category,
            
            @Param(description = "Giá tối thiểu", required = false)
            Double minPrice,
            
            @Param(description = "Giá tối đa", required = false)
            Double maxPrice,
            
            @Param(description = "Chỉ sản phẩm còn hàng", defaultValue = "false")
            Boolean inStockOnly,
            
            @Param(description = "Số lượng kết quả", defaultValue = "20")
            Integer limit
    ) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        sql.append("SELECT ")
           .append("    product_id, ")
           .append("    product_name, ")
           .append("    category, ")
           .append("    price, ")
           .append("    stock_quantity, ")
           .append("    description, ")
           .append("    status ")
           .append("FROM products ");
        
        List<String> conditions = new ArrayList<>();
        
        if (searchText != null && !searchText.trim().isEmpty()) {
            conditions.add("(UPPER(product_name) LIKE UPPER(?) OR UPPER(description) LIKE UPPER(?))");
            String pattern = buildLikePattern(searchText);
            params.add(pattern);
            params.add(pattern);
        }
        
        if (category != null && !category.trim().isEmpty()) {
            conditions.add("UPPER(category) = UPPER(?)");
            params.add(category.trim());
        }
        
        if (minPrice != null) {
            conditions.add("price >= ?");
            params.add(minPrice);
        }
        
        if (maxPrice != null) {
            conditions.add("price <= ?");
            params.add(maxPrice);
        }
        
        if (inStockOnly) {
            conditions.add("stock_quantity > 0");
        }
        
        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
        }
        
        sql.append("ORDER BY product_name ");
        sql.append(buildPaginationClause(limit, 0));
        
        return executeQuery(sql.toString(), params.toArray());
    }
    
    @LLMFunction(
        name = "oracle_get_product_sales",
        description = "Thống kê doanh số bán hàng của sản phẩm theo thời gian.",
        categories = {"oracle", "product", "sales"}
    )
    public List<Map<String, Object>> getProductSales(
            @Param(description = "Product ID (để trống để lấy all products)", required = false)
            Long productId,
            
            @Param(description = "Số ngày gần đây", defaultValue = "30")
            Integer days,
            
            @Param(description = "Số lượng sản phẩm top", defaultValue = "10")
            Integer limit
    ) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        sql.append("SELECT ")
           .append("    p.product_id, ")
           .append("    p.product_name, ")
           .append("    p.category, ")
           .append("    COUNT(oi.order_item_id) as order_count, ")
           .append("    SUM(oi.quantity) as total_quantity, ")
           .append("    SUM(oi.total_price) as total_revenue ")
           .append("FROM products p ")
           .append("INNER JOIN order_items oi ON p.product_id = oi.product_id ")
           .append("INNER JOIN orders o ON oi.order_id = o.order_id ")
           .append("WHERE o.order_date >= SYSDATE - ? ");
        
        params.add(days);
        
        if (productId != null) {
            sql.append("AND p.product_id = ? ");
            params.add(productId);
        }
        
        sql.append("GROUP BY p.product_id, p.product_name, p.category ")
           .append("ORDER BY total_revenue DESC ");
           
        sql.append(buildPaginationClause(limit, 0));
        
        return executeQuery(sql.toString(), params.toArray());
    }
}
```

## 3. LLM Integration Setup

### 3.1 Oracle Function Registration
```java
package com.noteflix.pcm.functions.oracle;

import com.noteflix.pcm.llm.registry.FunctionRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Oracle Function Initializer
 * 
 * Đăng ký tất cả Oracle functions với LLM system
 */
@Slf4j
public class OracleFunctionInitializer {
    
    public static void initializeOracleFunctions() {
        log.info("🔧 Initializing Oracle Function Calling...");
        
        try {
            // Verify Oracle connection
            if (!OracleConnectionManager.getInstance().testConnection()) {
                log.warn("⚠️ Oracle connection not available. Functions will be disabled.");
                return;
            }
            
            FunctionRegistry registry = FunctionRegistry.getInstance();
            
            // Scan Oracle function packages
            int functionsRegistered = registry.scanPackage("com.noteflix.pcm.functions.oracle");
            
            log.info("✅ Oracle functions initialized: {} functions registered", functionsRegistered);
            
            // Log registered Oracle functions
            registry.getAllFunctions().values().stream()
                .filter(f -> f.getName().startsWith("oracle_"))
                .forEach(f -> log.info("📋 Registered: {} - {}", f.getName(), f.getDescription()));
                
        } catch (Exception e) {
            log.error("❌ Failed to initialize Oracle functions", e);
        }
    }
}
```

### 3.2 Integration với AI Assistant
```java
package com.noteflix.pcm.ui.pages;

// Cập nhật AIAssistantPage để support Oracle functions

public class AIAssistantPage extends VBox {
    
    // ... existing code ...
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ... existing initialization ...
        
        // Initialize Oracle functions
        OracleFunctionInitializer.initializeOracleFunctions();
        
        // Add Oracle-specific message examples
        addOracleExamples();
    }
    
    private void addOracleExamples() {
        // Add example messages for Oracle queries
        exampleMessages.addAll(Arrays.asList(
            "Tìm khách hàng có tên Nguyễn",
            "Cho tôi xem 10 đơn hàng gần nhất",
            "Thống kê doanh số tháng này",
            "Tìm sản phẩm trong danh mục điện tử",
            "Khách hàng nào mua nhiều nhất?",
            "Đơn hàng nào đang pending?",
            "Xem chi tiết đơn hàng số 12345"
        ));
    }
    
    // ... existing sendMessage method enhanced with Oracle context ...
    
    private void sendMessage() {
        String messageText = messageInput.getText().trim();
        if (messageText.isEmpty()) return;
        
        // ... existing code ...
        
        // Get all available tools (including Oracle functions)
        List<Tool> tools = FunctionRegistry.getInstance().getAllTools();
        
        // Filter Oracle tools if Oracle is available
        if (OracleConnectionManager.getInstance().testConnection()) {
            // Oracle is available - include Oracle functions
            log.debug("Oracle connection available - including {} Oracle functions", 
                tools.stream().filter(t -> t.getName().startsWith("oracle_")).count());
        } else {
            // Oracle not available - filter out Oracle functions
            tools = tools.stream()
                .filter(t -> !t.getName().startsWith("oracle_"))
                .collect(Collectors.toList());
            log.debug("Oracle connection not available - using {} non-Oracle functions", tools.size());
        }
        
        // ... continue with existing LLM call with tools ...
    }
}
```

## 4. Configuration & Setup Guide

### 4.1 Oracle Database Preparation
```sql
-- Sample Oracle database schema for testing

-- Customers table
CREATE TABLE customers (
    customer_id NUMBER PRIMARY KEY,
    customer_name VARCHAR2(100) NOT NULL,
    email VARCHAR2(100),
    phone VARCHAR2(20),
    address VARCHAR2(200),
    city VARCHAR2(50),
    postal_code VARCHAR2(10),
    country VARCHAR2(50),
    registration_date DATE DEFAULT SYSDATE,
    status VARCHAR2(20) DEFAULT 'ACTIVE',
    customer_type VARCHAR2(20) DEFAULT 'REGULAR',
    credit_limit NUMBER(10,2)
);

-- Products table
CREATE TABLE products (
    product_id NUMBER PRIMARY KEY,
    product_name VARCHAR2(100) NOT NULL,
    category VARCHAR2(50),
    price NUMBER(10,2),
    stock_quantity NUMBER DEFAULT 0,
    description CLOB,
    status VARCHAR2(20) DEFAULT 'ACTIVE'
);

-- Orders table
CREATE TABLE orders (
    order_id NUMBER PRIMARY KEY,
    customer_id NUMBER NOT NULL,
    order_date DATE DEFAULT SYSDATE,
    total_amount NUMBER(10,2),
    status VARCHAR2(20) DEFAULT 'PENDING',
    payment_method VARCHAR2(20),
    shipping_address VARCHAR2(200),
    notes CLOB,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- Order items table
CREATE TABLE order_items (
    order_item_id NUMBER PRIMARY KEY,
    order_id NUMBER NOT NULL,
    product_id NUMBER NOT NULL,
    quantity NUMBER NOT NULL,
    unit_price NUMBER(10,2),
    total_price NUMBER(10,2),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Create sequences
CREATE SEQUENCE seq_customers START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_products START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_orders START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_order_items START WITH 1 INCREMENT BY 1;

-- Create indexes for performance
CREATE INDEX idx_customers_name ON customers(customer_name);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_date ON orders(order_date);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);

-- Sample data
INSERT INTO customers (customer_id, customer_name, email, phone, city) VALUES 
(seq_customers.NEXTVAL, 'Nguyễn Văn An', 'an.nguyen@email.com', '0901234567', 'Hà Nội'),
(seq_customers.NEXTVAL, 'Trần Thị Bình', 'binh.tran@email.com', '0912345678', 'TP.HCM'),
(seq_customers.NEXTVAL, 'Lê Văn Cường', 'cuong.le@email.com', '0923456789', 'Đà Nẵng');

INSERT INTO products (product_id, product_name, category, price, stock_quantity) VALUES
(seq_products.NEXTVAL, 'Laptop Dell XPS 13', 'Electronics', 25000000, 10),
(seq_products.NEXTVAL, 'iPhone 15 Pro', 'Electronics', 28000000, 15),
(seq_products.NEXTVAL, 'Samsung Galaxy S24', 'Electronics', 22000000, 12);

COMMIT;
```

### 4.2 Application Configuration
Update `PCMApplication.java`:

```java
@Override
public void init() throws Exception {
    super.init();
    
    // ... existing initialization ...
    
    // Initialize Oracle connection if configured
    initializeOracleIntegration();
    
    // Initialize LLM function registry
    initializeFunctionRegistry();
}

private void initializeOracleIntegration() {
    try {
        OracleConfig config = OracleConfigLoader.loadConfiguration();
        OracleConnectionManager.getInstance().initialize(config);
        
        if (OracleConnectionManager.getInstance().testConnection()) {
            log.info("✅ Oracle integration initialized successfully");
            
            // Initialize Oracle functions
            OracleFunctionInitializer.initializeOracleFunctions();
        } else {
            log.warn("⚠️ Oracle connection test failed - functions will be disabled");
        }
        
    } catch (Exception e) {
        log.warn("⚠️ Oracle configuration not found or invalid - running without Oracle integration", e);
    }
}

private void initializeFunctionRegistry() {
    FunctionRegistry registry = FunctionRegistry.getInstance();
    
    // Scan for all LLM functions in the application
    registry.scanPackage("com.noteflix.pcm.functions");
    
    log.info("✅ Function registry initialized with {} functions", registry.getFunctionCount());
}
```

## 5. User Interface Enhancements

### 5.1 Oracle Connection Status Widget
```java
package com.noteflix.pcm.ui.components;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class OracleStatusWidget extends HBox {
    
    private final Label statusLabel;
    private final Button testButton;
    private final ProgressIndicator progressIndicator;
    
    public OracleStatusWidget() {
        setSpacing(10);
        
        statusLabel = new Label();
        testButton = new Button("Test Connection");
        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(16, 16);
        progressIndicator.setVisible(false);
        
        setupEventHandlers();
        updateStatus();
        
        getChildren().addAll(new Label("Oracle:"), statusLabel, testButton, progressIndicator);
    }
    
    private void setupEventHandlers() {
        testButton.setOnAction(e -> testConnection());
    }
    
    private void testConnection() {
        progressIndicator.setVisible(true);
        testButton.setDisable(true);
        
        // Test connection in background
        CompletableFuture.supplyAsync(() -> {
            return OracleConnectionManager.getInstance().testConnection();
        }).whenComplete((success, error) -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                testButton.setDisable(false);
                updateStatus();
                
                if (success) {
                    showNotification("Oracle Connection", "Connection successful!");
                } else {
                    showErrorDialog("Oracle Connection", "Connection failed!");
                }
            });
        });
    }
    
    private void updateStatus() {
        boolean connected = OracleConnectionManager.getInstance().testConnection();
        
        if (connected) {
            statusLabel.setText("✅ Connected");
            statusLabel.getStyleClass().removeAll("status-error");
            statusLabel.getStyleClass().add("status-success");
        } else {
            statusLabel.setText("❌ Disconnected");
            statusLabel.getStyleClass().removeAll("status-success");
            statusLabel.getStyleClass().add("status-error");
        }
    }
}
```

### 5.2 Function Usage Analytics
```java
package com.noteflix.pcm.ui.pages;

public class FunctionAnalyticsPage extends VBox {
    
    @FXML private TableView<FunctionUsageStats> usageTable;
    @FXML private PieChart functionTypeChart;
    @FXML private LineChart<String, Number> usageTimeChart;
    
    public void initialize() {
        setupUsageTable();
        loadFunctionAnalytics();
    }
    
    private void loadFunctionAnalytics() {
        // Load Oracle function usage statistics
        List<FunctionUsageStats> stats = loadUsageStats();
        
        // Update table
        usageTable.getItems().setAll(stats);
        
        // Update charts
        updateFunctionTypeChart(stats);
        updateUsageTimeChart(stats);
    }
    
    private List<FunctionUsageStats> loadUsageStats() {
        // Get function usage from LLM logs
        return Arrays.asList(
            new FunctionUsageStats("oracle_search_customers", 45, "Customer queries"),
            new FunctionUsageStats("oracle_search_orders", 38, "Order queries"),
            new FunctionUsageStats("oracle_get_order_details", 22, "Order details"),
            new FunctionUsageStats("oracle_search_products", 18, "Product searches"),
            new FunctionUsageStats("oracle_customer_statistics", 12, "Statistics")
        );
    }
}

@Data
@AllArgsConstructor
class FunctionUsageStats {
    private String functionName;
    private int usageCount;
    private String description;
}
```

## 6. Testing & Validation

### 6.1 Oracle Function Tests
```java
package com.noteflix.pcm.functions.oracle;

@ExtendWith(MockitoExtension.class)
class CustomerOracleFunctionTest {
    
    @InjectMocks
    private CustomerOracleFunction customerFunction;
    
    @Mock
    private OracleConnectionManager oracleManager;
    
    @Mock
    private Connection connection;
    
    @Test
    void testSearchCustomers() throws SQLException {
        // Setup
        when(oracleManager.getConnection()).thenReturn(connection);
        
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(any())).thenReturn(stmt);
        
        ResultSet rs = mock(ResultSet.class);
        when(stmt.executeQuery()).thenReturn(rs);
        
        // Mock result set data
        when(rs.next()).thenReturn(true, false);
        when(rs.getObject("customer_id")).thenReturn(1L);
        when(rs.getObject("customer_name")).thenReturn("Nguyễn Văn An");
        when(rs.getObject("email")).thenReturn("an.nguyen@email.com");
        
        // Execute
        List<Map<String, Object>> result = customerFunction.searchCustomers("Nguyễn", 10, 0);
        
        // Verify
        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("customer_name")).isEqualTo("Nguyễn Văn An");
        
        verify(stmt).setObject(1, "%Nguyễn%");
        verify(stmt).setObject(2, "%Nguyễn%");
    }
}
```

### 6.2 Integration Test
```java
@SpringBootTest
@TestPropertySource(locations = "classpath:test-oracle.properties")
class OracleIntegrationTest {
    
    @Test
    void testOracleConnectionAndQueries() {
        // Test connection
        OracleConnectionManager manager = OracleConnectionManager.getInstance();
        assertTrue(manager.testConnection(), "Oracle connection should be available");
        
        // Test function execution
        CustomerOracleFunction customerFunction = new CustomerOracleFunction();
        List<Map<String, Object>> customers = customerFunction.searchCustomers("Test", 5, 0);
        
        assertNotNull(customers);
        
        // Test function registration
        FunctionRegistry registry = FunctionRegistry.getInstance();
        assertTrue(registry.hasFunction("oracle_search_customers"));
        
        // Test function execution through registry
        Map<String, Object> args = Map.of(
            "searchText", "Test",
            "limit", 5
        );
        
        Object result = registry.execute("oracle_search_customers", args);
        assertNotNull(result);
    }
}
```

## 7. Security & Best Practices

### 7.1 SQL Injection Prevention
```java
public class OracleSecurityUtils {
    
    /**
     * Validate and sanitize SQL parameters
     */
    public static String sanitizeSqlInput(String input) {
        if (input == null) return null;
        
        // Remove dangerous SQL keywords
        String sanitized = input
            .replaceAll("(?i)(;|--|/\\*|\\*/|xp_|sp_)", "")
            .replaceAll("(?i)(drop|delete|truncate|alter|create)", "")
            .trim();
        
        return sanitized;
    }
    
    /**
     * Validate numeric parameters
     */
    public static Long validateNumericParameter(Object value, String paramName) {
        if (value == null) return null;
        
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric parameter: " + paramName);
        }
    }
    
    /**
     * Limit query result size
     */
    public static int limitResultSize(Integer requestedLimit) {
        if (requestedLimit == null) return 20;
        return Math.min(requestedLimit, 1000); // Max 1000 results
    }
}
```

### 7.2 Connection Monitoring
```java
package com.noteflix.pcm.infrastructure.database.oracle;

@Component
public class OracleConnectionMonitor {
    
    private static final ScheduledExecutorService scheduler = 
        Executors.newScheduledThreadPool(1);
    
    @PostConstruct
    public void startMonitoring() {
        scheduler.scheduleAtFixedRate(this::checkConnectionHealth, 60, 60, TimeUnit.SECONDS);
    }
    
    private void checkConnectionHealth() {
        try {
            OracleConnectionManager manager = OracleConnectionManager.getInstance();
            
            if (!manager.testConnection()) {
                log.warn("Oracle connection lost - attempting reconnection");
                
                // Try to reconnect
                manager.close();
                
                if (manager.testConnection()) {
                    log.info("Oracle connection restored");
                } else {
                    log.error("Failed to restore Oracle connection");
                    // Could notify admin or disable Oracle functions
                }
            }
            
        } catch (Exception e) {
            log.error("Error in Oracle connection monitoring", e);
        }
    }
    
    @PreDestroy
    public void stopMonitoring() {
        scheduler.shutdown();
    }
}
```

## 8. Deployment Guide

### 8.1 Production Configuration
```properties
# oracle-database-production.properties

# Production Oracle settings
oracle.host=prod-oracle-server.company.com
oracle.port=1521
oracle.service=${ORACLE_SERVICE_NAME}
oracle.username=${ORACLE_APP_USER}
oracle.password=${ORACLE_APP_PASSWORD}

# Connection pool for production
oracle.connection.pool.enabled=true
oracle.connection.pool.min.size=5
oracle.connection.pool.max.size=20
oracle.connection.timeout=30000

# Security settings
oracle.ssl.enabled=true
oracle.ssl.truststore.path=/path/to/truststore.jks
oracle.ssl.truststore.password=${TRUSTSTORE_PASSWORD}

# Monitoring
oracle.health.check.interval=60
oracle.connection.test.query=SELECT 1 FROM DUAL
```

### 8.2 Docker Environment Variables
```dockerfile
# In Dockerfile or docker-compose.yml
ENV ORACLE_HOST=oracle-db-server
ENV ORACLE_PORT=1521
ENV ORACLE_SERVICE=CUSTOMER_PROD
ENV ORACLE_USERNAME=pcm_app
ENV ORACLE_PASSWORD=secure_password
ENV ORACLE_SCHEMA=CUSTOMER_DATA
```

## Tổng kết

Tài liệu này cung cấp implementation guide hoàn chỉnh để tích hợp Oracle Database vào PCM Desktop Application như một data source bổ sung cho LLM Function Calling:

### Key Features:
1. **Dual Database Architecture**: SQLite cho app data, Oracle cho customer data
2. **Comprehensive Function Library**: Customer, Order, Product query functions
3. **LLM Integration**: Seamless integration với existing function calling system
4. **Security**: SQL injection prevention, parameter validation
5. **Monitoring**: Connection health monitoring và automatic reconnection
6. **Flexible Configuration**: Environment-based configuration cho deployment

### Benefits:
- **Easy Customer Support**: AI assistant có thể truy xuất customer data instantly
- **Natural Language Queries**: Khách hàng có thể hỏi bằng tiếng Việt tự nhiên
- **Comprehensive Data Access**: Covers customers, orders, products, analytics
- **Production Ready**: Security, monitoring, error handling đầy đủ

### Use Cases:
- "Tìm khách hàng có tên Nguyễn Văn An"
- "Cho tôi xem đơn hàng gần nhất"
- "Thống kê doanh thu tháng này"
- "Sản phẩm nào bán chạy nhất?"
- "Khách hàng VIP là ai?"

Hệ thống này giúp customer service team truy xuất thông tin Oracle database một cách nhanh chóng và chính xác thông qua AI assistant.