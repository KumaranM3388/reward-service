package com.example.rewards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.rewards.exception.InvalidDateRangeException;
import com.example.rewards.model.CustomerRewardSummary;
import com.example.rewards.model.TransactionRecord;
import com.example.rewards.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RewardCalculationServiceTest {
    @Mock private TransactionRepository transactionRepository;
    private RewardCalculationService service;

    /** Creates a fresh service for each test. */
    @BeforeEach
    void setUp() { service = new RewardCalculationService(transactionRepository); }

    /** Verifies reward boundaries and the example transaction. */
    @Test
    void calculatesTierBoundaries() {
        assertEquals(0, service.calculatePoints(new BigDecimal("0")));
        assertEquals(0, service.calculatePoints(new BigDecimal("50")));
        assertEquals(1, service.calculatePoints(new BigDecimal("51")));
        assertEquals(50, service.calculatePoints(new BigDecimal("100")));
        assertEquals(52, service.calculatePoints(new BigDecimal("101")));
        assertEquals(90, service.calculatePoints(new BigDecimal("120")));
        assertEquals(250, service.calculatePoints(new BigDecimal("200")));
    }

    /** Verifies that fractional dollars do not earn partial points. */
    @Test
    void ignoresFractionalDollars() {
        assertEquals(25, service.calculatePoints(new BigDecimal("75.99")));
        assertEquals(90, service.calculatePoints(new BigDecimal("120.99")));
    }

    /** Verifies negative and null amounts are rejected. */
    @Test
    void rejectsInvalidAmounts() {
        assertThrows(IllegalArgumentException.class, () -> service.calculatePoints(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> service.calculatePoints(null));
    }

    /** Verifies aggregation across customers, months, and multiple transactions. */
    @Test
    void groupsTransactionsByCustomerAndMonth() {
        List<TransactionRecord> transactions = List.of(
                transaction(1, 1001, "2026-01-05", "120"),
                transaction(2, 1001, "2026-01-20", "75"),
                transaction(3, 1001, "2026-02-10", "200"),
                transaction(4, 1002, "2026-01-15", "150"),
                transaction(5, 1002, "2026-02-04", "99"));
        LocalDate start = LocalDate.parse("2026-01-01");
        LocalDate end = LocalDate.parse("2026-02-28");
        when(transactionRepository.findTransactions(start, end)).thenReturn(transactions);

        List<CustomerRewardSummary> result = service.calculateRewards(start, end);

        assertEquals(2, result.size());
        assertEquals(1001, result.get(0).customerId());
        assertEquals(115, result.get(0).monthlyRewards().get(0).points());
        assertEquals("2026-02", result.get(0).monthlyRewards().get(1).month());
        assertEquals(250, result.get(0).monthlyRewards().get(1).points());
        assertEquals(365, result.get(0).totalPoints());
        assertEquals(1002, result.get(1).customerId());
        assertEquals(199, result.get(1).totalPoints());
        verify(transactionRepository).findTransactions(start, end);
    }

    /** Verifies reversed date ranges are rejected. */
    @Test
    void rejectsReversedDateRange() {
        assertThrows(InvalidDateRangeException.class, () -> service.calculateRewards(
                LocalDate.parse("2026-03-01"), LocalDate.parse("2026-01-01")));
    }

    private TransactionRecord transaction(long id, long customerId, String date, String amount) {
        return new TransactionRecord(id, customerId, LocalDate.parse(date), new BigDecimal(amount));
    }
}
