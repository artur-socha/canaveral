package com.ffb.canaveral2.core.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.lang.annotation.Annotation;
import java.time.Clock;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.ffb.canaveral2.core.runtime.FullRunnerConfigurationProvider.applicationProviderMock;
import static com.ffb.canaveral2.core.runtime.FullRunnerConfigurationProvider.testContextMock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
class RunnerTest
{

    private Map<String, RunnerCache> cache;
    private Runner runner;

    @BeforeEach
    void setUp()
    {
        cache = new HashMap<>();
        runner = new Runner(cache);

        Mockito.reset(applicationProviderMock);
        Mockito.reset(testContextMock);
        Mockito.reset(MockedRunnerConfigurationProvider.configurationMock);
    }

    @AfterEach
    void tearDown()
    {
        cache.forEach((key, cache) -> runner.clearRunnerCache(cache));
    }

    @Test
    void shouldInitializeTestWithMinimalConfiguration()
    {

        // when
        runner.configureRunnerForTest(MinimalRunnerConfigurationTestClass.class);

        // then
        assertThat(cache).hasSize(1);
        assertThat(cache.values())
                .extracting(RunnerCache::isNotInitialized).containsOnly(false);
    }

    @Test
    void shouldNotReinitializeRunnerContextForSameConfiguartion()
    {
        runner.configureRunnerForTest(MinimalRunnerConfigurationTestClass.class);

        // when
        runner.configureRunnerForTest(MinimalRunnerConfigurationTestClass.class);

        // then
        assertThat(cache).hasSize(1);
    }

    @Test
    void shouldHandleErrorWhileInitializing()
    {
        Exception expectedException = new RuntimeException("test - fail on initialization");

        doThrow(expectedException)
                .when(MockedRunnerConfigurationProvider.configurationMock)
                .getSystemProperties();

        // when
        assertThatThrownBy(() -> runner.configureRunnerForTest(MockedRunnerConfigurationTestClass.class))
                .hasCause(expectedException)
                .isInstanceOf(RunnerInitializationException.class);
        assertThatThrownBy(() -> runner.configureRunnerForTest(MockedRunnerConfigurationTestClass.class))
                .hasCause(expectedException);

        // then
        RunnerCache runnerCache = cache.get(MockedRunnerConfigurationProvider.class.getCanonicalName());
        assertThat(runnerCache.isNotInitialized()).isTrue();
        assertThat(runnerCache.hasInitializationAlreadyFailed()).isTrue();
        assertThat(runnerCache.getInitializationCause()).isInstanceOf(RunnerInitializationException.class);
        assertThat(runnerCache.getInitializationCause()).hasCause(expectedException);

        verify(MockedRunnerConfigurationProvider.configurationMock, times(1)).getSystemProperties();
    }

    @Test
    void shouldInitializeTestsWithDifferentConfigurations()
    {
        when(applicationProviderMock.canProceed(ArgumentMatchers.any()))
                .thenReturn(true);
        when(testContextMock.canProceed(ArgumentMatchers.any()))
                .thenReturn(true);

        runner.configureRunnerForTest(FullRunnerConfigurationTestClass.class);

        // when
        runner.configureRunnerForTest(MinimalRunnerConfigurationTestClass.class);

        // then
        assertThat(cache).hasSize(2);
        assertThat(cache.values())
                .extracting(RunnerCache::isNotInitialized).containsOnly(false);
    }

    @Test
    void shouldInitializeFullConfiguration()
    {
        when(applicationProviderMock.canProceed(ArgumentMatchers.any()))
                .thenReturn(true);
        when(testContextMock.canProceed(ArgumentMatchers.any()))
                .thenReturn(true);


        // when
        runner.configureRunnerForTest(FullRunnerConfigurationTestClass.class);

        // then
        assertThat(cache).hasSize(1);
        RunnerCache runnerCache = cache.get(FullRunnerConfigurationProvider.class.getCanonicalName());
        assertThat(runnerCache.isNotInitialized()).isFalse();

        verify(applicationProviderMock).start(runnerCache);
        verify(applicationProviderMock).canProceed(runnerCache);
        verify(testContextMock).initialize(runnerCache);
        verify(testContextMock).canProceed(runnerCache);
    }

    @Test
    void shouldNotProceedWithFullConfigurationInitializationIfApplicationCannotStart()
    {
        when(applicationProviderMock.canProceed(ArgumentMatchers.any()))
                .thenReturn(false);

        String canonicalName = applicationProviderMock.getClass().getCanonicalName();
        InitializationError expected = new InitializationError("Application is not ready yet. See configured progress assertion.");

        // when
        assertThatThrownBy(() -> runner.configureRunnerForTest(FullRunnerConfigurationTestClass.class))
                // then
                .isInstanceOf(RunnerInitializationException.class)
                .hasCause(expected);
        assertThat(cache).hasSize(1);
        RunnerCache runnerCache = cache.get(FullRunnerConfigurationProvider.class.getCanonicalName());
        assertThat(runnerCache.isNotInitialized()).isTrue();

        verify(applicationProviderMock).start(runnerCache);
        verify(applicationProviderMock).canProceed(runnerCache);
        verify(testContextMock, never()).initialize(any());
        verify(testContextMock, never()).canProceed(any());
    }

    @Test
    void shouldNotProceedWithFullConfigurationInitializationIfTestContextCannotStart()
    {
        when(applicationProviderMock.canProceed(ArgumentMatchers.any()))
                .thenReturn(true);
        when(testContextMock.canProceed(ArgumentMatchers.any()))
                .thenReturn(false);

        String canonicalName = testContextMock.getClass().getCanonicalName();
        InitializationError expected = new InitializationError("Test context is not ready yet. See configured progress assertion.");
        // when
        assertThatThrownBy(() -> runner.configureRunnerForTest(FullRunnerConfigurationTestClass.class))
                // then
                .isInstanceOf(RunnerInitializationException.class)
                .hasCause(expected);

        // then
        assertThat(cache).hasSize(1);
        RunnerCache runnerCache = cache.get(FullRunnerConfigurationProvider.class.getCanonicalName());
        assertThat(runnerCache.isNotInitialized()).isTrue();

        verify(applicationProviderMock).start(runnerCache);
        verify(applicationProviderMock).canProceed(runnerCache);
        verify(testContextMock).initialize(runnerCache);
        verify(testContextMock).canProceed(runnerCache);
    }

    @Test
    void shouldCallAfterAllMockCreated()
    {
        // when
        runner.configureRunnerForTest(MinimalRunnerConfigurationTestClass.class);

        // then
        assertThat(cache).hasSize(1);
        RunnerCache runnerCache = cache.get(MinimalRunnerConfigurationProvider.class.getCanonicalName());
        DummyMockProvider mock = runnerCache.getMock(DummyMockProvider.class);

        assertThat(mock.cacheReference.get()).isSameAs(runnerCache);
        assertThat(mock.calledAfterAllMocksCreated.get()).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldInjectRequestedMocks()
    {
        Clock clock = Clock.systemDefaultZone();
        ObjectMapper objectMapper = new ObjectMapper();
        Clock testClock = Clock.system(ZoneId.of("UTC+9"));

        when(applicationProviderMock.canProceed(ArgumentMatchers.any())).thenReturn(true);
        when(testContextMock.canProceed(ArgumentMatchers.any())).thenReturn(true);

        when(applicationProviderMock.findBeanOrThrow(eq(Clock.class), any())).thenReturn(clock);
        when(applicationProviderMock.findBeanOrThrow(eq(ObjectMapper.class), any())).thenReturn(objectMapper);
        when(testContextMock.findBeanOrThrow(eq(Clock.class), any())).thenReturn(testClock);

        TestInstanceHelper testInstanceHelper = runner.configureRunnerForTest(FullRunnerConfigurationTestClass.class);
        FullRunnerConfigurationTestClass testInstance = new FullRunnerConfigurationTestClass();

        // when
        testInstanceHelper.initializeTestInstance(testInstance);

        // then
        assertThat(cache).hasSize(1);
        RunnerCache runnerCache = cache.get(FullRunnerConfigurationProvider.class.getCanonicalName());
        assertThat(runnerCache.isNotInitialized()).isFalse();

        assertThat(testInstance.clock).isSameAs(clock);
        assertThat(testInstance.testClock).isSameAs(testClock);

        assertThat(testInstance.mapper).isSameAs(objectMapper);

        assertThat(testInstance.mockProvider).isNotSameAs(testInstance.otherDummyMockProvider);
        assertThat(testInstance.mockProvider).isNotNull();
        assertThat(testInstance.otherDummyMockProvider).isNotNull();

        ArgumentCaptor<Set<Annotation>> passedAnnotationCaptor = ArgumentCaptor.forClass(Set.class);
        verify(applicationProviderMock).findBeanOrThrow(eq(ObjectMapper.class), passedAnnotationCaptor.capture());

        assertThat(passedAnnotationCaptor.getValue()).hasSize(2);
    }
}