package com.example.rewards.repository;

import com.example.rewards.model.TransactionRecord;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository {
    List<TransactionRecord> findTransactions(LocalDate startDate, LocalDate endDate);
}
