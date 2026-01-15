package com.exam.fraudmonitorapp.repo;

import com.exam.fraudmonitorapp.model.FraudAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FraudAlertRepo extends JpaRepository<FraudAlertEntity, Long> {

    List<FraudAlertEntity> findByStatusOrderByCreatedAtDesc(String status);

}
