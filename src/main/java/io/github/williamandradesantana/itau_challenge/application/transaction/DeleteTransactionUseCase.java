package io.github.williamandradesantana.itau_challenge.application.transaction;

import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.ResourceNotFoundException;
import io.github.williamandradesantana.itau_challenge.domain.transaction.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DeleteTransactionUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteTransactionUseCase.class);

    private final TransactionRepository transactionRepository;

    public DeleteTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void execute(UUID id) {
        if (id == null) throw new RequiredObjectIsNullException();
        transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        log.info("Deleting transaction with id: {}", id);
        transactionRepository.delete(id);
    }
}
