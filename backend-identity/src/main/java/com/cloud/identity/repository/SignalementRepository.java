package com.cloud.identity.repository;

import com.cloud.identity.entities.Signalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SignalementRepository extends JpaRepository<Signalement, UUID> {
}
