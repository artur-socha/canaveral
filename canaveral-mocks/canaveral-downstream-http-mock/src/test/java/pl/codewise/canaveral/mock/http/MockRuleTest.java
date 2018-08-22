package pl.codewise.canaveral.mock.http;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static pl.codewise.canaveral.mock.http.MockRule.create;

class MockRuleTest {

    private static final String PATH_PATTERN = "/login";
    private static final Map<String, List<String>> QUERY_PARAMS = ImmutableMap.of("A", of("1"));
    private static final Map<String, List<String>> HEADERS = ImmutableMap.of("H", of("text"));
    private static final String BODY = "The body";

    private static final HttpRequestRule REQUEST = httpRequestRule(PATH_PATTERN);

    @Mock
    private HttpResponseRule responseRule;

    @Mock
    private HttpRawRequest inRequest;

    private MockRule mockRule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(inRequest.getMethod()).thenReturn(Method.GET);
        when(inRequest.getPath()).thenReturn(PATH_PATTERN);
        when(inRequest.getHeaders()).thenReturn(emptyMap());
        when(inRequest.getQueryParams()).thenReturn(emptyMap());

        mockRule = create(REQUEST, responseRule);
    }

    @Test
    void shouldProvideRequestAndResponse() {
        // then
        assertThat(mockRule.getRequest()).isEqualTo(REQUEST);
        assertThat(mockRule.getResponse()).isEqualTo(responseRule);
    }

    @ParameterizedTest
    @MethodSource("simplePaths")
    void shouldMatchOnSimplePath(String inPath, boolean shouldMatch) {
        // given
        when(inRequest.getPath()).thenReturn(inPath);

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isEqualTo(shouldMatch);
    }

    @ParameterizedTest
    @MethodSource("pathPatterns")
    void shouldMatchOnPathPattern(String inPath, boolean shouldMatch) {
        // given
        mockRule = create(httpRequestRule("/login.*/user/.*"), responseRule);

        when(inRequest.getPath()).thenReturn(inPath);

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isEqualTo(shouldMatch);
    }

    @Test
    void shouldMatchOnMissingHeaders() {
        // given
        when(inRequest.getHeaders()).thenReturn(null);

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldMatchOnHeadersWhenRuleHeadersAreEmpty() {
        // given
        when(inRequest.getHeaders()).thenReturn(HEADERS);

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldMatchOnQueryParamsWhenRuleParamsAreEmpty() {
        // given
        when(inRequest.getQueryParams()).thenReturn(QUERY_PARAMS);

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isTrue();
    }

    @ParameterizedTest
    @MethodSource("headers")
    void shouldMatchOnSimpleHeaders(Map<String, List<String>> headers, boolean match) {
        // given
        HttpRequestRule requestRule = httpRequestRule(emptyMap(), HEADERS);
        MockRule mockRule = create(requestRule, responseRule);

        when(inRequest.getHeaders()).thenReturn(headers);

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isEqualTo(match);
    }

    @Test
    void shouldMatchOnMultipleHeaders() {
        // given
        when(inRequest.getHeaders()).thenReturn(ImmutableMap.of(
                "A", of("1", "2"),
                "B", of("3", "4")));

        ImmutableMap<String, List<String>> ruleHeaders = ImmutableMap.of(
                "A", of("1", "2"),
                "B", of("3"));
        HttpRequestRule requestRule = httpRequestRule(emptyMap(), ruleHeaders);
        MockRule mockRule = create(requestRule, responseRule);

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isTrue();
    }

    @ParameterizedTest
    @MethodSource("queryParams")
    void shouldMatchOnSimpleQueryParams(Map<String, List<String>> queryParams, boolean match) {
        // given
        mockRule = create(httpRequestRule(QUERY_PARAMS, emptyMap()), responseRule);

        when(inRequest.getQueryParams()).thenReturn(queryParams);

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isEqualTo(match);
    }

    @Test
    void shouldMatchOnMultipleQueryParams() {
        // given
        when(inRequest.getQueryParams()).thenReturn(ImmutableMap.of(
                "A", of("1", "2"),
                "B", of("3", "4")));

        ImmutableMap<String, List<String>> ruleParams = ImmutableMap.of(
                "A", of("1", "2"),
                "B", of("3"));
        HttpRequestRule requestRule = httpRequestRule(ruleParams, emptyMap());
        MockRule mockRule = create(requestRule, responseRule);

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isTrue();
    }

    @ParameterizedTest
    @MethodSource("bodies")
    void shouldMatchOnBody(String body, boolean match) {
        // given
        HttpRequestRule requestRule = new HttpRequestRule(Method.POST, PATH_PATTERN, emptyMap(), emptyMap(), BODY
                .getBytes());
        MockRule mockRule = create(requestRule, responseRule);

        when(inRequest.getMethod()).thenReturn(Method.POST);
        when(inRequest.getBody()).thenReturn(body.getBytes());

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isEqualTo(match);
    }

    @Test
    void shouldFailOnMatchingDifferentMethods() {
        // given
        when(inRequest.getMethod()).thenReturn(Method.POST);

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isFalse();
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> simplePaths() {
        return Stream.of(
                Arguments.of(PATH_PATTERN, true),
                Arguments.of("/login/a", false),
                Arguments.of("/a/login", false)
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> pathPatterns() {
        return Stream.of(
                Arguments.of(PATH_PATTERN + "/sys/user/kevin", true),
                Arguments.of(PATH_PATTERN + "/user/", true),
                Arguments.of("/a/login/sys/user/kevin", false),
                Arguments.of("/login/sys/kevin", false),
                Arguments.of("/login/sys/kevin", false)
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> headers() {
        return Stream.of(
                Arguments.of(HEADERS, true),
                Arguments.of(emptyMap(), false),
                Arguments.of(ImmutableMap.of("H", of("text2")), false),
                Arguments.of(ImmutableMap.of("HH", of("text2")), false)
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> queryParams() {
        return Stream.of(
                Arguments.of(QUERY_PARAMS, true),
                Arguments.of(emptyMap(), false),
                Arguments.of(ImmutableMap.of("A", of("2")), false),
                Arguments.of(ImmutableMap.of("AA", of("1")), false)
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> bodies() {
        return Stream.of(
                Arguments.of(BODY, true),
                Arguments.of("", false),
                Arguments.of("Other body", false)
        );
    }

    private static HttpRequestRule httpRequestRule(String pathPattern) {
        return new HttpRequestRule(Method.GET, pathPattern, emptyMap(), emptyMap(), new byte[0]);
    }

    private HttpRequestRule httpRequestRule(Map<String, List<String>> query, Map<String, List<String>> headers) {
        return new HttpRequestRule(Method.GET, PATH_PATTERN, query, headers, new byte[0]);
    }
}