package net.rafaelinfante.fleet.domain;

public enum ComparisonOperator {
    GT(">") {
        @Override
        public boolean test(double value, double threshold) {
            return value > threshold;
        }
    },
    GTE(">=") {
        @Override
        public boolean test(double value, double threshold) {
            return value >= threshold;
        }
    },
    LT("<") {
        @Override
        public boolean test(double value, double threshold) {
            return value < threshold;
        }
    },
    LTE("<=") {
        @Override
        public boolean test(double value, double threshold) {
            return value <= threshold;
        }
    };

    private final String symbol;

    ComparisonOperator(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public abstract boolean test(double value, double threshold);
}
