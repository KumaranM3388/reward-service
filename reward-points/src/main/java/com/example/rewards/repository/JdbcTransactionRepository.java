package com.example.rewards.repository;

import com.example.rewards.model.TransactionRecord;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTransactionRepository implements TransactionRepository {
    private static final String FIND_SQL = """
            SELECT id, customer_id, transaction_date, amount
            FROM transactions
            WHERE transaction_date >= ? AND transaction_date <= ?
            ORDER BY customer_id, transaction_date, id
            """;
    private final JdbcTemplate jdbcTemplate;

    public JdbcTransactionRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @Override
    public List<TransactionRecord> findTransactions(LocalDate startDate, LocalDate endDate) {
        return jdbcTemplate.query(FIND_SQL, this::mapRow, startDate, endDate);
    }

    private TransactionRecord mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        return new TransactionRecord(resultSet.getLong("id"), resultSet.getLong("customer_id"),
                resultSet.getDate("transaction_date").toLocalDate(), resultSet.getBigDecimal("amount"));
    }
}
