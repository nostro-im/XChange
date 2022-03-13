package org.knowm.xchange.dto.account;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.knowm.xchange.currency.Currency;

import java.math.BigDecimal;
import java.util.Objects;

@JsonDeserialize(builder = AccountMargin.Builder.class)
public class AccountMargin {
    /** margin currency **/
    private final Currency currency;
    /** Current margin balance */
    private final BigDecimal marginBalance;
    /** Current PNL */
    private final BigDecimal unrealizedProfit;
    /** free collateral */
    private final BigDecimal freeCollateral;
    /** account max leverage */
    private final BigDecimal leverage;
    /** current leverage: total position value / total account value */
    private final BigDecimal currentLeverage;

    public AccountMargin(Currency currency, BigDecimal marginBalance, BigDecimal unrealizedProfit, BigDecimal freeCollateral, BigDecimal leverage, BigDecimal currentLeverage) {
        this.currency = currency;
        this.marginBalance = marginBalance;
        this.unrealizedProfit = unrealizedProfit;
        this.freeCollateral = freeCollateral;
        this.leverage = leverage;
        this.currentLeverage = currentLeverage;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getMarginBalance() {
        return marginBalance;
    }

    public BigDecimal getUnrealizedProfit() {
        return unrealizedProfit;
    }

    public BigDecimal getFreeCollateral() {
        return freeCollateral;
    }

    public BigDecimal getLeverage() {
        return leverage;
    }

    public BigDecimal getCurrentLeverage() {
        return currentLeverage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountMargin that = (AccountMargin) o;
        return Objects.equals(currency, that.currency) &&
                Objects.equals(marginBalance, that.marginBalance) &&
                Objects.equals(unrealizedProfit, that.unrealizedProfit) &&
                Objects.equals(freeCollateral, that.freeCollateral) &&
                Objects.equals(leverage, that.leverage) &&
                Objects.equals(currentLeverage, that.currentLeverage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, marginBalance, unrealizedProfit, freeCollateral, leverage, currentLeverage);
    }

    @Override
    public String toString() {
        return "AccountMargin{" +
                "currency=" + currency +
                ", marginBalance=" + marginBalance +
                ", unrealizedProfit=" + unrealizedProfit +
                ", freeCollateral=" + freeCollateral +
                ", leverage=" + leverage +
                ", currentLeverage=" + currentLeverage +
                '}';
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private Currency currency;
        private BigDecimal marginBalance;
        private BigDecimal unrealizedProfit;
        private BigDecimal freeCollateral;
        private BigDecimal leverage;
        private BigDecimal currentLeverage;

        public Builder() {}

        public Builder currency(Currency currency) {
            this.currency = currency;
            return this;
        }

        public Builder marginBalance(BigDecimal marginBalance) {
            this.marginBalance = marginBalance;
            return this;
        }

        public Builder unrealizedProfit(BigDecimal unrealizedProfit) {
            this.unrealizedProfit = unrealizedProfit;
            return this;
        }

        public Builder freeCollateral(BigDecimal freeCollateral) {
            this.freeCollateral = freeCollateral;
            return this;
        }

        public Builder leverage(BigDecimal leverage) {
            this.leverage = leverage;
            return this;
        }

        public Builder currentLeverage(BigDecimal currentLeverage) {
            this.currentLeverage = currentLeverage;
            return this;
        }

        public AccountMargin build() {
            return new AccountMargin(currency, marginBalance, unrealizedProfit, freeCollateral, leverage, currentLeverage);
        }
    }
}
