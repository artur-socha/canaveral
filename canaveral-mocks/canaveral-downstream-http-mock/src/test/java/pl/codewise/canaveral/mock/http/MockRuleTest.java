package pl.codewise.canaveral.mock.http;

import com.google.common.collect.ImmutableList;
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

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class MockRuleTest {

    @Mock
    private HttpRequestRule request;

    @Mock
    private HttpResponseRule response;

    @Mock
    private HttpRawRequest inRequest;

    @Mock
    private Map<String, List<String>> multiMap;

    private MockRule mockRule;

    private Map<String, List<String>> queryParams = ImmutableMap.of("A", ImmutableList.of("1"));
    private Map<String, List<String>> headers = ImmutableMap.of("H", ImmutableList.of("text"));

    @SuppressWarnings("unused")
    private static Stream<Arguments> simplePaths() {
        return Stream.of(
                Arguments.of("login", true),
                Arguments.of("login/a", false),
                Arguments.of("a/login", false)
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> pathPatterns() {
        return Stream.of(
                Arguments.of("login/sys/user/kevin", true),
                Arguments.of("login/user/", true),
                Arguments.of("a/login/sys/user/kevin", false),
                Arguments.of("/login/sys/kevin", false),
                Arguments.of("login/sys/kevin", false)
        );
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(request.getPathPattern()).thenReturn("/login");
        when(request.getHeaders()).thenReturn(emptyMap());
        when(request.getQuery()).thenReturn(emptyMap());
        when(request.getBody()).thenReturn(new byte[0]);
        when(request.getMethod()).thenReturn(Method.GET);

        when(inRequest.getMethod()).thenReturn(Method.GET);
        when(inRequest.getPath()).thenReturn("login");
        when(inRequest.getHeaders()).thenReturn(null);
        when(inRequest.getQueryParams()).thenReturn(null);

        mockRule = MockRule.create(request, response);
    }

    @Test
    void shouldProvideRequestAndResponse() {
        // given

        // when

        // then
        assertThat(mockRule.getRequest()).isEqualTo(request);
        assertThat(mockRule.getResponse()).isEqualTo(response);
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
        when(request.getPathPattern()).thenReturn("/login.*/user/.*");
        mockRule = MockRule.create(request, response);

        when(inRequest.getPath()).thenReturn(inPath);

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isEqualTo(shouldMatch);
    }

    @Test
    void shouldMatchWhenHeadersAndQueryAreMissing() {
        // given

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldMatchOnEmptyHeadersAndEmptyParams() {
        // given
        when(inRequest.getHeaders()).thenReturn(emptyMap());
        when(inRequest.getQueryParams()).thenReturn(emptyMap());

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isTrue();
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

    @Test
    void shouldFailOnMatchingDifferentInputHeaders() {
        // given
        when(inRequest.getHeaders()).thenReturn(multiMap);
        when(multiMap.size()).thenReturn(1);

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldFailOnMatchingDifferentRuleHeaders() {
        // given
        when(request.getHeaders()).thenReturn(queryParams);

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isFalse();
    }

    @Test
    void shouldFailOnMatchingDifferentInputQueryParams() {
        // given
        when(inRequest.getQueryParams()).thenReturn(multiMap);
        when(multiMap.size()).thenReturn(1);
        when(multiMap.get("A")).thenReturn(ImmutableList.of("1"));

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldFailOnMatchingDifferentRuleQueryParams() {
        // given
        when(request.getQuery()).thenReturn(queryParams);

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isFalse();
    }

    @Test
    void shouldMatchOnHeaders() {
        // given
        when(request.getHeaders()).thenReturn(headers);
        when(inRequest.getHeaders()).thenReturn(headers);
        when(multiMap.size()).thenReturn(1);

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldMatchOnMultipleHeaders() {
        // given
        ImmutableMap<String, List<String>> ruleHeaders = ImmutableMap.of(
                "A", ImmutableList.of("1", "2"),
                "B", ImmutableList.of("3"));
        when(request.getHeaders()).thenReturn(ruleHeaders);
        when(inRequest.getHeaders()).thenReturn(ImmutableMap.of(
                "A", ImmutableList.of("1", "2"),
                "B", ImmutableList.of("3", "4")));
        when(multiMap.size()).thenReturn(4);

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldMatchOnQueryParams() {
        when(request.getQuery()).thenReturn(queryParams);
        when(inRequest.getQueryParams()).thenReturn(multiMap);
        when(multiMap.size()).thenReturn(1);
        when(multiMap.get("A")).thenReturn(ImmutableList.of("1"));

        // when
        boolean actual = mockRule.getCondition().test(inRequest);

        // then
        assertThat(actual).isTrue();
    }
}