package com.cts.mrfp.carrygo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.mrfp.carrygo.model.Transactions;
import java.util.List;
 
// JPA queries for the transactions table.
public interface TransactionsRepository extends JpaRepository<Transactions, Integer> {
    // Transaction history for one wallet.
    List<Transactions> findByWalletWalletId(Integer walletId);
}