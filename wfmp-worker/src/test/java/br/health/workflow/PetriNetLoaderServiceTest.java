package br.health.workflow;

import br.health.workflow.core.communication.AsyncCommunication;
import br.health.workflow.core.communication.SyncCommunication;
import br.health.workflow.service.PetriNetLoaderService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
public class PetriNetLoaderServiceTest {

    @InjectMocks
    private PetriNetLoaderService petriNetLoaderService;

    @Mock
    private TaskExecutor taskExecutor;

    @Mock
    private SyncCommunication synchronousCommunication;

    @Mock
    private AsyncCommunication asynchronousCommunication;

    @Before
    public void initMocks() {

    }

    @Test
    public void loadSemanticModelTest() {
        Assert.assertThat("Test", 1, is(1));
    }

}
