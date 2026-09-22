from __future__ import annotations

from dataclasses import dataclass
from typing import Any

import numpy as np
import pandas as pd
import yfinance as yf


@dataclass
class BacktestConfig:
    initial_capital: float = 100000.0
    transaction_cost_bps: float = 10.0
    fast_window: int = 20
    slow_window: int = 50


def _normalize_price_data(data: pd.DataFrame) -> pd.DataFrame:
    if data is None or data.empty:
        raise ValueError("No market data returned")

    df = data.copy()

    if isinstance(df.columns, pd.MultiIndex):
        if "Close" in df.columns.get_level_values(0):
            df = df.xs("Close", axis=1, level=0)
        elif "Close" in df.columns.get_level_values(1):
            df = df.xs("Close", axis=1, level=1)

        if isinstance(df, pd.DataFrame):
            if df.shape[1] == 1:
                df = df.iloc[:, 0].to_frame("Close")
            else:
                df = df.iloc[:, 0].to_frame("Close")

    if "Close" not in df.columns:
        raise ValueError("Market data does not contain Close prices")

    df = df[["Close"]].copy()
    df["Close"] = pd.to_numeric(df["Close"], errors="coerce")
    df = df.dropna()

    if df.empty:
        raise ValueError("No valid closing prices available")

    df.index = pd.to_datetime(df.index)
    df = df.sort_index()
    df = df[~df.index.duplicated(keep="last")]

    return df


def download_backtest_data(
    symbol: str,
    period: str = "5y"
) -> pd.DataFrame:

    symbol = symbol.strip().upper()

    if not symbol:
        raise ValueError("Symbol is required")

    data = yf.download(
        symbol,
        period=period,
        auto_adjust=True,
        progress=False
    )

    return _normalize_price_data(data)


def calculate_metrics(
    equity_curve: pd.Series,
    daily_returns: pd.Series,
    trade_count: int,
    total_transaction_costs: float,
    initial_capital: float
) -> dict[str, Any]:

    if equity_curve.empty:
        raise ValueError("Equity curve is empty")

    final_value = float(equity_curve.iloc[-1])

    total_return = (
        final_value / initial_capital
    ) - 1.0

    number_of_days = max(
        (equity_curve.index[-1] - equity_curve.index[0]).days,
        1
    )

    years = number_of_days / 365.25

    if years > 0 and final_value > 0:
        cagr = (
            final_value / initial_capital
        ) ** (1.0 / years) - 1.0
    else:
        cagr = 0.0

    annualized_volatility = (
        float(daily_returns.std(ddof=1)) * np.sqrt(252)
        if len(daily_returns) > 1
        else 0.0
    )

    mean_daily_return = (
        float(daily_returns.mean())
        if not daily_returns.empty
        else 0.0
    )

    daily_std = (
        float(daily_returns.std(ddof=1))
        if len(daily_returns) > 1
        else 0.0
    )

    if daily_std > 0:
        sharpe_ratio = (
            mean_daily_return / daily_std
        ) * np.sqrt(252)
    else:
        sharpe_ratio = 0.0

    running_max = equity_curve.cummax()

    drawdown = (
        equity_curve / running_max
    ) - 1.0

    max_drawdown = float(drawdown.min())

    positive_days = int(
        (daily_returns > 0).sum()
    )

    negative_days = int(
        (daily_returns < 0).sum()
    )

    total_days = positive_days + negative_days

    win_rate = (
        positive_days / total_days
        if total_days > 0
        else 0.0
    )

    return {
        "initial_capital": round(initial_capital, 2),
        "final_value": round(final_value, 2),
        "total_return": round(total_return, 6),
        "total_return_percent": round(total_return * 100, 4),
        "cagr": round(cagr, 6),
        "cagr_percent": round(cagr * 100, 4),
        "annualized_volatility": round(
            annualized_volatility,
            6
        ),
        "annualized_volatility_percent": round(
            annualized_volatility * 100,
            4
        ),
        "sharpe_ratio": round(
            sharpe_ratio,
            4
        ),
        "max_drawdown": round(
            max_drawdown,
            6
        ),
        "max_drawdown_percent": round(
            max_drawdown * 100,
            4
        ),
        "win_rate": round(
            win_rate,
            6
        ),
        "win_rate_percent": round(
            win_rate * 100,
            4
        ),
        "trade_count": trade_count,
        "total_transaction_costs": round(
            total_transaction_costs,
            2
        )
    }


def run_advanced_backtest(
    symbol: str,
    period: str = "5y",
    config: BacktestConfig | None = None
) -> dict[str, Any]:

    if config is None:
        config = BacktestConfig()

    if config.initial_capital <= 0:
        raise ValueError(
            "Initial capital must be greater than zero"
        )

    if config.transaction_cost_bps < 0:
        raise ValueError(
            "Transaction cost cannot be negative"
        )

    if config.fast_window <= 0:
        raise ValueError(
            "Fast moving-average window must be positive"
        )

    if config.slow_window <= config.fast_window:
        raise ValueError(
            "Slow window must be greater than fast window"
        )

    df = download_backtest_data(
        symbol,
        period
    )

    if len(df) < config.slow_window + 5:
        raise ValueError(
            "Not enough historical data for the selected windows"
        )

    df["fast_sma"] = (
        df["Close"]
        .rolling(config.fast_window)
        .mean()
    )

    df["slow_sma"] = (
        df["Close"]
        .rolling(config.slow_window)
        .mean()
    )

    df = df.dropna().copy()

    df["signal"] = (
        df["fast_sma"] > df["slow_sma"]
    ).astype(int)

    # Execute today's signal from the next trading day.
    # This prevents look-ahead bias.
    df["position"] = (
        df["signal"].shift(1).fillna(0)
    )

    df["market_return"] = (
        df["Close"].pct_change().fillna(0.0)
    )

    df["position_change"] = (
        df["position"].diff().abs().fillna(
            df["position"].abs()
        )
    )

    transaction_cost_rate = (
        config.transaction_cost_bps / 10000.0
    )

    df["transaction_cost"] = (
        df["position_change"]
        * transaction_cost_rate
    )

    df["strategy_return"] = (
        df["position"] * df["market_return"]
        - df["transaction_cost"]
    )

    df["strategy_growth"] = (
        1.0 + df["strategy_return"]
    )

    df["benchmark_growth"] = (
        1.0 + df["market_return"]
    )

    df["equity"] = (
        config.initial_capital
        * df["strategy_growth"].cumprod()
    )

    df["benchmark_equity"] = (
        config.initial_capital
        * df["benchmark_growth"].cumprod()
    )

    trade_count = int(
        df["position_change"].sum()
    )

    total_transaction_costs = (
        config.initial_capital
        * float(
            df["transaction_cost"].sum()
        )
    )

    metrics = calculate_metrics(
        equity_curve=df["equity"],
        daily_returns=df["strategy_return"],
        trade_count=trade_count,
        total_transaction_costs=total_transaction_costs,
        initial_capital=config.initial_capital
    )

    benchmark_final_value = float(
        df["benchmark_equity"].iloc[-1]
    )

    benchmark_return = (
        benchmark_final_value
        / config.initial_capital
    ) - 1.0

    strategy_return = (
        float(df["equity"].iloc[-1])
        / config.initial_capital
    ) - 1.0

    benchmark_difference = (
        strategy_return - benchmark_return
    )

    equity_curve = []

    for index, row in df.iterrows():
        equity_curve.append({
            "date": index.strftime("%Y-%m-%d"),
            "equity": round(
                float(row["equity"]),
                2
            ),
            "benchmark_equity": round(
                float(row["benchmark_equity"]),
                2
            ),
            "strategy_return": round(
                float(row["strategy_return"]),
                8
            ),
            "position": int(
                row["position"]
            )
        })

    return {
        "symbol": symbol.upper(),
        "period": period,
        "strategy": {
            "name": "SMA Crossover",
            "fast_window": config.fast_window,
            "slow_window": config.slow_window,
            "transaction_cost_bps": (
                config.transaction_cost_bps
            )
        },
        "metrics": metrics,
        "benchmark": {
            "name": "Buy and Hold",
            "final_value": round(
                benchmark_final_value,
                2
            ),
            "total_return": round(
                benchmark_return,
                6
            ),
            "total_return_percent": round(
                benchmark_return * 100,
                4
            )
        },
        "comparison": {
            "strategy_minus_benchmark": round(
                benchmark_difference,
                6
            ),
            "strategy_minus_benchmark_percent": round(
                benchmark_difference * 100,
                4
            ),
            "outperformed_benchmark": (
                strategy_return > benchmark_return
            )
        },
        "data": {
            "rows": len(df),
            "start_date": (
                df.index.min().strftime("%Y-%m-%d")
            ),
            "end_date": (
                df.index.max().strftime("%Y-%m-%d")
            )
        },
        "equity_curve": equity_curve
    }