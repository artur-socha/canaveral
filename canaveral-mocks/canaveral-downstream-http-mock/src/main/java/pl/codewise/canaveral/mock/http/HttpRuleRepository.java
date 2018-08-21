package pl.codewise.canaveral.mock.http;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class HttpRuleRepository implements HttpRuleCreator {

    private final List<MockRule> defaultRules;
    private volatile List<MockRule> rules = new CopyOnWriteArrayList<>();

    HttpRuleRepository(List<MockRule> defaults) {
        defaultRules = ImmutableList.copyOf(defaults);
        resetToDefault();
    }

    @Override
    public void addRule(HttpRequestRule request, HttpResponseRule response) {
        rules.add(0, MockRule.create(request, response));
    }

    public HttpRuleRepository removeRule(HttpRequestRule request) {
        rules.removeIf(mockRule -> mockRule.getRequest().equals(request));
        return this;
    }

    public void clear() {
        rules.clear();
    }

    void resetToDefault() {
        rules = new CopyOnWriteArrayList<>(defaultRules);
    }

    Optional<MockRule> findRule(HttpRawRequest request) {
        return rules.stream().filter(rule -> rule.getCondition().test(request)).findFirst();
    }
}
