package pl.codewise.canaveral.mock.http;

interface HttpRuleCreator {

    void addRule(HttpRequestRule request, HttpResponseRule response);
}
