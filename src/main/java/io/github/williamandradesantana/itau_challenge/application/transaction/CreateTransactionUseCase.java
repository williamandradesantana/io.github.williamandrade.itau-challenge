package io.github.williamandradesantana.itau_challenge.application.transaction;

import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;
import io.github.williamandradesantana.itau_challenge.application.transaction.dtos.CreateTransactionCommand;
import io.github.williamandradesantana.itau_challenge.domain.transaction.Transaction;
import io.github.williamandradesantana.itau_challenge.domain.transaction.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateTransactionUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateTransactionUseCase.class);

    private final TransactionRepository transactionRepository;

    public CreateTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void execute(CreateTransactionCommand command) {
        if (command == null) throw new RequiredObjectIsNullException();

        log.info("Creating transaction");

        Transaction transaction = Transaction.create(command.amount(), command.datetime());
        transactionRepository.save(transaction);
    }
}
