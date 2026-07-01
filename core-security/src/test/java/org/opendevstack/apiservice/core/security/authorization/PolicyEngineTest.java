package org.opendevstack.apiservice.core.security.authorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.core.contracts.auth.AuthorizationDecision;
import org.opendevstack.apiservice.core.contracts.policy.PolicyContext;
import org.opendevstack.apiservice.core.contracts.policy.PolicyEvaluator;
import org.opendevstack.apiservice.core.contracts.policy.PolicyRule;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PolicyEngineTest {

    private PolicyEvaluator evaluatorA;
    private PolicyEvaluator evaluatorB;
    private PolicyEngine engine;
    private PolicyContext context;

    @BeforeEach
    void setUp() {
        evaluatorA = mock(PolicyEvaluator.class);
        evaluatorB = mock(PolicyEvaluator.class);
        when(evaluatorA.supports("TYPE_A")).thenReturn(true);
        when(evaluatorB.supports("TYPE_B")).thenReturn(true);
        engine = new PolicyEngine(List.of(evaluatorA, evaluatorB));
        context = mock(PolicyContext.class);
        when(context.withRule(any())).thenReturn(context);
    }

    @Test
    void evaluate_emptyRules_returnsDeny() {
        assertEquals(AuthorizationDecision.DENY, engine.evaluate(context, Collections.emptyList()));
    }

    @Test
    void evaluate_nullRules_returnsDeny() {
        assertEquals(AuthorizationDecision.DENY, engine.evaluate(context, null));
    }

    @Test
    void evaluate_singleRulePermit_returnsPermit() {
        PolicyRule rule = rule("TYPE_A");
        when(evaluatorA.evaluate(context)).thenReturn(AuthorizationDecision.PERMIT);

        assertEquals(AuthorizationDecision.PERMIT, engine.evaluate(context, List.of(rule)));
    }

    @Test
    void evaluate_singleRuleDeny_returnsDeny() {
        PolicyRule rule = rule("TYPE_A");
        when(evaluatorA.evaluate(context)).thenReturn(AuthorizationDecision.DENY);

        assertEquals(AuthorizationDecision.DENY, engine.evaluate(context, List.of(rule)));
    }

    @Test
    void evaluate_twoRules_permitThenDeny_returnsDeny() {
        PolicyRule ruleA = rule("TYPE_A");
        PolicyRule ruleB = rule("TYPE_B");
        when(evaluatorA.evaluate(context)).thenReturn(AuthorizationDecision.PERMIT);
        when(evaluatorB.evaluate(context)).thenReturn(AuthorizationDecision.DENY);

        assertEquals(AuthorizationDecision.DENY, engine.evaluate(context, List.of(ruleA, ruleB)));
    }

    @Test
    void evaluate_twoRules_permitThenAbstain_returnsPermit() {
        PolicyRule ruleA = rule("TYPE_A");
        PolicyRule ruleB = rule("TYPE_B");
        when(evaluatorA.evaluate(context)).thenReturn(AuthorizationDecision.PERMIT);
        when(evaluatorB.evaluate(context)).thenReturn(AuthorizationDecision.ABSTAIN);

        assertEquals(AuthorizationDecision.PERMIT, engine.evaluate(context, List.of(ruleA, ruleB)));
    }

    @Test
    void evaluate_allAbstain_returnsPermit() {
        PolicyRule ruleA = rule("TYPE_A");
        PolicyRule ruleB = rule("TYPE_B");
        when(evaluatorA.evaluate(context)).thenReturn(AuthorizationDecision.ABSTAIN);
        when(evaluatorB.evaluate(context)).thenReturn(AuthorizationDecision.ABSTAIN);

        assertEquals(AuthorizationDecision.PERMIT, engine.evaluate(context, List.of(ruleA, ruleB)));
    }

    @Test
    void evaluate_ruleWithNoMatchingEvaluator_isSkipped() {
        PolicyRule unknownRule = rule("UNKNOWN_TYPE");

        // No evaluator supports UNKNOWN_TYPE → skipped → finalDecision stays ABSTAIN → PERMIT
        assertEquals(AuthorizationDecision.PERMIT, engine.evaluate(context, List.of(unknownRule)));
    }

    @Test
    void evaluate_denyShortCircuits_secondEvaluatorNotCalled() {
        PolicyRule ruleA = rule("TYPE_A");
        PolicyRule ruleB = rule("TYPE_B");
        when(evaluatorA.evaluate(context)).thenReturn(AuthorizationDecision.DENY);

        engine.evaluate(context, List.of(ruleA, ruleB));

        verify(evaluatorB, never()).evaluate(any());
    }

    private PolicyRule rule(String type) {
        return new PolicyRule(UUID.randomUUID(), "api-1", "client-1", type, Map.of());
    }
}
