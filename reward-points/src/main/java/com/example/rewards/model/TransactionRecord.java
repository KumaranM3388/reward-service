package com.example.rewards.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRecord(long id, long customerId, LocalDate transactionDate, BigDecimal amount) { }
