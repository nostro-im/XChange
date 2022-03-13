package org.knowm.xchange.dto.account;

import org.knowm.xchange.currency.Currency;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import javax.annotation.Nullable;

/**
 * DTO representing account information
 *
 * <p>Account information is anything particular associated with the user's login
 */
public final class AccountInfo implements Serializable {

  private static final long serialVersionUID = -3572240060624800060L;

  /** The name on the account */
  private final String username;

  /**
   * The current fee this account must pay as a fraction of the value of each trade. Null if there
   * is no such fee.
   */
  private final BigDecimal tradingFee;

  /** The wallets owned by this account */
  private final Map<String, Wallet> wallets;

  /** The open positions owned by this account */
  private final Collection<OpenPosition> openPositions;

  /**
   * The timestamp at which this account information was generated. May be null if not provided by
   * the exchange.
   */
  @Nullable private final Date timestamp;

  /** margin info per currency **/
  private final Map<Currency, AccountMargin> margins;

  /** @see #AccountInfo(String, BigDecimal, Collection) */
  public AccountInfo(Wallet... wallets) {

    // TODO when refactoring for separate feature interfaces, change this constructor to require at
    // least two wallets
    this(null, null, wallets);
  }

  /** @see #AccountInfo(String, BigDecimal, Collection, Date) */
  public AccountInfo(Date timestamp, Wallet... wallets) {

    // TODO when refactoring for separate feature interfaces, change this constructor to require at
    // least two wallets
    this(null, null, Arrays.asList(wallets), timestamp);
  }

  /** @see #AccountInfo(String, BigDecimal, Collection) */
  public AccountInfo(Collection<Wallet> wallets) {

    this(null, null, wallets);
  }

  /** @see #AccountInfo(String, BigDecimal, Collection) */
  public AccountInfo(String username, Wallet... wallets) {

    this(username, null, wallets);
  }

  /** @see #AccountInfo(String, BigDecimal, Collection) */
  public AccountInfo(String username, Collection<Wallet> wallets) {

    this(username, null, wallets);
  }

  /**
   * Constructs an {@link AccountInfo}.
   *
   * @param username the user name.
   * @param tradingFee the trading fee.
   * @param wallets the user's wallets
   */
  public AccountInfo(String username, BigDecimal tradingFee, Collection<Wallet> wallets) {
    this(username, tradingFee, wallets, null);
  }

  /**
   * Constructs an {@link AccountInfo}.
   *
   * @param username the user name.
   * @param tradingFee the trading fee.
   * @param wallets the user's wallets
   * @param timestamp the timestamp for the account snapshot.
   */
  public AccountInfo(
      String username, BigDecimal tradingFee, Collection<Wallet> wallets, Date timestamp) {

    this(username, tradingFee, wallets, Collections.emptySet(), Collections.emptySet(), timestamp);
  }

  /**
   * Constructs an {@link AccountInfo}.
   *
   * @param username the user name.
   * @param tradingFee the trading fee.
   * @param wallets the user's wallets
   * @param openPositions the users's open positions
   * @param margins account margin info collection
   * @param timestamp the timestamp for the account snapshot.
   */
  public AccountInfo(
          String username,
          BigDecimal tradingFee,
          Collection<Wallet> wallets,
          Collection<OpenPosition> openPositions,
          Collection<AccountMargin> margins,
          Date timestamp) {

    this.username = username;
    this.tradingFee = tradingFee;
    this.timestamp = timestamp;
    this.openPositions = openPositions;

    if (wallets == null || wallets.size() == 0) {
      this.wallets = Collections.emptyMap();
    } else if (wallets.size() == 1) {
      Wallet wallet = wallets.iterator().next();
      this.wallets = Collections.singletonMap(wallet.getId(), wallet);
    } else {
      this.wallets = new HashMap<>();
      for (Wallet wallet : wallets) {
        if (this.wallets.containsKey(wallet.getId())) {
          throw new IllegalArgumentException("duplicate wallets passed to AccountInfo");
        }
        this.wallets.put(wallet.getId(), wallet);
      }
    }

    if (margins == null || margins.size() == 0) {
      this.margins = Collections.emptyMap();
    } else if (margins.size() == 1) {
      AccountMargin margin = margins.iterator().next();
      this.margins = Collections.singletonMap(margin.getCurrency(), margin);
    } else {
      this.margins = new HashMap<>();
      for (AccountMargin margin : margins) {
        this.margins.put(margin.getCurrency(), margin);
      }
    }
  }

  /** @see #AccountInfo(String, BigDecimal, Collection) */
  public AccountInfo(String username, BigDecimal tradingFee, Wallet... wallets) {

    this(username, tradingFee, Arrays.asList(wallets));
  }

  /** Gets all wallets in this account */
  public Map<String, Wallet> getWallets() {

    return Collections.unmodifiableMap(wallets);
  }

  /** Gets wallet for accounts which don't use multiple wallets with ids */
  public Wallet getWallet() {

    if (wallets.size() != 1) {
      throw new UnsupportedOperationException(wallets.size() + " wallets in account");
    }

    return getWallet(wallets.keySet().iterator().next());
  }

  /** Gets the wallet with a specific id */
  public Wallet getWallet(String id) {

    return wallets.get(id);
  }

  /**
   * Get wallet with given feature
   *
   * @return null if no wallet on given exchange supports this feature
   * @throws UnsupportedOperationException if there are more then one wallets supporting the given
   *     feature
   */
  public Wallet getWallet(Wallet.WalletFeature feature) {
    List<Wallet> walletWithFeatures = new ArrayList<>();

    wallets.forEach(
        (s, wallet) -> {
          if (wallet.getFeatures() != null) {
            if (wallet.getFeatures().contains(feature)) {
              walletWithFeatures.add(wallet);
            }
          }
        });

    if (walletWithFeatures.size() > 1) {
      throw new UnsupportedOperationException("More than one wallet offer this feature.");
    } else if (walletWithFeatures.size() == 0) {
      return null;
    }
    return walletWithFeatures.get(0);
  }

  /** @return The user name */
  public String getUsername() {

    return username;
  }

  /**
   * Returns the current trading fee
   *
   * @return The trading fee
   */
  public BigDecimal getTradingFee() {

    return tradingFee;
  }

  /**
   * @return The timestamp at which this account information was generated. May be null if not
   *     provided by the exchange.
   */
  public Date getTimestamp() {
    return timestamp;
  }

  public Collection<OpenPosition> getOpenPositions() {
    return openPositions;
  }

  public Optional<AccountMargin> getAccountMargin(Currency currency) {
    return Optional.ofNullable(margins.get(currency));
  }

  public Optional<AccountMargin> getAccountMargin() {
    return margins.values().stream().findFirst();
  }

  public Map<Currency, AccountMargin> getAccountMargins() {
    return margins;
  }

  @Override
  public String toString() {

    return "AccountInfo [username="
        + username
        + ", tradingFee="
        + tradingFee
        + ", wallets="
        + wallets
        + ", openPositions="
        + openPositions
        + ", margins="
        + margins
        + "]";
  }

  public static final class Builder {
    private String username;
    private BigDecimal tradingFee;
    private Collection<Wallet> wallets;
    private Collection<OpenPosition> openPositions;
    private Date timestamp;
    private Collection<AccountMargin> margins;

    private Builder() {}

    public static Builder from(Collection<Wallet> wallets) {
      return new Builder().wallets(wallets);
    }

    public Builder username(String username) {
      this.username = username;
      return this;
    }

    public Builder tradingFee(BigDecimal tradingFee) {
      this.tradingFee = tradingFee;
      return this;
    }

    public Builder wallets(Collection<Wallet> wallets) {
      this.wallets = wallets;
      return this;
    }

    public Builder openPositions(Collection<OpenPosition> openPositions) {
      this.openPositions = openPositions;
      return this;
    }

    public Builder timestamp(Date timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public Builder margins(Collection<AccountMargin> margins) {
      this.margins = margins;
      return this;
    }

    public AccountInfo build() {
      return new AccountInfo(username, tradingFee, wallets, openPositions, margins, timestamp);
    }
  }
}
