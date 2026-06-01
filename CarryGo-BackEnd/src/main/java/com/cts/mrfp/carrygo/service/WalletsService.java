package com.cts.mrfp.carrygo.service;

import com.cts.mrfp.carrygo.model.Transactions;
import com.cts.mrfp.carrygo.model.Users;
import com.cts.mrfp.carrygo.model.Wallets;
import com.cts.mrfp.carrygo.repository.TransactionsRepository;
import com.cts.mrfp.carrygo.repository.UsersRepository;
import com.cts.mrfp.carrygo.repository.WalletsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

// Handles wallet balance changes and records a transaction row each time.
@Service
public class WalletsService {

    @Autowired
    private WalletsRepository walletsRepo;

    @Autowired
    private UsersRepository usersRepo;

    @Autowired
    private TransactionsRepository transactionsRepo;

    // Fetches the user's wallet. If they somehow don't have one yet, create it with 0 balance.
    public Wallets getOrCreateWallet(Integer userId) {
        return walletsRepo.findByUserUserId(userId).orElseGet(() -> {
            Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            Wallets w = new Wallets();
            w.setUser(user);
            w.setBalance(0f);
            w.setLastUpdated(LocalDateTime.now());
            return walletsRepo.save(w);
        });
    }

    public Wallets getWalletByUserId(Integer userId) {
        return getOrCreateWallet(userId);
    }

    // Internal helper used when paying a porter on DELIVERED.
    // Doesn't record a transaction row — keep it lightweight.
    public Wallets updateBalance(Integer userId, Float amount) {
        Wallets wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setLastUpdated(LocalDateTime.now());
        return walletsRepo.save(wallet);
    }

    // Adds money to the wallet and writes a CREDIT row to the transactions table.
    public Wallets topUp(Integer userId, Float amount) {
        Wallets wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setLastUpdated(LocalDateTime.now());
        walletsRepo.save(wallet);

        Transactions txn = new Transactions();
        txn.setWallet(wallet);
        txn.setType("CREDIT");
        txn.setAmount(amount);
        txn.setStatus("COMPLETED");
        txn.setCreatedAt(LocalDateTime.now());
        transactionsRepo.save(txn);

        return wallet;
    }

    // Removes money from the wallet and writes a DEBIT row.
    // The balance is allowed to go negative so the user isn't blocked from booking.
    public Wallets deduct(Integer userId, Float amount) {
        Wallets wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance() - amount);
        wallet.setLastUpdated(LocalDateTime.now());
        walletsRepo.save(wallet);

        Transactions txn = new Transactions();
        txn.setWallet(wallet);
        txn.setType("DEBIT");
        txn.setAmount(amount);
        txn.setStatus("COMPLETED");
        txn.setCreatedAt(LocalDateTime.now());
        transactionsRepo.save(txn);

        return wallet;
    }
}
