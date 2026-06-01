package com.cts.mrfp.carrygo.controller;
 
import com.cts.mrfp.carrygo.model.Transactions;
import com.cts.mrfp.carrygo.dto.TransactionsDTO;
import com.cts.mrfp.carrygo.service.TransactionsService;
import com.cts.mrfp.carrygo.util.DTOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
import java.util.stream.Collectors;
 
// Endpoints for the wallet's transaction history (credit / debit entries).
@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionsController {

    @Autowired
    private TransactionsService transactionsService;

    // POST /api/transactions — record a new transaction.
    @PostMapping
    public ResponseEntity<TransactionsDTO> createTransaction(@RequestBody TransactionsDTO transactionDTO) {
        Transactions transaction = new Transactions();
        transaction.setType(transactionDTO.getType());
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setStatus(transactionDTO.getStatus());

        Transactions created = transactionsService.createTransaction(transaction);
        return ResponseEntity.ok(DTOConverter.convertTransactionsToDTO(created));
    }

    // GET /api/transactions/wallet/{walletId} — history for one wallet.
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<TransactionsDTO>> getTransactionsByWallet(@PathVariable Integer walletId) {
        List<TransactionsDTO> dtos = transactionsService.getTransactionsByWallet(walletId).stream()
            .map(DTOConverter::convertTransactionsToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // GET /api/transactions — all transactions (admin / debug use).
    @GetMapping
    public List<TransactionsDTO> getAllTransactions() {
        return transactionsService.getAllTransactions().stream()
            .map(DTOConverter::convertTransactionsToDTO)
            .collect(Collectors.toList());
    }
}
