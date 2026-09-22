from __future__ import annotations

import numpy as np
import pandas as pd

from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score
)

from xgboost import XGBClassifier
import shap

from pipeline import FEATURE_COLUMNS


def walk_forward_xgboost(
    df: pd.DataFrame,
    minimum_train_size: int = 252
) -> dict:

    if len(df) <= minimum_train_size:
        raise ValueError(
            "Not enough historical data for walk-forward validation."
        )

    predictions = []
    actuals = []
    prediction_dates = []

    for index in range(
        minimum_train_size,
        len(df)
    ):

        train = df.iloc[:index]
        test = df.iloc[index:index + 1]

        X_train = train[FEATURE_COLUMNS]
        y_train = train["target"]

        X_test = test[FEATURE_COLUMNS]
        y_test = test["target"]

        model = XGBClassifier(
            n_estimators=200,
            max_depth=4,
            learning_rate=0.05,
            subsample=0.8,
            colsample_bytree=0.8,
            objective="binary:logistic",
            eval_metric="logloss",
            random_state=42
        )

        model.fit(
            X_train,
            y_train
        )

        prediction = int(
            model.predict(X_test)[0]
        )

        predictions.append(
            prediction
        )

        actuals.append(
            int(y_test.iloc[0])
        )

        if "date" in test.columns:
            prediction_dates.append(
                str(test["date"].iloc[0])
            )

    accuracy = accuracy_score(
        actuals,
        predictions
    )

    precision = precision_score(
        actuals,
        predictions,
        zero_division=0
    )

    recall = recall_score(
        actuals,
        predictions,
        zero_division=0
    )

    f1 = f1_score(
        actuals,
        predictions,
        zero_division=0
    )

    return {
        "samples": len(actuals),
        "accuracy": float(accuracy),
        "precision": float(precision),
        "recall": float(recall),
        "f1": float(f1),
        "predictions": predictions,
        "actuals": actuals,
        "prediction_dates": prediction_dates
    }


def train_xgboost_prediction(
    df: pd.DataFrame
) -> dict:

    if len(df) < 100:
        raise ValueError(
            "Not enough data to train XGBoost."
        )

    X = df[FEATURE_COLUMNS]
    y = df["target"]

    model = XGBClassifier(
        n_estimators=300,
        max_depth=4,
        learning_rate=0.05,
        subsample=0.8,
        colsample_bytree=0.8,
        objective="binary:logistic",
        eval_metric="logloss",
        random_state=42
    )

    model.fit(
        X,
        y
    )

    latest_features = X.iloc[[-1]]

    probability = float(
        model.predict_proba(
            latest_features
        )[0][1]
    )

    prediction = int(
        probability >= 0.5
    )

    explainer = shap.TreeExplainer(
        model
    )

    shap_values = explainer(
        latest_features
    )

    values = shap_values.values

    if values.ndim == 2:
        feature_importance = values[0]
    else:
        feature_importance = values

    importance = []

    for feature, value in zip(
        FEATURE_COLUMNS,
        feature_importance
    ):
        importance.append(
            {
                "feature": feature,
                "shap_value": float(value)
            }
        )

    importance.sort(
        key=lambda item: abs(
            item["shap_value"]
        ),
        reverse=True
    )

    return {
        "prediction": prediction,
        "direction": (
            "UP"
            if prediction == 1
            else "DOWN"
        ),
        "probability_up": probability,
        "features": {
            column: float(
                latest_features.iloc[0][column]
            )
            for column in FEATURE_COLUMNS
        },
        "shap_importance": importance
    }