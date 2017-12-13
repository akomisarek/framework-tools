package uk.gov.justice.framework.tools.repository;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import uk.gov.justice.framework.tools.entity.TestEvent;

import java.util.UUID;

@Repository
public interface TestViewstoreRepository extends EntityRepository<TestEvent, UUID> {

}
