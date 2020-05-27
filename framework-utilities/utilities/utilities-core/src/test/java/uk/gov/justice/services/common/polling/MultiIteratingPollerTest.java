package uk.gov.justice.services.common.polling;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.polling.MultiIteratingPoller;
import uk.gov.justice.services.common.polling.Poller;
import uk.gov.justice.services.common.util.Sleeper;

import java.util.function.BooleanSupplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class MultiIteratingPollerTest {

    @Mock
    private Poller poller;

    @Mock
    private Sleeper sleeper;

    @Test
    public void shouldCallThePollerTheSpecifiedNumberOfTimesWithTheSpecifiedWaitTime() throws Exception {

        final int numberOfPollingIterations = 3;
        final long waitTimeBetweenIterationsMillis = 1000L;

        final MultiIteratingPoller multiIteratingPoller = new MultiIteratingPoller(
                numberOfPollingIterations,
                waitTimeBetweenIterationsMillis,
                poller,
                sleeper);

        final BooleanSupplier conditionalFunction = mock(BooleanSupplier.class);

        when(poller.pollUntilTrue(conditionalFunction)).thenReturn(false, false, true);

        assertThat(multiIteratingPoller.pollUntilTrue(conditionalFunction), is(true));

        verify(sleeper, times(2)).sleepFor(waitTimeBetweenIterationsMillis);
    }

    @Test
    public void shouldAlwaysCallThePollerTheSpecifiedNumberOfTimes() throws Exception {

        final int numberOfPollingIterations = 3;
        final long waitTimeBetweenIterationsMillis = 1000L;

        final MultiIteratingPoller multiIteratingPoller = new MultiIteratingPoller(
                numberOfPollingIterations,
                waitTimeBetweenIterationsMillis,
                poller,
                sleeper);

        final BooleanSupplier conditionalFunction = mock(BooleanSupplier.class);

        when(poller.pollUntilTrue(conditionalFunction)).thenReturn(true, true, true);

        assertThat(multiIteratingPoller.pollUntilTrue(conditionalFunction), is(true));

        verify(sleeper, times(2)).sleepFor(waitTimeBetweenIterationsMillis);
    }

    @Test
    public void shouldReturnFalseIfTheConditionIsFinallyFalse() throws Exception {

        final int numberOfPollingIterations = 3;
        final long waitTimeBetweenIterationsMillis = 1000L;

        final MultiIteratingPoller multiIteratingPoller = new MultiIteratingPoller(
                numberOfPollingIterations,
                waitTimeBetweenIterationsMillis,
                poller,
                sleeper);

        final BooleanSupplier conditionalFunction = mock(BooleanSupplier.class);

        when(poller.pollUntilTrue(conditionalFunction)).thenReturn(false, true, false);

        assertThat(multiIteratingPoller.pollUntilTrue(conditionalFunction), is(false));

        verify(sleeper, times(numberOfPollingIterations)).sleepFor(waitTimeBetweenIterationsMillis);
    }
}
