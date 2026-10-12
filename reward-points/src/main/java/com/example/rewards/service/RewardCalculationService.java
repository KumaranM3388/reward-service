package com.example.rewards.service;

import com.example.rewards.exception.InvalidDateRangeException;
import com.example.rewards.model.CustomerRewardSummary;
import com.example.rewards.model.MonthlyReward;
import com.example.rewards.model.TransactionRecord;
import com.example.rewards.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;

@Service
public class RewardCalculationService {
    private static final BigDecimal FIFTY = BigDecimal.valueOf(50);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private final TransactionRepository transactionRepository;

    public RewardCalculationService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

   
    public List<CustomerRewardSummary> calculateRewards(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new InvalidDateRangeException("startDate and endDate are required.");
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException("startDate must be on or before endDate.");
        }

        Map<Long, Map<YearMonth, Integer>> totals = new TreeMap<>();
        for (TransactionRecord transaction : transactionRepository.findTransactions(startDate, endDate)) {
            YearMonth month = YearMonth.from(transaction.transactionDate());
            int points = calculatePoints(transaction.amount());
            totals.computeIfAbsent(transaction.customerId(), ignored -> new TreeMap<>())
                    .merge(month, points, (current, added) -> Math.addExact(current, added));
        }

        List<CustomerRewardSummary> response = new ArrayList<>();
        totals.forEach((customerId, monthlyTotals) -> {
            List<MonthlyReward> monthlyRewards = new ArrayList<>();
            int totalPoints = 0;
            for (Map.Entry<YearMonth, Integer> entry : monthlyTotals.entrySet()) {
                monthlyRewards.add(new MonthlyReward(entry.getKey().toString(), entry.getValue()));
                totalPoints = Math.addExact(totalPoints, entry.getValue());
            }
            response.add(new CustomerRewardSummary(customerId, List.copyOf(monthlyRewards), totalPoints));
        });
        return List.copyOf(response);
    }

    
    public int calculatePoints(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Transaction amount must be non-negative.");
        }
        BigDecimal wholeDollars = amount.setScale(0, RoundingMode.DOWN);
        if (wholeDollars.compareTo(FIFTY) <= 0) {
            return 0;
        }
        if (wholeDollars.compareTo(ONE_HUNDRED) <= 0) {
            return wholeDollars.subtract(FIFTY).intValueExact();
        }
        int pointsInFirstTier = 50;
        int pointsAboveOneHundred = wholeDollars.subtract(ONE_HUNDRED).intValueExact() * 2;
        return Math.addExact(pointsInFirstTier, pointsAboveOneHundred);
    }
}
