package io.github.williamandradesantana.itau_challenge.interfaces.transaction;

import io.github.williamandradesantana.itau_challenge.application.transaction.CalculateTransactionStatisticsUseCase;
import io.github.williamandradesantana.itau_challenge.application.transaction.CreateTransactionUseCase;
import io.github.williamandradesantana.itau_challenge.application.transaction.DeleteTransactionUseCase;
import io.github.williamandradesantana.itau_challenge.application.transaction.dtos.CalculateTransactionStatisticsOutput;
import io.github.williamandradesantana.itau_challenge.application.transaction.dtos.CreateTransactionCommand;
import io.github.williamandradesantana.itau_challenge.interfaces.transaction.dtos.TransactionRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transactions/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'COMMON')")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final DeleteTransactionUseCase deleteTransactionUseCase;
    private final CalculateTransactionStatisticsUseCase calculateTransactionStatisticsUseCase;

    public TransactionController(CreateTransactionUseCase createTransactionUseCase, DeleteTransactionUseCase deleteTransactionUseCase, CalculateTransactionStatisticsUseCase calculateTransactionStatisticsUseCase) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.deleteTransactionUseCase = deleteTransactionUseCase;
        this.calculateTransactionStatisticsUseCase = calculateTransactionStatisticsUseCase;
    }

    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CalculateTransactionStatisticsOutput> stats() {
        return ResponseEntity.ok(calculateTransactionStatisticsUseCase.execute());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createTransaction(@RequestBody @Valid TransactionRequest request) {
        CreateTransactionCommand command = new CreateTransactionCommand(request.amount(), request.dateTime());
        createTransactionUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable("id") UUID id) {
        deleteTransactionUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
