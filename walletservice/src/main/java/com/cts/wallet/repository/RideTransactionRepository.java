package com.cts.wallet.repository;

import com.cts.wallet.model.RideTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RideTransactionRepository extends JpaRepository<RideTransaction, String>, JpaSpecificationExecutor<RideTransaction> {
    
}