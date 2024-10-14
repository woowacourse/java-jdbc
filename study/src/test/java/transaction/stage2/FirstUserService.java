package transaction.stage2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FirstUserService {

    private static final Logger log = LoggerFactory.getLogger(FirstUserService.class);

    private final UserRepository userRepository;
    private final SecondUserService secondUserService;

    @Autowired
    public FirstUserService(final UserRepository userRepository,
                            final SecondUserService secondUserService) {
        this.userRepository = userRepository;
        this.secondUserService = secondUserService;
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Set<String> saveFirstTransactionWithRequired() {
        TransactionSynchronizationManager.setCurrentTransactionName("REQUIRED");
        final var firstTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        userRepository.save(User.createTest());
        logActualTransactionActive();

        final var secondTransactionName = secondUserService.saveSecondTransactionWithRequired();

        return of(firstTransactionName, secondTransactionName);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Set<String> saveFirstTransactionWithRequiredNew() {
        TransactionSynchronizationManager.setCurrentTransactionName("REQUIRED_NEW");
        final var firstTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        userRepository.save(User.createTest());
        logActualTransactionActive();

        final var secondTransactionName = secondUserService.saveSecondTransactionWithRequiresNew();

        return of(firstTransactionName, secondTransactionName);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Set<String> saveAndExceptionWithRequiredNew() {
        secondUserService.saveSecondTransactionWithRequiresNew();

        userRepository.save(User.createTest());
        logActualTransactionActive();

        throw new RuntimeException();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Set<String> saveFirstTransactionWithSupports() {
        final var firstTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        userRepository.save(User.createTest());
        logActualTransactionActive();

        final var secondTransactionName = secondUserService.saveSecondTransactionWithSupports();

        return of(firstTransactionName, secondTransactionName);
    }

        @Transactional(propagation = Propagation.REQUIRED)
    public Set<String> saveFirstTransactionWithMandatory() {
        final var firstTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        userRepository.save(User.createTest());
        logActualTransactionActive();

        final var secondTransactionName = secondUserService.saveSecondTransactionWithMandatory();

        return of(firstTransactionName, secondTransactionName);
    }

//    @Transactional(propagation = Propagation.REQUIRED)
    public Set<String> saveFirstTransactionWithNotSupported() {
        final var firstTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        userRepository.save(User.createTest());
        logActualTransactionActive();

        final var secondTransactionName = secondUserService.saveSecondTransactionWithNotSupported();

        return of(firstTransactionName, secondTransactionName);
    }

//    @Transactional(propagation = Propagation.REQUIRED)
    public Set<String> saveFirstTransactionWithNested() {
        final var firstTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        userRepository.save(User.createTest());
        logActualTransactionActive();

        final var secondTransactionName = secondUserService.saveSecondTransactionWithNested();

        return of(firstTransactionName, secondTransactionName);
    }

//    @Transactional(propagation = Propagation.REQUIRED)
    public Set<String> saveFirstTransactionWithNever() {
        final var firstTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        userRepository.save(User.createTest());
        logActualTransactionActive();

        final var secondTransactionName = secondUserService.saveSecondTransactionWithNever();

        return of(firstTransactionName, secondTransactionName);
    }

    private Set<String> of(final String firstTransactionName, final String secondTransactionName) {
        if (firstTransactionName == null && secondTransactionName == null) {
            return Set.of("NO_TRANSACTION");
        }
        if (firstTransactionName == null) {
            return Set.of("SECOND");
        }
        if (secondTransactionName == null) {
            return Set.of("FIRST");
        }
        if (firstTransactionName.equals(secondTransactionName)) {
            return Set.of("SAME");
        }
        return Set.of("FIRST", "SECOND");
    }

    private void logActualTransactionActive() {
        final var currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        final var actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        final var emoji = actualTransactionActive ? "✅" : "❌";
        log.info("\n{} is Actual Transaction Active : {} {}", currentTransactionName, emoji, actualTransactionActive);
    }
}
