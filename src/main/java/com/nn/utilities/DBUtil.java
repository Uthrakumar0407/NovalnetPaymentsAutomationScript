package com.nn.utilities;

import java.sql.*;

public class DBUtil {


    private static Connection getDBConnection(String DB_HOST, String DB_NAME, String DB_USERNAME, String DB_PASSWORD) throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://" + DB_HOST + ":3306/" + DB_NAME + "?useSSL=false",
                DB_USERNAME,
                DB_PASSWORD);
    }

    public static Connection connectDB(String DB_HOST, String DB_NAME, String DB_USERNAME, String DB_PASSWORD) throws SQLException {
        Connection connection = null;
        try {
            connection = getDBConnection(DB_HOST, DB_NAME, DB_USERNAME, DB_PASSWORD);
        } catch (SQLException e) {
            Log.error("Error connecting to database");
            throw new SQLException("Error connecting to database", e);
        }
        return connection;
    }

    public static void countOfMagentoRecords(String DB_HOST, String DB_NAME, String DB_USERNAME, String DB_PASSWORD, String tableName) throws SQLException {
        Connection connection = null;
        try {
            connection = connectDB(DB_HOST, DB_NAME, DB_USERNAME, DB_PASSWORD);
            if (connection == null) {
                return;
            }

            try (Statement statement = connection.createStatement()) {
                // SQL query to get the count of records in the specified table
                String sqlQuery = "SELECT COUNT(*) AS record_count FROM " + tableName;

                // Execute the query and get the result set
                try (ResultSet resultSet = statement.executeQuery(sqlQuery)) {
                    // Process the result set and get the record count
                    int recordCount = 0;
                    if (resultSet.next()) {
                        recordCount = resultSet.getInt("record_count");
                    }

                    Log.info("Total records in " + tableName + " table: " + recordCount);
                }
            } catch (SQLException e) {
                Log.error("Error executing countOfRecords");
                throw new SQLException("Error connecting to database", e);
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }


    public static void createMagentoStoreView(String DB_HOST, String DB_NAME, String DB_USERNAME, String DB_PASSWORD, String storeCode, String storeName, int storeGroupId) throws SQLException {
      /*  Connection connection = null;
        try {
            connection = connectDB(DB_HOST, DB_NAME, DB_USERNAME, DB_PASSWORD);
            if (connection == null) {
                return;
            }

            try (Statement statement = connection.createStatement()) {
                String query = "INSERT INTO store (store_id, code, website_id, group_id, name, sort_order, is_active) " +
                        "VALUES (NULL, '" + storeCode + "', 1, " + storeGroupId + ", '" + storeName + "', 0, 1)";
                int affectedRows = statement.executeUpdate(query);

                if (affectedRows > 0) {
                    Log.info("Created store view with code '" + storeCode + "' and name '" + storeName + "'");
                } else {
                    Log.error("Failed to create store view.");
                }
            } catch (SQLException e) {
                Log.error("Error executing createStoreView: " + e.getMessage());
                throw new SQLException("Error executing createStoreView", e);
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }*/

        Connection connection = null;
        try {
            connection = connectDB(DB_HOST, DB_NAME, DB_USERNAME, DB_PASSWORD);
            if (connection == null) {
                return;
            }

            // Get the next value for the auto-incremented primary key (store_id)
            int nextStoreId;
            try (PreparedStatement idStatement = connection.prepareStatement("SELECT AUTO_INCREMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'store'")) {
                idStatement.setString(1, DB_NAME);
                try (ResultSet idResultSet = idStatement.executeQuery()) {
                    if (idResultSet.next()) {
                        nextStoreId = idResultSet.getInt("AUTO_INCREMENT");
                    } else {
                        Log.error("Failed to retrieve the next auto-increment ID for the 'store' table.");
                        return;
                    }
                }
            }

            // Now, manually set the store_id and insert the data
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO store (store_id, code, website_id, group_id, name, sort_order, is_active) VALUES (?, ?, ?, ?, ?, 0, 1)")) {
                preparedStatement.setInt(1, nextStoreId); // Manually set the auto-incremented ID
                preparedStatement.setString(2, "pln");
                preparedStatement.setInt(3, 0);
                preparedStatement.setInt(4, 0);
                preparedStatement.setString(5, storeName);


                // Execute the insert operation
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    Log.info("Created store view with ID " + nextStoreId + ", code '" + storeCode + "', and name '" + storeName + "'");
                } else {
                    Log.error("Failed to create store view.");
                }
            } catch (SQLException e) {
                Log.error("Error executing createStoreView: " + e.getMessage());
                throw new SQLException("Error executing createStoreView", e);
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

    }

    public static void configureMagentoStoreViewCurrency(String DB_HOST, String DB_NAME, String DB_USERNAME, String DB_PASSWORD, String storeCode, String baseCurrency, String defaultCurrency, String allowedCurrencies) throws SQLException {
        Connection connection = null;
        try {
            connection = connectDB(DB_HOST, DB_NAME, DB_USERNAME, DB_PASSWORD);
            if (connection == null) {
                return;
            }

            // Delete existing currency settings for the store view
            try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM core_config_data WHERE scope = 'stores' AND scope_id = (SELECT store_id FROM store WHERE code = ?) AND path LIKE 'currency/options/%'")) {
                deleteStatement.setString(1, storeCode);
                deleteStatement.executeUpdate();
            }

            // Insert new currency settings for the store view
            try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO core_config_data (scope, scope_id, path, value) VALUES (?, (SELECT store_id FROM store WHERE code = ?), ?, ?)")) {
                // Insert base currency setting
              insertStatement.setString(1, "stores");
                insertStatement.setString(2, storeCode);
            /*    insertStatement.setString(3, "currency/options/base");
                insertStatement.setString(4, baseCurrency);
                insertStatement.executeUpdate();*/

                // Insert default currency setting
                insertStatement.setString(3, "currency/options/default");
                insertStatement.setString(4, defaultCurrency);
                insertStatement.executeUpdate();

                // Insert allowed currencies setting
                insertStatement.setString(3, "currency/options/allow");
                insertStatement.setString(4, allowedCurrencies);
                insertStatement.executeUpdate();
            }

            Log.info("Configured currency settings for store view with code '" + storeCode + "'");
        } catch (SQLException e) {
            Log.error("Error executing configureMagentoStoreViewCurrency: " + e.getMessage());
            throw new SQLException("Error executing configureMagentoStoreViewCurrency", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public static void deleteMagentoRecordsWithEmailPrefix(String DB_HOST, String DB_NAME, String DB_USERNAME, String DB_PASSWORD, String tableName, String emailPrefix) throws SQLException {
        Connection connection = null;
        try {
            connection = connectDB(DB_HOST, DB_NAME, DB_USERNAME, DB_PASSWORD);
            if (connection == null) {
                return;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE email LIKE ?")) {
                preparedStatement.setString(1, emailPrefix + "%");
                // Execute the delete operation
                int deletedRows = preparedStatement.executeUpdate();
                Log.info("Deleted " + deletedRows + " records from " + tableName + " table with email prefix '" + emailPrefix + "'");
            } catch (SQLException e) {
                Log.error("Error executing deleteRecordsWithEmailPrefix");
                throw new SQLException("Error connecting to database", e);
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }


    public static void updateWooCommerceRoleToAdministrator(String DB_HOST, String DB_NAME, String DB_USERNAME, String DB_PASSWORD, int userId) throws SQLException {
        Connection connection = null;
        try {
            connection = connectDB(DB_HOST, DB_NAME, DB_USERNAME, DB_PASSWORD);
            if (connection == null) {
                return;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE wp_usermeta SET meta_value = 'a:1:{s:13:\"administrator\";s:1:\"1\";}' WHERE user_id = ? AND meta_key = 'wp_capabilities'")) {
                preparedStatement.setInt(1, userId);
                // Execute the update operation
                int updatedRows = preparedStatement.executeUpdate();
                if (updatedRows > 0) {
                    Log.info("User with ID " + userId + " role updated to 'Administrator'");
                } else {
                    Log.info("User with ID " + userId + " not found or role already set as 'Administrator'");
                }
            } catch (SQLException e) {
                Log.error("Error executing updateRoleToAdministrator");
                throw new SQLException("Error connecting to database", e);
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }


    public static int getWooCommerceUserIdByEmail(String DB_HOST, String DB_NAME, String DB_USERNAME, String DB_PASSWORD, String email) throws SQLException {
        Connection connection = null;
        int userId = -1; // Default value if user ID is not found

        try {
            connection = connectDB(DB_HOST, DB_NAME, DB_USERNAME, DB_PASSWORD);
            if (connection == null) {
                return userId;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT ID FROM wp_users WHERE user_email = ?")) {
                preparedStatement.setString(1, email);
                // Execute the query to retrieve user ID by email
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    userId = resultSet.getInt("ID");
                }
            } catch (SQLException e) {
                Log.error("Error executing getUserIdByEmail");
                throw new SQLException("Error connecting to database", e);
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        return userId;
    }


    public static void updateWooCommerceProductTypeToSubscription(String host, String database, String username, String password, String productName) {
        Connection connection = null;
        try {
            // Connect to the database
            connection = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database, username, password);

            // Find the product ID based on the product name
            int productId = findWooCommerceProductIdByName(connection, productName);
            if (productId == -1) {
                System.out.println("Product with name '" + productName + "' not found.");
                return;
            }

            // Check if the product attributes exist in the wp_postmeta table
            String existingAttributes = getExistingWooCommerceProductAttributes(connection, productId);

            if (existingAttributes == null) {
                System.out.println("Product attributes not found for product with name '" + productName + "'.");
                return;
            }

            // Modify the product type to "subscription" in the existing attributes
            String updatedAttributes = modifyWooCommerceProductAttributes(existingAttributes);

            // Update the product attributes
            String updateQuery = "UPDATE wp_postmeta SET meta_value = ? WHERE post_id = ? AND meta_key = '_product_attributes'";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                preparedStatement.setString(1, updatedAttributes);
                preparedStatement.setInt(2, productId);

                // Execute the update operation
                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Product type updated to 'subscription' for product with name '" + productName + "'.");
                } else {
                    System.out.println("Failed to update product type.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static int findWooCommerceProductIdByName(Connection connection, String productName) throws SQLException {
        String query = "SELECT ID FROM wp_posts WHERE post_type = 'product' AND post_title = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, productName);

            // Execute the query to find the product ID
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                System.out.println("Product ID: " + resultSet.getInt("ID"));
                return resultSet.getInt("ID");
            }
        }
        return -1; // Product not found
    }


    private static String getExistingWooCommerceProductAttributes(Connection connection, int productId) throws SQLException {
        String query = "SELECT meta_value FROM wp_postmeta WHERE post_id = ? AND meta_key = '_product_attributes'";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, productId);

            // Execute the query to check if the product attributes exist
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {

                System.out.println("Meta_value: " + resultSet.getString("meta_value"));
                return resultSet.getString("meta_value");
            }
        }
        return null; // Product attributes not found
    }

    private static String modifyWooCommerceProductAttributes(String existingAttributes) {

        System.out.println("Existing attributes before update: " + existingAttributes);
        // Find the index of "type":"simple" in the existing attributes string
        int startIndex = existingAttributes.indexOf("\"type\":\"simple\"");
        if (startIndex != -1) {
            // Replace "simple" with "subscription"
            int endIndex = startIndex + "\"type\":\"simple\"".length();
            String updatedAttributes = existingAttributes.substring(0, startIndex) + "\"type\":\"subscription\"" + existingAttributes.substring(endIndex);
            System.out.println("Existing attributes after update: " + existingAttributes);
            return updatedAttributes;
        }

        // If "type":"simple" is not found, return the original string
        return existingAttributes;
    }

    public static void updateFirstNameWithAPrefix(Connection connection) throws SQLException {
        // Select the first product
        String selectQuery = "SELECT ID, post_title FROM wp_posts WHERE post_type = 'product' ORDER BY ID ASC LIMIT 1";
        String updateQuery = "UPDATE wp_posts SET post_title = ? WHERE ID = ?";

        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {

            ResultSet resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                int productId = resultSet.getInt("ID");
                String currentName = resultSet.getString("post_title");
                String updatedName = "A" + currentName; // Prefix with "A"

                // Update the product name
                updateStatement.setString(1, updatedName);
                updateStatement.setInt(2, productId);
                int rowsUpdated = updateStatement.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("Product with ID " + productId + " updated to: " + updatedName);
                } else {
                    System.out.println("No product updated.");
                }
            } else {
                System.out.println("No product found.");
            }
        }
    }

    public static void updateWooCommerceProductName(String host, String database, String username, String password ) {
        Connection connection = null;
        try {
            // Connect to the database
            connection = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database, username, password);

            // Find the product ID based on the product name
            updateFirstNameWithAPrefix(connection);

        } catch (Exception e) {
        }
    }


    public static void updateShopwareProductPrice(String DB_HOST, String DB_NAME, String DB_USERNAME, String DB_PASSWORD, String productNumber, double amount) throws SQLException {
        Connection connection = null;
        try {
            // Use the fixed price JSON string with the given amount
            String price = "{\"cb7d2554b0ce847cd82f3ac9bd1c0dfca\": {\"net\": " + amount + ", \"gross\": " + amount + ", \"linked\": true, \"currencyId\": \"b7d2554b0ce847cd82f3ac9bd1c0dfca\"}}";

            connection = connectDB(DB_HOST, DB_NAME, DB_USERNAME, DB_PASSWORD);
            if (connection == null) {
                return;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE product SET price = ? WHERE product_number = ?")) {
                preparedStatement.setString(1, price);
                preparedStatement.setString(2, productNumber);

                // Execute the update operation
                int updatedRows = preparedStatement.executeUpdate();
                if (updatedRows > 0) {
                    System.out.println("Product with product number " + productNumber + " updated.");
                } else {
                    System.out.println("Product with product number " + productNumber + " not found.");
                }
            } catch (SQLException e) {
                System.err.println("Error executing update operation: " + e.getMessage());
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }


}
