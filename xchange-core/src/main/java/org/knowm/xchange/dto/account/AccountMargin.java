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

    public AccountMargin(Currency currency, BigDecimal marginBalance, BigDecimal unrealizedProfit) {
        this.currency = currency;
        this.marginBalance = marginBalance;
        this.unrealizedProfit = unrealizedProfit;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountMargin that = (AccountMargin) o;
        return Objects.equals(currency, that.currency) &&
                Objects.equals(marginBalance, that.marginBalance) &&
                Objects.equals(unrealizedProfit, that.unrealizedProfit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, marginBalance, unrealizedProfit);
    }

    @Override
    public String toString() {
        return "AccountMargin{" +
                "currency=" + currency +
                ", marginBalance=" + marginBalance +
                ", unrealizedProfit=" + unrealizedProfit +
                '}';
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private Currency currency;
        private BigDecimal marginBalance;
        private BigDecimal unrealizedProfit;

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

        public AccountMargin build() {
            return new AccountMargin(currency, marginBalance, unrealizedProfit);
        }
    }
}
