package com.exam.fraudmonitorapp.repo;

import com.exam.fraudmonitorapp.model.TransactionEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepo extends JpaRepository<TransactionEventEntity, Long> {

    List<TransactionEventEntity> findByAccountIdOrderByEventTimeDesc(String accountId);

    List<TransactionEventEntity> findByAccountIdAndEventTimeBetween(@Param("accountId") String accountId,
                                                                    @Param("start") LocalDateTime start,
                                                                    @Param("end") LocalDateTime end);

    List<String> findDistinctDeviceIdsByAccountId(@Param("accountId") String accountId);

}
