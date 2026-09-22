from __future__ import annotations

import numpy as np
import pandas as pd
import yfinance as yf


FEATURE_COLUMNS = [
    "return_1d",
    "return_5d",
    "return_20d",
    "sma_5_ratio",
    "sma_20_ratio",
    "sma_50_ratio",
    "volatility_20",
    "rsi_14",
    "volume_ratio",
]


def download_market_data(
    symbol: str,
    period: str = "5y"
) -> pd.DataFrame:

    ticker = yf.Ticker(symbol)

    data = ticker.history(
        period=period,
        auto_adjust=True
    )

    if data.empty:
        raise ValueError(
            f"No market data found for symbol: {symbol}"
        )

    data = data.reset_index()

    data.columns = [
        str(column).lower().replace(" ", "_")
        for column in data.columns
    ]

    return data


def calculate_rsi(
    series: pd.Series,
    period: int = 14
) -> pd.Series:

    delta = series.diff()

    gain = delta.clip(lower=0)
    loss = -delta.clip(upper=0)

    average_gain = gain.rolling(
        period
    ).mean()

    average_loss = loss.rolling(
        period
    ).mean()

    rs = average_gain / average_loss.replace(
        0,
        np.nan
    )

    rsi = 100 - (
        100 / (1 + rs)
    )

    return rsi


def create_features(
    data: pd.DataFrame,
    horizon: int = 1
) -> pd.DataFrame:

    df = data.copy()

    df["return_1d"] = (
        df["close"].pct_change(1)
    )

    df["return_5d"] = (
        df["close"].pct_change(5)
    )

    df["return_20d"] = (
        df["close"].pct_change(20)
    )

    df["sma_5"] = (
        df["close"].rolling(5).mean()
    )

    df["sma_20"] = (
        df["close"].rolling(20).mean()
    )

    df["sma_50"] = (
        df["close"].rolling(50).mean()
    )

    df["sma_5_ratio"] = (
        df["close"] / df["sma_5"]
    )

    df["sma_20_ratio"] = (
        df["close"] / df["sma_20"]
    )

    df["sma_50_ratio"] = (
        df["close"] / df["sma_50"]
    )

    df["volatility_20"] = (
        df["return_1d"]
        .rolling(20)
        .std()
    )

    df["rsi_14"] = calculate_rsi(
        df["close"],
        14
    )

    df["volume_sma_20"] = (
        df["volume"]
        .rolling(20)
        .mean()
    )

    df["volume_ratio"] = (
        df["volume"]
        / df["volume_sma_20"]
    )

    future_return = (
        df["close"]
        .shift(-horizon)
        / df["close"]
        - 1
    )

    df["target"] = (
        future_return > 0
    ).astype(int)

    df = df.dropna(
        subset=FEATURE_COLUMNS
    )

    return df


def prepare_market_features(
    symbol: str,
    period: str = "5y",
    horizon: int = 1
) -> pd.DataFrame:

    data = download_market_data(
        symbol,
        period
    )

    return create_features(
        data,
        horizon
    )