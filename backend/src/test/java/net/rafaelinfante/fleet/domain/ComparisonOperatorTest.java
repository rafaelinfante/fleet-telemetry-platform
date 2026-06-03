package net.rafaelinfante.fleet.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ComparisonOperatorTest {

    @Test
    void evaluatesEachOperator() {
        assertThat(ComparisonOperator.GT.test(5, 3)).isTrue();
        assertThat(ComparisonOperator.GT.test(3, 3)).isFalse();
        assertThat(ComparisonOperator.GTE.test(3, 3)).isTrue();
        assertThat(ComparisonOperator.LT.test(2, 3)).isTrue();
        assertThat(ComparisonOperator.LTE.test(3, 3)).isTrue();
        assertThat(ComparisonOperator.LTE.test(4, 3)).isFalse();
    }

    @Test
    void exposesSymbol() {
        assertThat(ComparisonOperator.GTE.symbol()).isEqualTo(">=");
    }
}
