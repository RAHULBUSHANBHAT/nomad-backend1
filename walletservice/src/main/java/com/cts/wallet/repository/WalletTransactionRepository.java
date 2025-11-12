package com.cts.wallet.repository;

import com.cts.wallet.model.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, String> {
    
    // For the user's "My Transactions" page
    Page<WalletTransaction> findByWalletId(String walletId, Pageable pageable);
}