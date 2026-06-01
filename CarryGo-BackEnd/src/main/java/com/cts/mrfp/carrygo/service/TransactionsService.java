package com.cts.mrfp.carrygo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.mrfp.carrygo.model.Transactions;
import com.cts.mrfp.carrygo.repository.TransactionsRepository;

// Simple CRUD for the transactions table (wallet credits and debits).
@Service
public class TransactionsService {

    @Autowired
    private TransactionsRepository transactionsRepository;

    // Saves a transaction with the current timestamp.
    public Transactions createTransaction(Transactions transaction) {
        transaction.setCreatedAt(LocalDateTime.now());
        return transactionsRepository.save(transaction);
    }

    public List<Transactions> getTransactionsByWallet(Integer walletId) {
        return transactionsRepository.findByWalletWalletId(walletId);
    }

    public List<Transactions> getAllTransactions() {
        return transactionsRepository.findAll();
    }
}