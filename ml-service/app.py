from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from pipeline import (
    download_market_data,
    prepare_market_features,
    FEATURE_COLUMNS
)

from models import (
    walk_forward_xgboost,
    train_xgboost_prediction
)

from advanced_backtest import (
    BacktestConfig,
    run_advanced_backtest
)


app = FastAPI(
    title="WhereIsMyMoney ML Service",
    version="1.0.0"
)


class PredictionRequest(BaseModel):

    symbol: str

    period: str = "5y"

    horizon: int = Field(
        default=1,
        ge=1,
        le=30
    )


class AdvancedBacktestRequest(BaseModel):

    symbol: str

    period: str = "5y"

    initial_capital: float = Field(
        default=100000.0,
        gt=0
    )

    transaction_cost_bps: float = Field(
        default=10.0,
        ge=0
    )

    fast_window: int = Field(
        default=20,
        ge=2
    )

    slow_window: int = Field(
        default=50,
        ge=3
    )


@app.get("/health")
def health():

    return {
        "status": "UP",
        "service": "whereismymoney-ml"
    }


@app.get("/market/{symbol}")
def market_data(symbol: str):

    try:

        df = download_market_data(
            symbol.upper(),
            "5y"
        )

        return {
            "symbol": symbol.upper(),
            "rows": len(df),
            "data": df.reset_index().to_dict(
                orient="records"
            )
        }

    except Exception as exc:

        raise HTTPException(
            status_code=400,
            detail=str(exc)
        )


@app.get("/features/{symbol}")
def features(symbol: str):

    try:

        market_df = download_market_data(
            symbol.upper(),
            "5y"
        )

        feature_df = prepare_market_features(
            market_df,
            horizon=1
        )

        available_columns = [
            column
            for column in FEATURE_COLUMNS
            if column in feature_df.columns
        ]

        return {
            "symbol": symbol.upper(),
            "rows": len(feature_df),
            "features": available_columns,
            "data": feature_df[
                available_columns
            ].tail(100).reset_index(
                drop=True
            ).to_dict(
                orient="records"
            )
        }

    except Exception as exc:

        raise HTTPException(
            status_code=400,
            detail=str(exc)
        )


@app.post("/predict")
def predict(request: PredictionRequest):

    try:

        market_df = download_market_data(
            request.symbol.upper(),
            request.period
        )

        feature_df = prepare_market_features(
            market_df,
            horizon=request.horizon
        )

        result = train_xgboost_prediction(
            feature_df
        )

        return {
            "symbol": request.symbol.upper(),
            **result
        }

    except Exception as exc:

        raise HTTPException(
            status_code=400,
            detail=str(exc)
        )


@app.get("/walk-forward/{symbol}")
def walk_forward(symbol: str):

    try:

        market_df = download_market_data(
            symbol.upper(),
            "5y"
        )

        feature_df = prepare_market_features(
            market_df,
            horizon=1
        )

        result = walk_forward_xgboost(
            feature_df
        )

        return {
            "symbol": symbol.upper(),
            **result
        }

    except Exception as exc:

        raise HTTPException(
            status_code=400,
            detail=str(exc)
        )


@app.post("/advanced-backtest")
def advanced_backtest(
    request: AdvancedBacktestRequest
):

    try:

        if request.slow_window <= request.fast_window:

            raise HTTPException(
                status_code=400,
                detail=(
                    "slow_window must be greater "
                    "than fast_window"
                )
            )

        config = BacktestConfig(
            initial_capital=request.initial_capital,
            transaction_cost_bps=(
                request.transaction_cost_bps
            ),
            fast_window=request.fast_window,
            slow_window=request.slow_window
        )

        result = run_advanced_backtest(
            symbol=request.symbol.upper(),
            period=request.period,
            config=config
        )

        return result

    except HTTPException:
        raise

    except Exception as exc:

        raise HTTPException(
            status_code=400,
            detail=str(exc)
        )